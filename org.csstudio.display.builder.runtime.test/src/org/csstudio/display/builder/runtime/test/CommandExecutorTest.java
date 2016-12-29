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
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.csstudio.display.builder.runtime.CommandExecutor;
import org.junit.Test;

/** JUnit test of the CommandExecutor
 *  @author Kay Kasemir
 */
public class CommandExecutorTest
{
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
        setupLogger();

        final String cmd = new File("examples/cmd_short.sh \"With one arg\"").getAbsolutePath();
        new CommandExecutor(cmd, new File("examples")).call();

        final String log = getLoggedMessages();
        assertThat(log, containsString("Example warning"));
        assertThat(log, containsString("Finished OK"));
    }

    @Test
    public void testErrorCommand() throws Exception
    {
        setupLogger();

        final String cmd = new File("examples/cmd_error.sh").getAbsolutePath();
        new CommandExecutor(cmd, new File("examples")).call();

        final String log = getLoggedMessages();
        assertThat(log, containsString("Example ERROR"));
        assertThat(log, containsString("exited with status 2"));
    }
}
