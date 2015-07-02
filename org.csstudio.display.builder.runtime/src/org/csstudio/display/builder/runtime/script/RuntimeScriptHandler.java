/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.script;

import java.util.List;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.properties.ScriptInfo;
import org.csstudio.display.builder.model.properties.ScriptPV;
import org.csstudio.display.builder.runtime.ResourceUtil;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.csstudio.vtype.pv.PV;
import org.csstudio.vtype.pv.PVListener;
import org.csstudio.vtype.pv.PVPool;
import org.epics.vtype.VType;

/** Handler for one script of a widget.
 *
 *  <p>Compiles script, connects to PVs,
 *  invokes script on trigger PV changes.
 *
 *  @author Kay Kasemir
 */
public class RuntimeScriptHandler implements PVListener
{
    private final Widget widget;
    private final List<ScriptPV> infos;
    private final Script script;
    private final PV[] pvs;

    /** @param widget Widget on which the script is invoked
     *  @param script_info Script to handle
     *  @throws Exception on error
     */
    public RuntimeScriptHandler(final Widget widget, final ScriptInfo script_info) throws Exception
    {
        this.widget = widget;
        this.infos = script_info.getPVs();

        final Macros macros = widget.getEffectiveMacros();

        // Compile script
        final String script_name = MacroHandler.replace(macros, script_info.getFile());
        final ScriptSupport scripting = RuntimeUtil.getScriptSupport(widget);

        final DisplayModel model = RuntimeUtil.getDisplayModel(widget);
        final String parent_display = model.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
        final String resolved = ResourceUtil.resolveDisplay(parent_display, script_name);
        script = scripting.compile(script_name, ResourceUtil.openInputStream(resolved));

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

    private void invoke_script()
    {
        script.submit(widget, pvs);
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
        RuntimeUtil.getExecutor().execute(this::invoke_script);
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