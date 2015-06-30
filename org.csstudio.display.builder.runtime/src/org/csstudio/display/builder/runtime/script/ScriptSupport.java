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
    private final JythonScriptSupport jython;
    // TODO Implement for JavaScript

    public ScriptSupport() throws Exception
    {
        jython = new JythonScriptSupport();
    }

    /** Parse and compile script file
     *
     *  @param filename Full path to script file
     *  @return {@link Script}
     *  @throws Exception on error
     */
    public Script compile(final String filename) throws Exception
    {
        return compile(filename, new FileInputStream(filename));
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
        if (name.endsWith(".py"))
            return jython.compile(name, stream);
        // TODO Handle ".js"
        throw new Exception("Cannot compile '" + name + "'");
    }

    /** Release resources (interpreter, ...) */
    public void close()
    {
        jython.close();
    }
}
