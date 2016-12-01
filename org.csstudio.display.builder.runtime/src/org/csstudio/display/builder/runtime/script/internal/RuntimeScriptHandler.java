/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.script.internal;

import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.macros.MacroValueProvider;
import org.csstudio.display.builder.model.properties.RuleInfo;
import org.csstudio.display.builder.model.properties.RuleToScript;
import org.csstudio.display.builder.model.properties.ScriptInfo;
import org.csstudio.display.builder.model.properties.ScriptPV;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.display.builder.runtime.pv.PVFactory;
import org.csstudio.display.builder.runtime.pv.RuntimePV;
import org.csstudio.display.builder.runtime.pv.RuntimePVListener;
import org.diirt.vtype.VType;

/** Handler for one script of a widget.
 *
 *  <p>Compiles script, connects to PVs,
 *  invokes script when trigger PVs change.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RuntimeScriptHandler implements RuntimePVListener
{
    private final Widget widget;
    private final List<ScriptPV> infos;
    private final Script script;

    /** 'pvs' is aligned with 'infos', i.e. pvs[i] goes with infos.get(i) */
    private final RuntimePV[] pvs;

    /** Is there a subscription to pvs[i]? */
    private final AtomicBoolean[] subscribed;

    /** Has script executed once? */
    private final AtomicBoolean executed_once = new AtomicBoolean();

    /** Helper to compile script
     *
     *  <p>Resolves script path based on macros and display,
     *  can be invoked by other code.
     *
     *  @param widget Widget on which the script is invoked
     *  @param macros
     *  @param script_info Script to compile
     *  @return Compiled script
     *  @throws Exception on error
     */
    public static Script compileScript(final Widget widget, final MacroValueProvider macros,
            final ScriptInfo script_info) throws Exception
    {
        // Compile script
        final String script_name = MacroHandler.replace(macros, script_info.getPath());
        final ScriptSupport scripting = RuntimeUtil.getScriptSupport(widget);

        final InputStream stream;
        final DisplayModel model = widget.getDisplayModel();
        final String parent_display = model.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
        final String path;
        if (script_info.getText() == null)
        {   // Load external script
            final String resolved = ModelResourceUtil.resolveResource(parent_display, script_name);
            stream = ModelResourceUtil.openResourceStream(resolved);
            path = ModelResourceUtil.getDirectory(ModelResourceUtil.getLocalPath(resolved));
        }
        else
        {   // Use script text that was embedded in display
            stream = new ByteArrayInputStream(script_info.getText().getBytes());
            path = ModelResourceUtil.getDirectory(ModelResourceUtil.getLocalPath(parent_display));
        }
        return scripting.compile(path, script_name, stream);
    }


    /** Helper to compile rules script
     *
     *  <p>Gets text of script from rules utility
     *
     *  @param widget Widget on which the rule is invoked
     *  @param rule_info Rule to compile
     *  @return Compiled script
     *  @throws Exception on error
     */
    public static Script compileScript(final Widget widget,
            final RuleInfo rule_info) throws Exception
    {
        // Compile script
        final ScriptSupport scripting = RuntimeUtil.getScriptSupport(widget);

        final String script = rule_info.getTextPy(widget);
        final InputStream stream = new ByteArrayInputStream(script.getBytes());
        String dummy_name = widget.getName() + ":" + rule_info.getName() + ".rule.py";

        logger.log(Level.FINER, () -> "Compiling rule script for " + dummy_name + "\n" + RuleToScript.addLineNumbers(script));
        try
        {
            return scripting.compile(null, dummy_name, stream);
        }
        catch (Exception e)
        {
            throw new Exception("Cannot compile rule: " + dummy_name + "\n" + RuleToScript.addLineNumbers(script), e);
        }
    }

    /** @param widget Widget on which the script is invoked
     *  @param script_info Script to handle
     *  @throws Exception on error
     */
    public RuntimeScriptHandler(final Widget widget, final ScriptInfo script_info) throws Exception
    {
        this(widget, compileScript(widget, widget.getMacrosOrProperties(), script_info), script_info.getPVs());
    }

    /** @param widget Widget on which the rule is invoked
     *  @param rule_info Rule to handle
     *  @throws Exception on error
     */
    public RuntimeScriptHandler(final Widget widget, final RuleInfo rule_info) throws Exception
    {
        this(widget, compileScript(widget, rule_info), rule_info.getPVs());
    }

    /** @param widget Widget on which the script is invoked
     *  @param script Script to execute
     *  @throws Exception on error
     */
    private RuntimeScriptHandler(final Widget widget, final Script script, final List<ScriptPV> infos) throws Exception
    {
        this.widget = widget;
        this.infos = infos;
        this.script = script;
        pvs = new RuntimePV[infos.size()];
        subscribed = new AtomicBoolean[infos.size()];
        createPVs();
    }

    private void createPVs() throws Exception
    {
        // Create PVs
        final WidgetRuntime<Widget> runtime = WidgetRuntime.ofWidget(widget);
        final MacroValueProvider macros = widget.getMacrosOrProperties();

        for (int i=0; i<pvs.length; ++i)
        {
            final String pv_name = MacroHandler.replace(macros, infos.get(i).getName());
            pvs[i] = PVFactory.getPV(pv_name);
            subscribed[i] = new AtomicBoolean(true);
            runtime.addPV(pvs[i]);
        }
        // Subscribe to all PVs.
        // Will later unsubscribe from non-trigger PVs
        for (int i=0; i<pvs.length; ++i)
            pvs[i].addListener(this);
    }

    /** Must be invoked to dispose PVs */
    public void shutdown()
    {
        final WidgetRuntime<Widget> runtime = WidgetRuntime.ofWidget(widget);
        for (int i=0; i<pvs.length; ++i)
        {
            if (subscribed[i].getAndSet(false))
                pvs[i].removeListener(this);
            runtime.removePV(pvs[i]);
            PVFactory.releasePV(pvs[i]);
        }
    }

    @Override
    public void valueChanged(final RuntimePV pv, final VType value)
    {
        if (logger.isLoggable(Level.FINE))
        {
            final StringBuilder buf = new StringBuilder();
            buf.append(script).append("\nTriggered by ").append(pv).append(" = ").append(value).append("\n");
            for (int i=0; i<pvs.length; ++i)
            {
                buf.append("pvs[").append(i).append("]: ").append(pvs[i]).append(" = ").append(pvs[i].read());
                if (infos.get(i).isTrigger())
                    buf.append(" - trigger!");
                buf.append("\n");
            }
            logger.fine(buf.toString());
        }
        // Skip script execution unless all PVs are connected
        for (RuntimePV p : pvs)
            if (p.read() == null)
                return;

        // Check if this PV is a 'trigger'
        // (linear search, expecting only a few PVs per script)
        int i;
        for (i=0; i<pvs.length; ++i)
            if (pv == pvs[i])
                break;
        if (i >= pvs.length)
            throw new IllegalStateException(script + " triggered by unknown PV " + pv);

        // If this is a trigger PV, execute the script.
        // If not trigger PV,
        // avoid further events from this PV
        // and execute script only if this would be the first run.
        // Scenario:
        // All the 'trigger' PVs connected, but since non-trigger PVs
        // were missing the script didn't execute.
        // Finally all connect, and in this case the last connecting
        // non-trigger PV actually triggers the (first) run.
        if (! infos.get(i).isTrigger())
        {
            if (subscribed[i].getAndSet(false))
                pvs[i].removeListener(this);
            if (executed_once.getAndSet(true))
                return;
        }

        // Request execution of script
        script.submit(widget, pvs);
    }
}