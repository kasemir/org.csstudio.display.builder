/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.beans.PropertyChangeEvent;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.XYPlotWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.javafx.rtplot.RTValuePlot;
import org.epics.vtype.VType;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class XYPlotRepresentation extends JFXBaseRepresentation<Pane, XYPlotWidget>
{
    private final DirtyFlag dirty_position = new DirtyFlag();
    private final DirtyFlag dirty_value = new DirtyFlag();

    /** Canvas that displays the image. */
    private RTValuePlot plot;

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
        return plot;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addPropertyListener(this::positionChanged);
        model_widget.positionHeight().addPropertyListener(this::positionChanged);

        // TODO 'getElement(3)' is too fragile as structure is extended. Lookup by element name?
        // TODO Widget only aware of 'trace', not trace's 'x_value'
//        model_widget.behaviorTrace().getElement(3).addPropertyListener(this::valueChanged);
//        model_widget.behaviorTrace().getElement(4).addPropertyListener(this::valueChanged);
    }

    private void positionChanged(final PropertyChangeEvent event)
    {
        dirty_position.mark();
        dirty_value.mark();
        toolkit.scheduleUpdate(this);
    }

    private void valueChanged(final PropertyChangeEvent event)
    {
        WidgetProperty<VType> x = model_widget.behaviorTrace().getElement(3);
        WidgetProperty<VType> y = model_widget.behaviorTrace().getElement(4);

        System.out.println("XYPlot: x = " + x);
        System.out.println("XYPlot: y = " + y);

        dirty_value.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_position.checkAndClear())
        {
            final int w = model_widget.positionWidth().getValue();
            final int h = model_widget.positionHeight().getValue();
            plot.setPrefWidth(w);
            plot.setPrefHeight(h);
        }
        if (dirty_value.checkAndClear())
        {
        }
    }
}
