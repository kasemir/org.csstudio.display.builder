/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.trends.databrowser3.bobwidget;

import static org.csstudio.display.builder.model.widgets.plots.PlotWidgetProperties.propToolbar;
import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.runtime.Messages;
import org.csstudio.display.builder.runtime.RuntimeAction;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.display.builder.runtime.pv.PVFactory;
import org.csstudio.display.builder.runtime.pv.RuntimePV;
import org.csstudio.display.builder.runtime.pv.RuntimePVListener;
import org.diirt.vtype.VType;

/** Runtime for the DataBrowserWidget
 *
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class DataBrowserWidgetRuntime  extends WidgetRuntime<DataBrowserWidget>
{

    private class ToggleToolbarAction extends RuntimeAction
    {
        private final Widget widget;

        public ToggleToolbarAction(final Widget widget)
        {
            super(Messages.Toolbar_Hide,
                    "platform:/plugin/org.csstudio.javafx.rtplot/icons/toolbar.png");
            this.widget = widget;
            updateDescription();
        }

        private void updateDescription()
        {
            description = widget.getPropertyValue(propToolbar)
                    ? Messages.Toolbar_Hide
                            : Messages.Toolbar_Show;
        }

        @Override
        public void run()
        {
            widget.setPropertyValue(propToolbar, ! widget.getPropertyValue(propToolbar));
            updateDescription();
        }
    }

    private final List<RuntimeAction> runtime_actions = new ArrayList<>(1);

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
    public void initialize(final DataBrowserWidget widget)
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

        //        for (TraceWidgetProperty trace : widget.propTraces().getValue())
        //        {
        //            bind(trace.traceXPV(), trace.traceXValue());
        //            bind(trace.traceYPV(), trace.traceYValue());
        //            bind(trace.traceErrorPV(), trace.traceErrorValue());
        //        }
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
