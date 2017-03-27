/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.util;

import static org.csstudio.display.builder.model.ModelPlugin.logger;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;

/** Generic cache
 *
 *  <p>When fetching an entry, it will return
 *  a previously submitted entry that's still valid.
 *  If there is none, or it has expired, a new entry is created.
 *
 *  @author Kay Kasemir
 *
 *  @param <T> Value type for cache entries
 */
@SuppressWarnings("nls")
public class Cache<T>
{
    private final Duration timeout;

    @FunctionalInterface
    public interface CreateEntry<K, V>
    {
        public V create(K key) throws Exception;
    };

    private class Entry
    {
        private final T value;
        private final Instant expire;

        public Entry(final T value)
        {
            this.value = value;
            this.expire = Instant.now().plus(timeout);
        }

        public T getValue()
        {
            return value;
        }

        public boolean isExpired()
        {
            return Instant.now().isAfter(expire);
        }
    };
    private final ConcurrentHashMap<String, Future<Entry>> cache = new ConcurrentHashMap<>();

    /** @param timeout How long entries remain valid */
    public Cache(final Duration timeout)
    {
        this.timeout = timeout;
    }

    /** Get existing entry or create new one
     *  @param key Key for entry
     *  @param creator Function to create entry, if there is none in the cache
     *  @return Entry, either the newly created one or a previously cached one
     *  @throws Exception on error
     */
    public T getCachedOrNew(final String key, final CreateEntry<String, T> creator) throws Exception
    {
        // In case two concurrent callers request the same key,
        // first one will submit the future,
        // second one will just 'get' the future submitted by first one.
        final Future<Entry> future_entry = cache.computeIfAbsent(key, k ->
        {
            final Callable<Entry> create_entry = () -> new Entry(creator.create(key));
            return ModelThreadPool.getExecutor().submit(create_entry);
        });
        // Both concurrent threads will await future
        final Entry entry = future_entry.get();

        // Check expiration
        if (! entry.isExpired())
            return entry.getValue();
        // Re-create
        // Two threads might concurrently find an expired entry.
        // Both will remove it (second one is then a NOP),
        // and then create it, where only first one calls the supplier:
        cache.remove(key);
        return getCachedOrNew(key, creator);
    }

    // TODO Call something like this to perform regular cleanup
    public void schedule_cleanup()
    {
        ModelThreadPool.getExecutor().submit(() ->
        {
            try
            {
                Thread.sleep(timeout.toMillis());
            }
            catch (InterruptedException ex)
            {
                // Ignore
                return;
            }

            cache.forEach((key, future) ->
            {
                try
                {
                    if (future.isDone()  &&  future.get().isExpired())
                        cache.remove(key, future);
                }
                catch (Exception ex)
                {
                    logger.log(Level.WARNING, "Cannot clean URL cache", ex);
                }
            });
        });
    }
}
