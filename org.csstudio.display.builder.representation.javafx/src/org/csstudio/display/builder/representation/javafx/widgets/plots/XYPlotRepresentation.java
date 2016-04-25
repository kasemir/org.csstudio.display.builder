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
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.XYPlotWidget;
import org.csstudio.display.builder.model.widgets.XYPlotWidget.AxisWidgetProperty;
import org.csstudio.display.builder.representation.javafx.widgets.RegionBaseRepresentation;
import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.RTValuePlot;
import org.csstudio.javafx.rtplot.Trace;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.javafx.rtplot.YAxis;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VType;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

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

    /** Canvas that displays the image. */
    private RTValuePlot plot;

    // TODO Support 0..N traces, not 1
    private AtomicReference<Trace<Double>> trace0 = new AtomicReference<>();
    final private XYVTypeDataProvider data0 = new XYVTypeDataProvider();

    @Override
    public Pane createJFXNode() throws Exception
    {
        plot = new RTValuePlot();
        plot.showToolbar(false);
        plot.showCrosshair(true);
        return plot;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();

        model_widget.behaviorLegend().addUntypedPropertyListener(config_listener);

        trackAxisChanges(model_widget.behaviorXAxis());

        for (AxisWidgetProperty axis : model_widget.behaviorYAxes().getValue())
            trackAxisChanges(axis);
        model_widget.behaviorYAxes().addPropertyListener(this::yAxesChanged);

        final UntypedWidgetPropertyListener position_listener = this::positionChanged;
        model_widget.positionWidth().addUntypedPropertyListener(position_listener);
        model_widget.positionHeight().addUntypedPropertyListener(position_listener);

        // TODO Handle multiple traces
        // TODO model_widget.behaviorTraces().addPropertyListener(this::tracesChanged);
        model_widget.behaviorTraces().getElement(0).xValue().addUntypedPropertyListener(this::valueChanged);
        model_widget.behaviorTraces().getElement(0).yValue().addUntypedPropertyListener(this::valueChanged);
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
    }

    private void yAxesChanged(final WidgetProperty<List<AxisWidgetProperty>> property, final List<AxisWidgetProperty> removed, final List<AxisWidgetProperty> added)
    {
        if (removed != null)
            for (AxisWidgetProperty axis : removed)
                ignoreAxisChanges(axis);

        if (added != null)
            for (AxisWidgetProperty axis : added)
                trackAxisChanges(axis);

        config_listener.propertyChanged(property, removed, added);
    }

    private void positionChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_position.mark();
        toolkit.scheduleUpdate(this);
    }

    private void valueChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        try
        {
            final WidgetProperty<VType> x = model_widget.behaviorTraces().getElement(0).xValue();
            final WidgetProperty<VType> y = model_widget.behaviorTraces().getElement(0).yValue();
            final VType x_value = x.getValue();
            final VType y_value = y.getValue();
            if (x_value instanceof VNumberArray  &&  y_value instanceof VNumberArray)
            {
                // Create trace as value changes for the first time and thus sends units
                if (trace0.get() == null)
                {
                    Trace<Double> old_trace = trace0.getAndSet(plot.addTrace(model_widget.behaviorTraces().getElement(0).traceY().getValue(),
                                                               ((VNumberArray)y_value).getUnits(),
                                                               data0, Color.BLUE, TraceType.SINGLE_LINE_DIRECT, 1, PointType.NONE, 5, 0));
                    // Can race result in two value updates trying to add the same trace?
                    // --> Remove the previous one
                    if (old_trace != null)
                        plot.removeTrace(old_trace);
                }
                data0.setData( ((VNumberArray)x_value).getData(), ((VNumberArray)y_value).getData());
                plot.requestUpdate();
            }
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "XYGraph data error", ex);
        }
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_config.checkAndClear())
            updateConfig();
        if (dirty_position.checkAndClear())
        {
            final int w = model_widget.positionWidth().getValue();
            final int h = model_widget.positionHeight().getValue();
            plot.setPrefWidth(w);
            plot.setPrefHeight(h);
        }
        plot.requestUpdate();
    }

    private void updateConfig()
    {
        plot.showLegend(model_widget.behaviorLegend().getValue());

        // Update X Axis
        plot.getXAxis().setName(model_widget.behaviorXAxis().title().getValue());
        plot.getXAxis().setValueRange(model_widget.behaviorXAxis().minimum().getValue(),
                                      model_widget.behaviorXAxis().maximum().getValue());
        plot.getXAxis().setAutoscale(model_widget.behaviorXAxis().autoscale().getValue());

        // Update Y Axes
        final List<AxisWidgetProperty> model_y = model_widget.behaviorYAxes().getValue();
        // Remove extra axes
        int count = plot.getYAxes().size();
        while (count > model_y.size())
            plot.removeYAxis(--count);
        // Add and/or adjust config of existing axes
        for (int i=0;  i<model_y.size();  ++i)
        {
            final AxisWidgetProperty model_axis = model_y.get(i);
            final YAxis<Double> plot_axis;
            if (i < plot.getYAxes().size())
            {
                plot_axis = plot.getYAxes().get(i);
                plot_axis.setName(model_axis.title().getValue());
            }
            else
                plot_axis = plot.addYAxis(model_axis.title().getValue());

            plot_axis.setValueRange(model_axis.minimum().getValue(),
                                    model_axis.maximum().getValue());
            plot_axis.setAutoscale(model_axis.autoscale().getValue());
        }
    }

    @Override
    public void dispose()
    {
        plot.dispose();
        super.dispose();
    }
}
