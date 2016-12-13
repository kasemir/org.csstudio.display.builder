/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.persist;

import static org.csstudio.display.builder.model.ModelPlugin.logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/** Test helper: Delayed access to a file
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DelayedStream implements Callable<InputStream>
{
    private final String filename;
    private final int seconds;

    public DelayedStream(final String filename, final int seconds)
    {
        this.filename = filename;
        this.seconds = seconds;
    }

    @Override
    public InputStream call() throws Exception
    {
        logger.warning("Delaying file access.. on " + Thread.currentThread().getName());
        TimeUnit.SECONDS.sleep(seconds);
        logger.warning("Finally opening the file");
        return new FileInputStream(filename);
    }
}