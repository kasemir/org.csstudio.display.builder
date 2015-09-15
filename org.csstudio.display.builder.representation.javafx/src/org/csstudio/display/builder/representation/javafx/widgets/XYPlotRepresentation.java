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
import org.csstudio.display.builder.model.widgets.XYPlotWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.javafx.rtplot.RTValuePlot;

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
    private final DirtyFlag dirty_content = new DirtyFlag();

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
        model_widget.runtimeValue().addPropertyListener(this::contentChanged);
    }

    private void positionChanged(final PropertyChangeEvent event)
    {
        dirty_position.mark();
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    private void contentChanged(final PropertyChangeEvent event)
    {
        dirty_content.mark();
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
        if (dirty_content.checkAndClear())
        {
        }
    }
}
