/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** Factory of named daemon threads
 *
 *  <p>Primarily, this allows using the Executors.*
 *  with threads names that can be recognized in the
 *  debugger.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class NamedDaemonPool implements ThreadFactory
{
    private final String name;

    private final AtomicInteger instance = new AtomicInteger();

    public NamedDaemonPool(final String name)
    {
        this.name = name;
    }

    @Override
    public Thread newThread(final Runnable target)
    {
        final int inst = instance.incrementAndGet();
        final String thread_name = (inst == 1)
                ? name
                : name + "-" + inst;
        final Thread thread = new Thread(target, thread_name);
        thread.setDaemon(true);
        return thread;
    }
}
