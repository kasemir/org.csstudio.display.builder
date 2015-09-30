/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets.plots;

import java.util.List;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.XYPlotWidget;
import org.csstudio.display.builder.model.widgets.XYPlotWidget.AxisWidgetProperty;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.JFXBaseRepresentation;
import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.RTValuePlot;
import org.csstudio.javafx.rtplot.Trace;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.javafx.rtplot.YAxis;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VType;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class XYPlotRepresentation extends JFXBaseRepresentation<Pane, XYPlotWidget>
{
    private final DirtyFlag dirty_position = new DirtyFlag();
    private final DirtyFlag dirty_config = new DirtyFlag();

    /** Canvas that displays the image. */
    private RTValuePlot plot;

    // TODO Support 0..N traces, not 1
    private Trace<Double> trace0;
    private XYVTypeDataProvider data0;

    public XYPlotRepresentation(final ToolkitRepresentation<Group, Node> toolkit,
                                final XYPlotWidget model_widget)
    {
        super(toolkit, model_widget);
    }

    @Override
    public Pane createJFXNode() throws Exception
    {
        plot = new RTValuePlot();
        plot.showToolbar(false);
        plot.showCrosshair(true);

        // TODO Create data & trace as value changes for the first time and thus sends units
        data0 = new XYVTypeDataProvider();
        trace0 = plot.addTrace("Data", "Units", data0, Color.BLUE, TraceType.SINGLE_LINE_DIRECT, 1, PointType.NONE, 5, 0);
        return plot;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.behaviorXAxis().title().addUntypedPropertyListener(this::configChanged);
        model_widget.behaviorXAxis().minimum().addUntypedPropertyListener(this::configChanged);
        model_widget.behaviorXAxis().maximum().addUntypedPropertyListener(this::configChanged);
        model_widget.behaviorXAxis().autoscale().addUntypedPropertyListener(this::configChanged);
        model_widget.positionWidth().addUntypedPropertyListener(this::positionChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::positionChanged);
        model_widget.behaviorTrace().xValue().addUntypedPropertyListener(this::valueChanged);
        model_widget.behaviorTrace().yValue().addUntypedPropertyListener(this::valueChanged);
    }

    private void configChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_config.mark();
        toolkit.scheduleUpdate(this);
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
            final WidgetProperty<VType> x = model_widget.behaviorTrace().xValue();
            final WidgetProperty<VType> y = model_widget.behaviorTrace().yValue();
            final VType x_value = x.getValue();
            final VType y_value = y.getValue();
            if (x_value instanceof VNumberArray  &&  y_value instanceof VNumberArray)
            {
                data0.setData( ((VNumberArray)x_value).getData(), ((VNumberArray)y_value).getData());
            }
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "XYGraph data error", ex);
        }

        plot.requestUpdate();
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_config.checkAndClear())
            updateAxes();
        if (dirty_position.checkAndClear())
        {
            final int w = model_widget.positionWidth().getValue();
            final int h = model_widget.positionHeight().getValue();
            plot.setPrefWidth(w);
            plot.setPrefHeight(h);
        }
        plot.requestUpdate();
    }

    private void updateAxes()
    {
        // Update X Axis
        plot.getXAxis().setName(model_widget.behaviorXAxis().title().getValue());
        plot.getXAxis().setValueRange(model_widget.behaviorXAxis().minimum().getValue(),
                                      model_widget.behaviorXAxis().maximum().getValue());
        plot.getXAxis().setAutoscale(model_widget.behaviorXAxis().autoscale().getValue());

        // Update Y Axes
        final List<AxisWidgetProperty> model_y = model_widget.behaviorYAxes().getValue();
        for (int i=0;  i<model_y.size();  ++i)
        {
            final AxisWidgetProperty model_axis = model_y.get(i);
            final YAxis<Double> plot_axis;
            if (i <= plot.getYAxes().size())
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
}
