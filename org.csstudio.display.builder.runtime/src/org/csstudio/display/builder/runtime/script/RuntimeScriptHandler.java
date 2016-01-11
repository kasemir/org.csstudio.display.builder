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
import org.csstudio.vtype.pv.PV;
import org.csstudio.vtype.pv.PVListener;
import org.csstudio.vtype.pv.PVPool;
import org.diirt.vtype.VType;

/** Handler for one script of a widget.
 *
 *  <p>Compiles script, connects to PVs,
 *  invokes script when trigger PVs change.
 *
 *  @author Kay Kasemir
 */
public class RuntimeScriptHandler implements PVListener
{
    private final Widget widget;
    private final List<ScriptPV> infos;
    private final Script script;

    /** 'pvs' is aligned with 'infos', i.e. pv[i] goes with infos.get(i) */
    private final PV[] pvs;

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
        if (script_info.getText() == null)
        {   // Load external script
            final DisplayModel model = widget.getDisplayModel();
            final String parent_display = model.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
            final String resolved = ModelResourceUtil.resolveResource(parent_display, script_name);
            stream = ModelResourceUtil.openResourceStream(resolved);
        }
        else
        {   // Use script text that was embedded in display
            stream = new ByteArrayInputStream(script_info.getText().getBytes());
        }
        return scripting.compile(script_name, stream);
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
        pvs = new PV[infos.size()];
        for (int i=0; i<pvs.length; ++i)
        {
            final String pv_name = MacroHandler.replace(macros, infos.get(i).getName());
            pvs[i] = PVPool.getPV(pv_name);
        }
        // Subscribe to trigger PVs
        for (int i=0; i<pvs.length; ++i)
            if (infos.get(i).isTrigger())
                pvs[i].addListener(this);
    }

    /** Must be invoked to dispose PVs */
    public void shutdown()
    {
        for (int i=0; i<pvs.length; ++i)
        {
            if (infos.get(i).isTrigger())
                pvs[i].removeListener(this);
            PVPool.releasePV(pvs[i]);
        }
    }

    @Override
    public void valueChanged(final PV pv, final VType value)
    {
        // Request execution of script
        script.submit(widget, pvs);
    }

    @Override
    public void disconnected(final PV pv)
    {
        // NOP
    }

    @Override
    public void permissionsChanged(final PV pv, final boolean readonly)
    {
        // NOP
    }
}