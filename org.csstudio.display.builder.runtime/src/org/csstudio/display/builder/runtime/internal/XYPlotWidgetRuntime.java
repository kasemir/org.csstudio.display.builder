/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.widgets.plots.PlotWidgetProperties.TraceWidgetProperty;
import org.csstudio.display.builder.model.widgets.plots.XYPlotWidget;
import org.csstudio.display.builder.runtime.RuntimeAction;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.display.builder.runtime.pv.PVFactory;
import org.csstudio.display.builder.runtime.pv.RuntimePV;
import org.csstudio.display.builder.runtime.pv.RuntimePVListener;
import org.diirt.vtype.VType;

/** Runtime for the XYPlotWidget
 *
 *  <p>Supports changing the PV names for a trace's X, Y, Error PV.
 *
 *  <p>Does not support adding or removing traces.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class XYPlotWidgetRuntime  extends WidgetRuntime<XYPlotWidget>
{
    private final List<RuntimeAction> runtime_actions = new ArrayList<>(1);

    /** Binds a trace's PV name property to the corresponding value property */
    private class PVBinding implements WidgetPropertyListener<String>
    {
        private final WidgetProperty<String> name;
        private final RuntimePVListener listener;
        private final AtomicReference<RuntimePV> pv_ref = new AtomicReference<>();

        public PVBinding(final WidgetProperty<String> name, final WidgetProperty<VType> value)
        {
            this.name = name;
            listener = new PropertyUpdater(value);
            connect();
            name.addPropertyListener(this);
        }

        @Override
        public void propertyChanged(final WidgetProperty<String> property,
                                    final String old_value, final String new_value)
        {
            // PV name changed: Disconnect existing PV
            disconnect();
            // and connect to new PV
            connect();
        }

        private void connect()
        {
            final String pv_name = name.getValue();
            if (pv_name.isEmpty())
                return;
            logger.log(Level.FINE,  "Connecting {0} {1}", new Object[] { widget, name });
            final RuntimePV pv;
            try
            {
                pv = PVFactory.getPV(pv_name);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot connect to PV " + pv_name, ex);
                return;
            }
            pv.addListener(listener);
            addPV(pv);
            pv_ref.set(pv);
        }

        private void disconnect()
        {
            final RuntimePV pv = pv_ref.getAndSet(null);
            if (pv == null)
                return;
            pv.removeListener(listener);
            PVFactory.releasePV(pv);
            removePV(pv);
        }

        public void dispose()
        {
            disconnect();
            name.removePropertyListener(this);
        }
    }
    private final List<PVBinding> bindings = new ArrayList<>();

    @Override
    public void initialize(final XYPlotWidget widget)
    {
        super.initialize(widget);
        runtime_actions.add(new ToggleToolbarAction(widget));
    }

    @Override
    public Collection<RuntimeAction> getRuntimeActions()
    {
        return runtime_actions;
    }

    @Override
    public void start() throws Exception
    {
        super.start();

        for (TraceWidgetProperty trace : widget.propTraces().getValue())
        {
            bindings.add(new PVBinding(trace.traceXPV(), trace.traceXValue()));
            bindings.add(new PVBinding(trace.traceYPV(), trace.traceYValue()));
            bindings.add(new PVBinding(trace.traceErrorPV(), trace.traceErrorValue()));
        }
    }

    @Override
    public void stop()
    {
        for (PVBinding binding : bindings)
            binding.dispose();
        super.stop();
    }
}
