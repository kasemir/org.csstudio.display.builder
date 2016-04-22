/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.script;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.macros.MacroValueProvider;
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
public class RuntimeScriptHandler implements RuntimePVListener
{
    private final Widget widget;
    private final List<ScriptPV> infos;
    private final Script script;

    /** 'pvs' is aligned with 'infos', i.e. pv[i] goes with infos.get(i) */
    private final RuntimePV[] pvs;

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
            path = ModelResourceUtil.getLocation(resolved);
        }
        else
        {   // Use script text that was embedded in display
            stream = new ByteArrayInputStream(script_info.getText().getBytes());
            path = ModelResourceUtil.getLocation(parent_display);
        }
        return scripting.compile(path, script_name, stream);
    }

    /** @param widget Widget on which the script is invoked
     *  @param script_info Script to handle
     *  @throws Exception on error
     */
    public RuntimeScriptHandler(final Widget widget, final ScriptInfo script_info) throws Exception
    {
        this.widget = widget;
        this.infos = script_info.getPVs();

        final MacroValueProvider macros = widget.getEffectiveMacros();
        script = compileScript(widget, macros, script_info);

        // Create PVs
        final WidgetRuntime<Widget> runtime = WidgetRuntime.ofWidget(widget);
        pvs = new RuntimePV[infos.size()];
        for (int i=0; i<pvs.length; ++i)
        {
            final String pv_name = MacroHandler.replace(macros, infos.get(i).getName());
            pvs[i] = PVFactory.getPV(pv_name);
            runtime.addPV(pvs[i]);
        }
        // Subscribe to trigger PVs
        for (int i=0; i<pvs.length; ++i)
            if (infos.get(i).isTrigger())
                pvs[i].addListener(this);
    }

    /** Must be invoked to dispose PVs */
    public void shutdown()
    {
        final WidgetRuntime<Widget> runtime = WidgetRuntime.ofWidget(widget);
        for (int i=0; i<pvs.length; ++i)
        {
            if (infos.get(i).isTrigger())
                pvs[i].removeListener(this);
            runtime.removePV(pvs[i]);
            PVFactory.releasePV(pvs[i]);
        }
    }

    @Override
    public void valueChanged(final RuntimePV pv, final VType value)
    {
        // Request execution of script
        script.submit(widget, pvs);
    }
}