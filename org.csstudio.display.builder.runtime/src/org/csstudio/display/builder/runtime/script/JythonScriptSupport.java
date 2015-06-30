/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.script;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.util.NamedDaemonPool;
import org.csstudio.vtype.pv.PV;
import org.python.core.PyCode;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/** Jython script support
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class JythonScriptSupport
{
    private final ExecutorService executor;
    private final PySystemState state;
    private final PythonInterpreter python;

    public JythonScriptSupport() throws Exception
    {
        executor = Executors.newSingleThreadExecutor(new NamedDaemonPool("JythonScriptSupport"));
        state = new PySystemState();
        python = new PythonInterpreter(null, state);
    }

    /** Parse and compile script file
     *
     *  @param name Name of script (file name, URL)
     *  @param stream Stream for the script content
     *  @return {@link Script}
     *  @throws Exception on error
     */
    public Script compile(final String name, final InputStream stream) throws Exception
    {
        final PyCode code = python.compile(new InputStreamReader(stream), name);
        return new JythonScript(this, name, code);
    }

    Future<Object> submit(final JythonScript script, final Widget widget, final PV... pvs)
    {
        // System.out.println("Submit on " + Thread.currentThread().getName());
        return executor.submit(() ->
        {
            // System.out.println("Execute on " + Thread.currentThread().getName());
            try
            {
                python.set("widget", widget);
                python.set("pvs", pvs);
                python.exec(script.getCode());
            }
            catch (final Throwable ex)
            {
                Logger.getLogger(JythonScriptSupport.class.getName())
                    .log(Level.WARNING,
                         "Execution of '" + script.getName() + "' failed", ex);
            }
            return null;
        });
    }

    /** Release resources (interpreter, ...) */
    public void close()
    {
        executor.shutdown();
        python.close();
    }
}
