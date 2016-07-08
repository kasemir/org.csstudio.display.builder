package org.csstudio.display.builder.runtime.script.internal;

import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.runtime.pv.RuntimePV;

/**
 * Python script support. Unlike Jython, executes Python scripts through a
 * gateway server, in a separate process and using the Python interpreter,
 * libraries, etc. installed on the system.
 * 
 * Based on {@link JavaScriptSupport} and {@link JythonScriptSupport} by Kay
 * Kasemir.
 * 
 * @author Amanda Carpenter
 *
 */
public class PythonScriptSupport
{
    ScriptSupport support;

    // See comments on queued_scripts in JythonScriptSupport
    private final Set<PythonScript> queued_scripts = Collections.newSetFromMap(new ConcurrentHashMap<PythonScript, Boolean>());

    public PythonScriptSupport(final ScriptSupport support)
    {
        this.support = support;
    }

    /**
     * Request that a script gets executed
     * 
     * @param script {@link PythonScript}
     * @param widget Widget that requests execution
     * @param pvs PVs that are available to the script
     * @return Future for script that was just submitted
     */
    @SuppressWarnings("nls")
    public Future<Object> submit(PythonScript script, Widget widget, RuntimePV[] pvs)
    {
        // Skip script that's already in the queue.
        // Check-then-set, no atomic submit-unless-queued logic.
        // Might still add some scripts twice, but good enough.
        if (queued_scripts.contains(script))
        {
            logger.log(Level.FINE, "Skipping script {0}, already queued for execution", script);
            return null;
        }
        queued_scripts.add(script);

        return support.submit(() ->
        {
            // Script may be queued again
            queued_scripts.remove(script);
            try
            {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("widget", widget);
                map.put("pv", pvs);
                PythonGatewaySupport.run(map, script.getName());
            } catch (final Throwable ex)
            {
                logger.log(Level.WARNING, "Execution of '" + script + "' failed for " + widget, ex);
            }
            return null;
        });
    }

    /**
     * Obtain a Python script object which can be submitted for execution. This
     * naming scheme is consistent with {@link JythonScriptSupport} and
     * {@link JavaScriptSupport}.
     * 
     * @param path Path to script file
     * @param name Name of script file
     * @return {@link Script}
     * @throws Exception on error
     */
    //Since compile is only called after checking the type of the script,
    //and for ScriptInfo.isPython() to return true, the file must exist
    //and be readable, it seems redundant to check the file exists.
    PythonScript compile(String name) throws Exception
    {
        if (new File(name).exists())
            return new PythonScript(this, name);
        throw new Exception("Python script file " + name + " does not exist."); //$NON-NLS-1$
    }
}
