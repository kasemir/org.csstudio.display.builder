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
import java.util.List;
import java.util.logging.Level;

import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.plots.PlotWidgetProperties.TraceWidgetProperty;
import org.csstudio.display.builder.model.widgets.plots.XYPlotWidget;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.display.builder.runtime.pv.PVFactory;
import org.csstudio.display.builder.runtime.pv.RuntimePV;
import org.csstudio.display.builder.runtime.pv.RuntimePVListener;
import org.diirt.vtype.VType;

/** Runtime for the XYPlotWidget
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class XYPlotWidgetRuntime  extends WidgetRuntime<XYPlotWidget>
{
    private static class Subscription
    {
        final RuntimePV pv;
        final RuntimePVListener listener;
        Subscription(final RuntimePV pv, final RuntimePVListener listener)
        {
            this.pv = pv;
            this.listener = listener;
        }
    }
    private final List<Subscription> subscriptions = new ArrayList<>();

    @Override
    public void start() throws Exception
    {
        super.start();

        for (TraceWidgetProperty trace : widget.behaviorTraces().getValue())
        {
            bind(trace.traceXPV(), trace.traceXValue());
            bind(trace.traceYPV(), trace.traceYValue());
        }
    }

    private void bind(final WidgetProperty<String> name, final WidgetProperty<VType> value) throws Exception
    {
        final String pv_name = name.getValue();
        if (pv_name.isEmpty())
            return;
        logger.log(Level.FINER,  "Connecting {0} to {1}", new Object[] { widget, pv_name });
        final RuntimePV pv = PVFactory.getPV(pv_name);
        final RuntimePVListener listener = new PropertyUpdater(value);
        pv.addListener(listener);
        subscriptions.add(new Subscription(pv, listener));
        addPV(pv);
    }

    @Override
    public void stop()
    {
        for (Subscription sub : subscriptions)
        {
            sub.pv.removeListener(sub.listener);
            PVFactory.releasePV(sub.pv);
            removePV(sub.pv);
        }
        super.stop();
    }
}
