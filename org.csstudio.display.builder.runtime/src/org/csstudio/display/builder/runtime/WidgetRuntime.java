/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorScripts;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.properties.ScriptInfo;
import org.csstudio.display.builder.runtime.script.RuntimeScriptHandler;
import org.csstudio.vtype.pv.PV;
import org.csstudio.vtype.pv.PVListener;
import org.csstudio.vtype.pv.PVPool;
import org.epics.vtype.VType;

/** Runtime for a Widget.
 *
 *  <p>Connects to scripts and PVs.
 *
 *  @author Kay Kasemir
 *  @param <MW> Model widget
 */
@SuppressWarnings("nls")
public class WidgetRuntime<MW extends Widget>
{
    final private static Logger logger = Logger.getLogger(WidgetRuntime.class.getName());

    /** The widget handled by this runtime */
    protected final MW widget;

    /** Primary widget PV for behaviorPVName property */
    private volatile Optional<PV> primary_pv = Optional.empty();

    /** Listener for <code>primary_pv</code> */
    private PrimaryPVListener primary_pv_listener;

    /** Handlers for widget's behaviorScripts property */
    private List<RuntimeScriptHandler> script_handlers = new CopyOnWriteArrayList<>();

    /** PVListener that updates 'value' property with received VType */
    private class PrimaryPVListener implements PVListener
    {
        @Override
        public void valueChanged(final PV pv, final VType value)
        {
            widget.setPropertyValue(runtimeValue, value);
        }

        @Override
        public void permissionsChanged(final PV pv, final boolean readonly)
        {
            // NOP
        }

        @Override
        public void disconnected(final PV pv)
        {
            widget.setPropertyValue(runtimeValue, null);
        }
    };

    /** Construct runtime
     *  @param widget Model widget
     */
    public WidgetRuntime(final MW widget)
    {
        this.widget = widget;
        widget.setUserData(Widget.USER_DATA_RUNTIME, this);
    }

    /** Start: Connect to PVs, start scripts
     *  @throws Exception on error
     */
    public void start() throws Exception
    {
        // Update "value" property from primary PV, if defined
        if (widget.hasProperty(behaviorPVName) &&  widget.hasProperty(runtimeValue))
        {
            final String pv_name = widget.getPropertyValue(behaviorPVName);
            if (! pv_name.isEmpty())
            {
                logger.log(Level.FINER, "Connecting {0} to {1}",  new Object[] { widget, pv_name });
                final PV pv = PVPool.getPV(pv_name);
                primary_pv_listener = new PrimaryPVListener();
                pv.addListener(primary_pv_listener);
                primary_pv = Optional.of(pv);
            }
        }

        // Start scripts in pool because Jython setup is expensive
        ForkJoinPool.commonPool().execute(this::startScripts);
    }

    /** Start Scripts */
    private void startScripts()
    {
        for (ScriptInfo script_info : widget.getPropertyValue(behaviorScripts))
        {
            try
            {
                script_handlers.add(new RuntimeScriptHandler(widget, script_info));
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING,
                    "Widget " + widget.getName() + " script " + script_info.getFile() + " failed to initialize", ex);
            }
        }
    }

    /** Stop: Disconnect PVs, ... */
    public void stop()
    {
        final PV pv = primary_pv.orElse(null);
        primary_pv = Optional.empty();
        if (pv != null)
        {
            pv.removeListener(primary_pv_listener);
            PVPool.releasePV(pv);
        }

        for (RuntimeScriptHandler handler : script_handlers)
            handler.shutdown();
    }
}

