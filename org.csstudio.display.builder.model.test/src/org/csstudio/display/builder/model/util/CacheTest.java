/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.util;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/** JUnit test of {@link Cache}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class CacheTest
{
    final long start = System.currentTimeMillis();

    private String trace()
    {
        final long ms = System.currentTimeMillis() - start;
        return String.format("%5.3f [%35s] ", ms/1000.0, Thread.currentThread().getName());
    }

    private String createEntry(final String key) throws Exception
    {
        final String value = "Entry for " + key;
        System.out.println(trace() + ">> Creating " + value + " ...");
        if (key.equals("A"))
            TimeUnit.SECONDS.sleep(2);
        System.out.println(trace() + "<< Returned " + value);
               return value;
    }

    @Test
    public void testCache() throws Exception
    {
        System.out.println(trace() + "Start");
        final Cache<String> cache = new Cache<>(Duration.ofSeconds(2));

        final AtomicReference<String> A = new AtomicReference<>();
        final ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(() ->
        {
            final String key = "A";
            System.out.println(trace() + "> Requesting " + key + " for 1st time ...");
            A.set(cache.getCachedOrNew(key, this::createEntry));
            System.out.println(trace() + "< Got initial" + key);
            return null;
        });
        pool.submit(() ->
        {
            final String key = "B";
            System.out.println(trace() + "> Requesting " + key + "...");
            cache.getCachedOrNew(key, this::createEntry);
            System.out.println(trace() + "< Got " + key);
            return null;
        });
        pool.submit(() ->
        {
            final String key = "A";
            System.out.println(trace() + "> Requesting " + key + " again (cached)...");
            cache.getCachedOrNew(key, this::createEntry);
            System.out.println(trace() + "< Got cached " + key);
            return null;
        });

        String A2 = cache.getCachedOrNew("A", this::createEntry);
        assertThat(A2, equalTo("Entry for A"));
        assertThat(A2, sameInstance(A.get()));

        System.out.println(trace() + "Allowing to expire");
        TimeUnit.SECONDS.sleep(3);
        A2 = cache.getCachedOrNew("A", this::createEntry);
        assertThat(A2, not(sameInstance(A.get())));
    }
}
