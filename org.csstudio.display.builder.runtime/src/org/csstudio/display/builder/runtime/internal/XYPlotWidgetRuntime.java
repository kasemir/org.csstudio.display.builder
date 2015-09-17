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
import org.epics.vtype.VType;

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

    public XYPlotWidgetRuntime(final XYPlotWidget widget)
    {
        super(widget);
    }

    @Override
    public void start() throws Exception
    {
        super.start();

        // TODO 'getElement(3)' is too fragile as structure is extended. Lookup by element name?
        WidgetProperty<String> name = widget.behaviorTrace().getElement(0);
        WidgetProperty<VType> value = widget.behaviorTrace().getElement(3);
        bind(name, value);

        name = widget.behaviorTrace().getElement(1);
        value = widget.behaviorTrace().getElement(4);
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
