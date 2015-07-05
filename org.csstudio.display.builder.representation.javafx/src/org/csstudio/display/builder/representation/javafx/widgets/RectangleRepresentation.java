/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionHeight;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionY;

import java.beans.PropertyChangeEvent;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.widgets.RectangleWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class RectangleRepresentation extends JFXBaseRepresentation<Rectangle, RectangleWidget>
{
    private final DirtyFlag dirty_size = new DirtyFlag();

    public RectangleRepresentation(final ToolkitRepresentation<Group, Node> toolkit,
                                   final RectangleWidget model_widget)
    {
        super(toolkit, model_widget);
    }

    @Override
    public Rectangle createJFXNode() throws Exception
    {
        final Rectangle rect = new Rectangle(
	  		 model_widget.getPropertyValue(positionX).doubleValue(),
	         model_widget.getPropertyValue(positionY).doubleValue(),
	         model_widget.getPropertyValue(positionWidth).intValue(),
	         model_widget.getPropertyValue(positionHeight).intValue());
        rect.setFill(Color.TRANSPARENT);
        rect.setStroke(Color.MAGENTA);
        rect.setStrokeWidth(1);
        rect.setStrokeType(StrokeType.INSIDE);
        return rect;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addPropertyListener(this::contentChanged);
        model_widget.positionHeight().addPropertyListener(this::contentChanged);
    }

    private void contentChanged(final PropertyChangeEvent event)
    {
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
        {
            jfx_node.setWidth(model_widget.getPropertyValue(positionWidth).doubleValue());
            jfx_node.setHeight(model_widget.getPropertyValue(positionHeight).doubleValue());
        }
    }
}
