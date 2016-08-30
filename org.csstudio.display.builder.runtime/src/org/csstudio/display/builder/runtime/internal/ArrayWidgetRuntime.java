package org.csstudio.display.builder.runtime.internal;

import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.widgets.ArrayWidget;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.display.builder.runtime.pv.ArrayPVDispatcher;
import org.csstudio.display.builder.runtime.pv.RuntimePV;

@SuppressWarnings("nls")
public class ArrayWidgetRuntime extends WidgetRuntime<ArrayWidget>
{
    private ArrayPVDispatcher dispatcher;
    private CopyOnWriteArrayList<String> pvnames = new CopyOnWriteArrayList<String>();
    private String pvid;

    private final ArrayPVDispatcher.Listener assign_pv_names = new ArrayPVDispatcher.Listener()
    {
        @Override
        public void arrayChanged(List<RuntimePV> element_pvs)
        {
            pvnames.clear();
            for (RuntimePV pv : element_pvs)
                pvnames.add(pv.getName());
            setPVNames(0, new ArrayList<Widget>(widget.runtimeChildren().getValue()));
        }
    };

    private final WidgetPropertyListener<List<Widget>> children_listener = (prop, removed, added) ->
    {
        if (added != null)
            setPVNames(this.widget.runtimeChildren().getValue().size() - added.size(), added);
        else //removed != null
            for (Widget widget : removed)
            {
                final Optional<WidgetProperty<Object>> pvname = widget.checkProperty("pv_name");
                if (!pvname.isPresent())
                    return;
                try
                {
                    pvname.get().setValueFromObject(pvname.get().getDefaultValue());
                }
                catch (Exception ex)
                {
                    logger.log(Level.WARNING, "Unable to clear pv name of " + widget, ex);
                }
            }
    };

    @Override
    public void initialize(final ArrayWidget widget)
    {
        super.initialize(widget);
        pvid = "elem" + widget.getID() + "_";
    }

    @Override
    public void start() throws Exception
    {
        super.start();
        RuntimePV pv = getPrimaryPV().orElse(null);
        if (pv != null)
            dispatcher = new ArrayPVDispatcher(pv, pvid, assign_pv_names);
        widget.runtimeChildren().addPropertyListener(children_listener);
    }

    @Override
    public void stop()
    {
        widget.runtimeChildren().removePropertyListener(children_listener);
        if (dispatcher != null)
            dispatcher.close();
        super.stop();
    }

    private void setPVNames(int i, List<Widget> added)
    {
        for (Widget widget : added)
            if (i < pvnames.size())
            {
                setPVName(widget, pvnames.get(i));
                i++;
            }
    }

    private void setPVName(Widget widget, String name)
    {
        final Optional<WidgetProperty<Object>> pvname = widget.checkProperty("pv_name");
        if (!pvname.isPresent())
            return;
        try
        {
            pvname.get().setValueFromObject(name);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Unable to set pv name of " + widget, ex);
        }
    }
}
