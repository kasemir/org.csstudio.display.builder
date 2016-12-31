/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.test;

import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.csstudio.display.builder.runtime.CommandExecutor;
import org.junit.Test;

/** JUnit test of the CommandExecutor
 *  @author Kay Kasemir
 */
public class CommandExecutorTest
{
    /** Example shell scripts are only functional on Linux and Mac OS X.
     *  Skip tests on Windows.
     */
    private static final boolean is_windows = System.getProperty("os.name").startsWith("Windows");
    private ByteArrayOutputStream log_buf;
    private StreamHandler handler;

    @Test
    public void testCommandSplit() throws Exception
    {
        List<String> cmd = CommandExecutor.splitCmd("path/cmd");
        System.out.println(cmd);
        assertThat(cmd, equalTo(Arrays.asList("path/cmd")));

        cmd = CommandExecutor.splitCmd("path/cmd arg1");
        System.out.println(cmd);
        assertThat(cmd, equalTo(Arrays.asList("path/cmd", "arg1")));

        cmd = CommandExecutor.splitCmd("path/cmd arg1 arg2");
        System.out.println(cmd);
        assertThat(cmd, equalTo(Arrays.asList("path/cmd", "arg1", "arg2")));

        cmd = CommandExecutor.splitCmd("path/cmd \"one arg\"");
        System.out.println(cmd);
        assertThat(cmd, equalTo(Arrays.asList("path/cmd", "one arg")));

        cmd = CommandExecutor.splitCmd("path/cmd \"one arg\" arg2 arg3");
        System.out.println(cmd);
        assertThat(cmd, equalTo(Arrays.asList("path/cmd", "one arg", "arg2", "arg3")));
    }

    public void setupLogger()
    {
        // 1-date, 2-source, 3-logger, 4-level, 5-message, 6-thrown
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
        log_buf = new ByteArrayOutputStream();
        handler = new StreamHandler(log_buf, new SimpleFormatter());
        logger.addHandler(handler);
    }

    public String getLoggedMessages()
    {
        handler.flush();
        return log_buf.toString();
    }

    @Test
    public void testShortCommand() throws Exception
    {
        if (is_windows)
            return;
        setupLogger();

        final String cmd = new File("examples/cmd_short.sh \"With one arg\" another_arg").getAbsolutePath();
        final Integer status = new CommandExecutor(cmd, new File("examples")).call();

        final String log = getLoggedMessages();
        assertThat(log, containsString("Example warning"));
        assertThat(log, containsString("2 arguments"));
        assertThat(log, containsString("Finished OK"));
        assertThat(status, equalTo(0));
    }

    @Test
    public void testErrorCommand() throws Exception
    {
        if (is_windows)
            return;
        setupLogger();

        final String cmd = new File("examples/cmd_short.sh").getAbsolutePath();
        // Start one directory 'up' to generate error
        final Integer status = new CommandExecutor(cmd, new File("examples").getParentFile()).call();

        final String log = getLoggedMessages();
        assertThat(log, containsString("Wrong directory"));
        assertThat(log, containsString("exited with status 2"));
        assertThat(status, equalTo(2));
    }

    @Test
    public void testLongCommand() throws Exception
    {
        if (is_windows)
            return;
        setupLogger();

        final String cmd = new File("examples/cmd_long.sh").getAbsolutePath();
        final CommandExecutor executor = new CommandExecutor(cmd, new File("examples"));
        System.out.println(executor);
        assertThat(executor.toString(), containsString("(idle)"));
        final Integer status = executor.call();

        // No exit status since process remains running
        System.out.println(executor + " call() returned " + status);
        assertThat(executor.toString(), containsString("(running)"));
        assertThat(status, nullValue());

        // Messages continue to be logged
        int log_writers = 0;
        Thread[] threads = new Thread[10];
        Thread.enumerate(threads);
        for (Thread thread : threads)
            if (thread != null  &&  thread.getName().contains("LogWriter"))
            {
                System.out.println("Found " + thread.getName());
                ++log_writers;
            }
        assertThat(log_writers, equalTo(2));

        // Wait for external process to end
        String log = getLoggedMessages();
        int wait = 20;
        while (! log.contains("Finished OK"))
        {
            TimeUnit.SECONDS.sleep(1);
            log = getLoggedMessages();
            if (--wait < 0)
                throw new TimeoutException();
        }
        System.out.println(executor);
        assertThat(executor.toString(), containsString("(0)"));

        // Wait a little longer to allow checking in debugger
        // that the LogWriter threads exit
        TimeUnit.SECONDS.sleep(5);

        log_writers = 0;
        Thread.enumerate(threads);
        for (Thread thread : threads)
            if (thread != null  &&  thread.isAlive()  &&  thread.getName().contains("LogWriter"))
            {
                System.out.println("Found " + thread.getName());
                ++log_writers;
            }
        assertThat(log_writers, equalTo(0));
    }
}
