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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.runtime.Messages;
import org.csstudio.display.builder.runtime.RuntimeAction;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.display.builder.runtime.pv.PVFactory;
import org.csstudio.display.builder.runtime.pv.RuntimePV;
import org.csstudio.javafx.rtplot.data.PlotDataItem;
import org.csstudio.trends.databrowser3.model.ModelItem;
import org.csstudio.trends.databrowser3.model.ModelListener;
import org.csstudio.trends.databrowser3.model.ModelListenerAdapter;
import org.csstudio.trends.databrowser3.model.TimeHelper;
import org.diirt.util.array.ArrayDouble;
import org.diirt.util.array.ListDouble;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueFactory;

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

    private class ModelSampleSelectionListener extends ModelListenerAdapter
    {
        private ListDouble convert(final List<Double> values)
        {
            final double[] array = new double[values.size()];
            for (int i=0; i<array.length; ++i)
                array[i] = values.get(i);
            return new ArrayDouble(array);
        }

        @Override
        public void selectedSamplesChanged()
        {
            // Create VTable value from selected samples
            final List<String> names = new ArrayList<>();
            final List<String> times = new ArrayList<>();
            final List<Double> values = new ArrayList<>();

            for (ModelItem item : widget.getDataBrowserModel().getItems())
            {
                names.add(item.getDisplayName());
                final Optional<PlotDataItem<Instant>> sample = item.getSelectedSample();
                if (sample.isPresent())
                {
                    times.add(TimeHelper.format(sample.get().getPosition()));
                    values.add(sample.get().getValue());
                }
                else
                {
                    times.add("-");
                    values.add(Double.NaN);
                }
            }
            final VType value = ValueFactory.newVTable(
                    Arrays.asList(String.class, String.class, double.class),
                    Arrays.asList("Trace", "Timestamp", "Value"),
                    Arrays.<Object>asList(names,times, convert(values)));
            try
            {
                selection_pv.write(value);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot update selection PV", ex);
            }
        }
    }

    private final List<RuntimeAction> runtime_actions = new ArrayList<>(1);

    private ModelListener db_model_listener;

    private RuntimePV selection_pv;

    @Override
    public void initialize(final DataBrowserWidget widget)
    {
        super.initialize(widget);
        runtime_actions.add(new ToggleToolbarAction(widget));
        runtime_actions.add(new OpenDataBrowserAction(widget));
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

        String pv_name = widget.propSelectionValuePVName().getValue();
        if (! pv_name.isEmpty())
            pv_name = MacroHandler.replace(widget.getEffectiveMacros(), pv_name);
        if (! pv_name.isEmpty())
        {
            final RuntimePV pv = PVFactory.getPV(pv_name);
            addPV(pv);
            selection_pv = pv;

            db_model_listener = new ModelSampleSelectionListener();
            widget.getDataBrowserModel().addListener(db_model_listener);
        }
    }

    @Override
    public void stop()
    {
        if (db_model_listener != null)
        {
            widget.getDataBrowserModel().removeListener(db_model_listener);
            db_model_listener = null;
        }

        final RuntimePV pv = selection_pv;
        if (pv != null)
        {
            removePV(pv);
            PVFactory.releasePV(pv);
            selection_pv = null;
        }

        super.stop();
    }
}
