/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.vtype.pv.PV;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.python.core.PyCode;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/** Jython script support
 *
 *  <p>To debug, see python.verbose which can also be set
 *  as VM property.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
class JythonScriptSupport extends BaseScriptSupport
{
    final static boolean initialized = init();

    private final PythonInterpreter python;

    /** Perform static, one-time initialization */
    private static boolean init()
    {
        try
        {
            final Properties pre_props = System.getProperties();
            final Properties props = new Properties();

            // Locate the jython plugin for 'home' to allow use of /Lib in there
            final String home = getPluginPath("org.python.jython", "/");
            if (home == null)
                throw new Exception("Cannot locate jython bundle");

            // Jython 2.7(b3) needs these to set sys.prefix and sys.executable.
            // If left undefined, initialization of Lib/site.py fails with
            // posixpath.py", line 394, in normpath AttributeError:
            // 'NoneType' object has no attribute 'startswith'
            props.setProperty("python.home", home);
            props.setProperty("python.executable", "None");

            // Disable cachedir to avoid creation of cachedir folder.
            // See http://www.jython.org/jythonbook/en/1.0/ModulesPackages.html#java-package-scanning
            // and http://wiki.python.org/jython/PackageScanning
            props.setProperty(PySystemState.PYTHON_CACHEDIR_SKIP, "true");

            // With python.home defined, there is no more
            // "ImportError: Cannot import site module and its dependencies: No module named site"
            // Skipping the site import still results in faster startup
            props.setProperty("python.import.site", "false");

            // Prevent: console: Failed to install '': java.nio.charset.UnsupportedCharsetException: cp0.
            props.setProperty("python.console.encoding", "UTF-8");

            // TODO Set search path to list of path elements separated by java.io.File.pathSeparator
            // This will replace entries found on JYTHONPATH
            // props.setProperty("python.path", search_path);

            // Options: error, warning, message (default), comment, debug
            // props.setProperty("python.verbose", "debug");
            // Options.verbose = Py.DEBUG;

            PythonInterpreter.initialize(pre_props, props, new String[0]);
            return true;
        }
        catch (Exception ex)
        {
            Logger.getLogger(JythonScriptSupport.class.getName())
                  .log(Level.SEVERE, "Once this worked OK, but now the Jython initialization failed. Don't you hate computers?", ex);
        }
        return false;
    }

    /** Locate a path inside a bundle.
     *
     *  <p>If the bundle is JAR-ed up, the {@link FileLocator} will
     *  return a location with "file:" and "..jar!/path".
     *  This method patches the location such that it can be used
     *  on the Jython path.
     *
     *  @param bundle_name Name of bundle
     *  @param path_in_bundle Path within bundle
     *  @return Location of that path within bundle, or <code>null</code> if not found or no bundle support
     *  @throws IOException on error
     */
    private static String getPluginPath(final String bundle_name, final String path_in_bundle) throws IOException
    {
        final Bundle bundle = Platform.getBundle(bundle_name);
        if (bundle == null)
            return null;
        final URL url = FileLocator.find(bundle, new Path(path_in_bundle), null);
        if (url == null)
            return null;
        String path = FileLocator.resolve(url).getPath();

        // Turn politically correct URL into path digestible by jython
        if (path.startsWith("file:/"))
           path = path.substring(5);
        path = path.replace(".jar!", ".jar");

        return path;
    }


    /** Create executor for jython scripts */
    public JythonScriptSupport() throws Exception
    {
        // Creating a PythonInterpreter is very slow.
        //
        // In addition, concurrent creation is not supported, resulting in
        //     Lib/site.py", line 571, in <module> ..
        //     Lib/sysconfig.py", line 159, in _subst_vars AttributeError: {'userbase'}
        // or  Lib/site.py", line 122, in removeduppaths java.util.ConcurrentModificationException
        //
        // Sync. on JythonScriptSupport to serialize the interpreter creation and avoid above errors.
        // Curiously, this speeds the interpreter creation up,
        // presumably because they're not concurrently trying to access the same resources?
        synchronized (JythonScriptSupport.class)
        {
             python = new PythonInterpreter(null, null);
        }
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
     *  @return Future for script that was just started
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
        return super.submit(() ->
        {
            // System.out.println("Executing " + script + " on " + Thread.currentThread().getName());
            try
            {
                // Executor is single-threaded.
                // OK to set 'widget' etc.
                // of the shared python interpreter
                // because only one script will execute at a time.
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
            // System.out.println("Finished " + script);
            return null;
        });
    }

    /** Release resources (interpreter, ...) */
    public void close()
    {
        python.close();
        super.close();
    }
}
