/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets.plots;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.plots.PlotWidgetPointType;
import org.csstudio.display.builder.model.widgets.plots.PlotWidgetProperties.AxisWidgetProperty;
import org.csstudio.display.builder.model.widgets.plots.PlotWidgetProperties.TraceWidgetProperty;
import org.csstudio.display.builder.model.widgets.plots.PlotWidgetProperties.YAxisWidgetProperty;
import org.csstudio.display.builder.model.widgets.plots.PlotWidgetTraceType;
import org.csstudio.display.builder.model.widgets.plots.XYPlotWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.display.builder.representation.javafx.widgets.RegionBaseRepresentation;
import org.csstudio.javafx.rtplot.Axis;
import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.RTValuePlot;
import org.csstudio.javafx.rtplot.Trace;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.javafx.rtplot.YAxis;
import org.diirt.util.array.ArrayDouble;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VType;

import javafx.scene.layout.Pane;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class XYPlotRepresentation extends RegionBaseRepresentation<Pane, XYPlotWidget>
{
    private final DirtyFlag dirty_position = new DirtyFlag();
    private final DirtyFlag dirty_config = new DirtyFlag();

    private final UntypedWidgetPropertyListener config_listener = (WidgetProperty<?> property, Object old_value, Object new_value) ->
    {
        dirty_config.mark();
        toolkit.scheduleUpdate(this);
    };

    /** Plot */
    private RTValuePlot plot;

    /** Handler for one trace of the plot
     *
     *  <p>Updates the plot when the configuration of a trace
     *  or the associated X or Y value in the model changes.
     */
    private class TraceHandler
    {
        private final TraceWidgetProperty model_trace;
        private final XYVTypeDataProvider data = new XYVTypeDataProvider();
        private final UntypedWidgetPropertyListener trace_listener = this::traceChanged,
                                                    value_listener = this::valueChanged;
        private final Trace<Double> trace;

        TraceHandler(final TraceWidgetProperty model_trace)
        {
            this.model_trace = model_trace;

            trace = plot.addTrace(getDisplayName(), "", data,
                                  JFXUtil.convert(model_trace.traceColor().getValue()),
                                  map(model_trace.traceType().getValue()),
                                  model_trace.traceWidth().getValue(),
                                  map(model_trace.tracePointType().getValue()),
                                  model_trace.tracePointSize().getValue(),
                                  model_trace.traceYAxis().getValue());

            model_trace.traceName().addUntypedPropertyListener(trace_listener);
            // Not tracking X and Error PVs. Only matter to runtime.
            // Y PV name is shown in legend, so track that for the editor.
            model_trace.traceYPV().addUntypedPropertyListener(trace_listener);
            model_trace.traceYAxis().addUntypedPropertyListener(trace_listener);
            model_trace.traceType().addUntypedPropertyListener(trace_listener);
            model_trace.traceColor().addUntypedPropertyListener(trace_listener);
            model_trace.traceWidth().addUntypedPropertyListener(trace_listener);
            model_trace.tracePointType().addUntypedPropertyListener(trace_listener);
            model_trace.tracePointSize().addUntypedPropertyListener(trace_listener);
            model_trace.traceXValue().addUntypedPropertyListener(value_listener);
            model_trace.traceYValue().addUntypedPropertyListener(value_listener);
            model_trace.traceErrorValue().addUntypedPropertyListener(value_listener);
        }

        private TraceType map(final PlotWidgetTraceType value)
        {
            // AREA* types create just a line if the input data is
            // a plain array, but will also handle VStatistics
            switch (value)
            {
            case NONE:          return TraceType.NONE;
            case STEP:          return TraceType.AREA;
            case ERRORBAR:      return TraceType.ERROR_BARS;
            case LINE_ERRORBAR: return TraceType.LINES_ERROR_BARS;
            case LINE:
            default:            return TraceType.AREA_DIRECT;
            }
        }

        private PointType map(final PlotWidgetPointType value)
        {   // For now the ordinals match,
            // only different types to keep the Model separate from the Representation
            return PointType.fromOrdinal(value.ordinal());
        }

        private String getDisplayName()
        {
            String name = model_trace.traceName().getValue();
            if (name.isEmpty())
                return model_trace.traceYPV().getValue();
            return name;
        }

        private void traceChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
        {
            trace.setName(getDisplayName());
            trace.setType(map(model_trace.traceType().getValue()));
            trace.setColor(JFXUtil.convert(model_trace.traceColor().getValue()));
            trace.setWidth(model_trace.traceWidth().getValue());
            trace.setPointType(map(model_trace.tracePointType().getValue()));
            trace.setPointSize(model_trace.tracePointSize().getValue());

            final int desired = model_trace.traceYAxis().getValue();
            if (desired != trace.getYAxis())
                plot.moveTrace(trace, desired);
            plot.requestLayout();
        };

        private void valueChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
        {
            try
            {
                final VType y_value = model_trace.traceYValue().getValue();
                final ListNumber y_data = (y_value instanceof VNumberArray) ? ((VNumberArray)y_value).getData() : null;

                final VType x_value = model_trace.traceXValue().getValue();
                final ListNumber x_data = (x_value instanceof VNumberArray) ? ((VNumberArray)x_value).getData() : null;

                if (y_data == null)
                {   // Clear data
                    data.setData(XYVTypeDataProvider.EMPTY, XYVTypeDataProvider.EMPTY, XYVTypeDataProvider.EMPTY);
                    plot.requestUpdate();
                    return;
                }

                trace.setUnits(((VNumberArray)y_value).getUnits());

                final VType error_value = model_trace.traceErrorValue().getValue();
                final ListNumber error;
                if (error_value == null)
                    error = XYVTypeDataProvider.EMPTY;
                else if (error_value instanceof VNumberArray)
                    error = ((VNumberArray)error_value).getData();
                else
                    error = new ArrayDouble(VTypeUtil.getValueNumber(error_value).doubleValue());

                data.setData(x_data, y_data, error);
                plot.requestUpdate();
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "XYGraph data error", ex);
            }
        }

        void dispose()
        {
            model_trace.traceName().removePropertyListener(trace_listener);
            model_trace.traceYPV().removePropertyListener(trace_listener);
            model_trace.traceYAxis().removePropertyListener(trace_listener);
            model_trace.traceType().removePropertyListener(trace_listener);
            model_trace.traceColor().removePropertyListener(trace_listener);
            model_trace.traceWidth().removePropertyListener(trace_listener);

            model_trace.tracePointType().removePropertyListener(trace_listener);
            model_trace.tracePointSize().removePropertyListener(trace_listener);
            model_trace.traceXValue().removePropertyListener(value_listener);
            model_trace.traceYValue().removePropertyListener(value_listener);
            model_trace.traceErrorValue().removePropertyListener(value_listener);
            plot.removeTrace(trace);
        }
    };

    private final List<TraceHandler> trace_handlers = new CopyOnWriteArrayList<>();


    @Override
    public Pane createJFXNode() throws Exception
    {
        // Plot is only active in runtime mode, not edit mode
        plot = new RTValuePlot(! toolkit.isEditMode());
        plot.showToolbar(false);
        plot.showCrosshair(false);
        return plot;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();

        model_widget.propBackground().addUntypedPropertyListener(config_listener);
        model_widget.propTitle().addUntypedPropertyListener(config_listener);
        model_widget.propTitleFont().addUntypedPropertyListener(config_listener);
        model_widget.propToolbar().addUntypedPropertyListener(config_listener);
        model_widget.propLegend().addUntypedPropertyListener(config_listener);

        trackAxisChanges(model_widget.propXAxis());

        // Track initial Y axis
        final List<YAxisWidgetProperty> y_axes = model_widget.propYAxes().getValue();
        trackAxisChanges(y_axes.get(0));
        // Create additional Y axes from model
        if (y_axes.size() > 1)
            yAxesChanged(model_widget.propYAxes(), null, y_axes.subList(1, y_axes.size()));
        // Track added/remove Y axes
        model_widget.propYAxes().addPropertyListener(this::yAxesChanged);

        final UntypedWidgetPropertyListener position_listener = this::positionChanged;
        model_widget.propWidth().addUntypedPropertyListener(position_listener);
        model_widget.propHeight().addUntypedPropertyListener(position_listener);

        tracesChanged(model_widget.propTraces(), null, model_widget.propTraces().getValue());
        model_widget.propTraces().addPropertyListener(this::tracesChanged);
    }

    /** Listen to changed axis properties
     *  @param axis X or Y axis
     */
    private void trackAxisChanges(final AxisWidgetProperty axis)
    {
        axis.title().addUntypedPropertyListener(config_listener);
        axis.minimum().addUntypedPropertyListener(config_listener);
        axis.maximum().addUntypedPropertyListener(config_listener);
        axis.autoscale().addUntypedPropertyListener(config_listener);
        axis.titleFont().addUntypedPropertyListener(config_listener);
        axis.scaleFont().addUntypedPropertyListener(config_listener);
        if (axis instanceof YAxisWidgetProperty)
            ((YAxisWidgetProperty)axis).logscale().addUntypedPropertyListener(config_listener);
    }

    /** Ignore changed axis properties
     *  @param axis X or Y axis
     */
    private void ignoreAxisChanges(final AxisWidgetProperty axis)
    {
        axis.title().removePropertyListener(config_listener);
        axis.minimum().removePropertyListener(config_listener);
        axis.maximum().removePropertyListener(config_listener);
        axis.autoscale().removePropertyListener(config_listener);
        axis.titleFont().removePropertyListener(config_listener);
        axis.scaleFont().removePropertyListener(config_listener);
        if (axis instanceof YAxisWidgetProperty)
            ((YAxisWidgetProperty)axis).logscale().removePropertyListener(config_listener);
    }

    private void yAxesChanged(final WidgetProperty<List<YAxisWidgetProperty>> property,
                              final List<YAxisWidgetProperty> removed, final List<YAxisWidgetProperty> added)
    {
        // Remove axis
        if (removed != null)
        {   // Notification holds the one removed axis, which was the last one
            final AxisWidgetProperty axis = removed.get(0);
            final int index = plot.getYAxes().size()-1;
            ignoreAxisChanges(axis);
            plot.removeYAxis(index);
        }

        // Add missing axes
        // Notification will hold the one added axis,
        // but initial call from registerListeners() will hold all axes to add
        if (added != null)
            for (AxisWidgetProperty axis : added)
            {
                plot.addYAxis(axis.title().getValue());
                trackAxisChanges(axis);
            }
        // Update axis detail: range, ..
        config_listener.propertyChanged(property, removed, added);
    }

    private void positionChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_position.mark();
        toolkit.scheduleUpdate(this);
    }

    private void tracesChanged(final WidgetProperty<List<TraceWidgetProperty>> property,
                               final List<TraceWidgetProperty> removed, final List<TraceWidgetProperty> added)
    {
        final List<TraceWidgetProperty> model_traces = property.getValue();
        int count = trace_handlers.size();
        // Remove extra traces
        while (count > model_traces.size())
            trace_handlers.remove(--count).dispose();
        // Add missing traces
        while (count < model_traces.size())
            trace_handlers.add(new TraceHandler(model_traces.get(count++)));
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_config.checkAndClear())
            updateConfig();
        if (dirty_position.checkAndClear())
        {
            final int w = model_widget.propWidth().getValue();
            final int h = model_widget.propHeight().getValue();
            plot.setPrefWidth(w);
            plot.setPrefHeight(h);
        }
        plot.requestUpdate();
    }

    private void updateConfig()
    {
        plot.setBackground(JFXUtil.convert(model_widget.propBackground().getValue()));
        plot.setTitleFont(JFXUtil.convert(model_widget.propTitleFont().getValue()));
        plot.setTitle(model_widget.propTitle().getValue());

        plot.showToolbar(model_widget.propToolbar().getValue());

        // Show trace names either in legend or on axis
        final boolean legend = model_widget.propLegend().getValue();
        plot.showLegend(legend);
        for (YAxis<Double> axis : plot.getYAxes())
            axis.useTraceNames(!legend);

        // Update X Axis
        updateAxisConfig(plot.getXAxis(), model_widget.propXAxis());
        // Use X axis font for legend
        plot.setLegendFont(JFXUtil.convert(model_widget.propXAxis().titleFont().getValue()));

        // Update Y Axes
        final List<YAxisWidgetProperty> model_y = model_widget.propYAxes().getValue();
        if (plot.getYAxes().size() != model_y.size())
        {
            logger.log(Level.WARNING, "Plot has " + plot.getYAxes().size() + " while model has " + model_y.size() + " Y axes");
            return;
        }
        for (int i=0;  i<model_y.size();  ++i)
            updateYAxisConfig( plot.getYAxes().get(i), model_y.get(i));
    }

    private void updateYAxisConfig(final YAxis<Double> plot_axis, final YAxisWidgetProperty model_axis)
    {
        updateAxisConfig(plot_axis, model_axis);
        plot_axis.setLogarithmic(model_axis.logscale().getValue());
    }

    private void updateAxisConfig(final Axis<Double> plot_axis, final AxisWidgetProperty model_axis)
    {
        plot_axis.setName(model_axis.title().getValue());
        plot_axis.setValueRange(model_axis.minimum().getValue(), model_axis.maximum().getValue());
        plot_axis.setAutoscale(model_axis.autoscale().getValue());
        plot_axis.setLabelFont(JFXUtil.convert(model_axis.titleFont().getValue()));
        plot_axis.setScaleFont(JFXUtil.convert(model_axis.scaleFont().getValue()));
    }

    @Override
    public void dispose()
    {
        plot.dispose();
        super.dispose();
    }
}
