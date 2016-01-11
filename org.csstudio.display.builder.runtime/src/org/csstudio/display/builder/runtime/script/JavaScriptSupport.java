/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.script;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.vtype.pv.PV;

/** JavaScript support
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
class JavaScriptSupport
{
    private final ExecutorService executor;
    private final ScriptEngine engine;
    private final Bindings bindings;

    /** Create executor for java scripts */
    public JavaScriptSupport() throws Exception
    {
        executor = Executors.newSingleThreadExecutor(ScriptSupport.POOL);
        engine = Objects.requireNonNull(new ScriptEngineManager().getEngineByName("nashorn"));
        bindings = engine.createBindings();
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
        final CompiledScript code = ((Compilable) engine).compile(new InputStreamReader(stream));
        return new JavaScript(this, name, code);
    }

    /** Request that a script gets executed
     *  @param script {@link JavaScript}
     *  @param widget Widget that requests execution
     *  @param pvs PVs that are available to the script
     *  @return
     */
    public Future<Object> submit(final JavaScript script, final Widget widget, final PV[] pvs)
    {
        // TODO See comments in JythonScriptSupport
        return executor.submit(() ->
        {
            // System.out.println("Execute on " + Thread.currentThread().getName());
            try
            {
                bindings.put("widget", widget);
                bindings.put("pvs", pvs);
                script.getCode().eval(bindings);
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

    /** Release resources */
    public void close()
    {
        executor.shutdown();
    }
}
