/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.script;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Base for JavaScript and Jython script support
 *
 *  <p>Tracks running scripts to cancel them on shutdown.
 *
 *  @author Kay Kasemir
 */
class BaseScriptSupport
{
    private final ExecutorService executor = Executors.newSingleThreadExecutor(ScriptSupport.POOL);
    private final Queue<Future<Object>> active_scripts = new ConcurrentLinkedQueue<>();

    /** Request that a script gets executed
     *  @param callable {@link Callable} for executing the script
     *  @return Future for script that was just started
     */
    protected Future<Object> submit(final Callable<Object> callable)
    {
        final Future<Object> running = executor.submit(callable);
        // No longer track scripts that have finished
        active_scripts.removeIf(f -> f.isDone());
        active_scripts.add(running);
        return running;
    }

    /** Release resources (interpreter, ...) */
    public void close()
    {
        // Prevent new scripts from starting
        executor.shutdown();
        // Interrupt scripts which are still running
        // (OK to cancel() if script already finished)
        for (Future<Object> running : active_scripts)
            running.cancel(true);
    }
}
