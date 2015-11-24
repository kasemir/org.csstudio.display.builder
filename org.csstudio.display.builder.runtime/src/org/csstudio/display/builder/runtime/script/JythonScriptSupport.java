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
import org.csstudio.vtype.pv.PV;
import org.python.core.PyCode;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/** Jython script support
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
class JythonScriptSupport
{
    private final ExecutorService executor;
    private final PySystemState state;
    private final PythonInterpreter python;

    /** Create executor for jython scripts */
    public JythonScriptSupport() throws Exception
    {
        executor = Executors.newSingleThreadExecutor(ScriptSupport.POOL);
        state = new PySystemState();

        // TODO Figure out how to best handle this.
        // Setting this options prevents
        // "ImportError: Cannot import site module and its dependencies: No module named site"
        System.setProperty("python.import.site", "false");

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

    /** Request that a script gets executed
     *  @param script {@link JythonScript}
     *  @param widget Widget that requests execution
     *  @param pvs PVs that are available to the script
     *  @return
     */
    public Future<Object> submit(final JythonScript script, final Widget widget, final PV... pvs)
    {
        // TODO Add script throttle/check
        // Instead of simply submitting for execution,
        // submit to a queue off which a thread executes items.
        // If the queue already contains this script/widget/pvs,
        // skip adding another one.
        // That way, if one widget & PV generates many submissions,
        // it does not flood the queue but only maintains one active request.

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
