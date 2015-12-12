/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.XYPlotWidget;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.vtype.pv.PV;
import org.csstudio.vtype.pv.PVListener;
import org.csstudio.vtype.pv.PVPool;
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
        final PV pv;
        final PVListener listener;
        Subscription(final PV pv, final PVListener listener)
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

        WidgetProperty<String> name = widget.behaviorTrace().traceX();
        WidgetProperty<VType> value = widget.behaviorTrace().xValue();
        bind(name, value);

        name = widget.behaviorTrace().traceY();
        value = widget.behaviorTrace().yValue();
        bind(name, value);
    }

    private void bind(final WidgetProperty<String> name, final WidgetProperty<VType> value) throws Exception
    {
        final String pv_name = name.getValue();
        if (pv_name.isEmpty())
            return;
        logger.log(Level.FINER,  "Connecting {0} to {1}", new Object[] { widget, pv_name });
        final PV pv = PVPool.getPV(pv_name);
        final PVListener listener = new PropertyUpdater(value);
        pv.addListener(listener);
        subscriptions.add(new Subscription(pv, listener));
    }

    @Override
    public void stop()
    {
        super.stop();

        for (Subscription sub : subscriptions)
        {
            sub.pv.removeListener(sub.listener);
            PVPool.releasePV(sub.pv);
        }
    }
}
