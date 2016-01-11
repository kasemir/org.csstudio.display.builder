/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.script;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.ThreadFactory;

import org.csstudio.display.builder.model.properties.ScriptInfo;
import org.csstudio.display.builder.model.util.NamedDaemonPool;

/** Script (Jython, Javascript) Support
 *
 *  <p>Each instance of the support module maintains one interpreter instance.
 *  Script files are parsed/compiled (possibly slow) and can then be executed
 *  multiple times (hopefully faster).
 *
 *  <p>Scripts are executed on one thread per support/interpreter.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ScriptSupport
{
    /** Pool for script related executors */
    static final ThreadFactory POOL = new NamedDaemonPool("ScriptSupport");

    private final JythonScriptSupport jython;
    private final JavaScriptSupport javascript;

    public ScriptSupport() throws Exception
    {
        jython = new JythonScriptSupport();
        javascript = new JavaScriptSupport();
    }

    /** Parse and compile script file
     *
     *  @param path Full path to script file
     *  @return {@link Script}
     *  @throws Exception on error
     */
    public Script compile(final String path) throws Exception
    {
        return compile(path, new FileInputStream(path));
    }

    /** Parse and compile script file
     *
     *  @param path Name of script (file name, URL)
     *  @param stream Stream for the script content
     *  @return {@link Script}
     *  @throws Exception on error
     */
    public Script compile(final String path, final InputStream stream) throws Exception
    {
        if (ScriptInfo.isJython(path))
            return jython.compile(path, stream);
        else if (ScriptInfo.isJavaScript(path))
            return javascript.compile(path, stream);
        throw new Exception("Cannot compile '" + path + "'");
    }

    /** Release resources (interpreter, ...) */
    public void close()
    {
        javascript.close();
        jython.close();
    }
}
