/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.RectangleWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class RectangleRepresentation extends JFXBaseRepresentation<Rectangle, RectangleWidget>
{
    private final DirtyFlag dirty_size = new DirtyFlag();
    private final DirtyFlag dirty_look = new DirtyFlag();
    private Color background, line_color;

    @Override
    public Rectangle createJFXNode() throws Exception
    {
        final Rectangle rect = new Rectangle();
        updateColors();
        return rect;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::sizeChanged);
        model_widget.displayCornerWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.displayCornerHeight().addUntypedPropertyListener(this::sizeChanged);

        model_widget.displayBackgroundColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.displayTransparent().addUntypedPropertyListener(this::lookChanged);
        model_widget.displayLineColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.displayLineWidth().addUntypedPropertyListener(this::lookChanged);
   }

    private void sizeChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }

    private void lookChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        updateColors();
        dirty_look.mark();
        toolkit.scheduleUpdate(this);
    }

    private void updateColors()
    {
        background = model_widget.displayTransparent().getValue()
                   ? Color.TRANSPARENT
                   : JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
        line_color = JFXUtil.convert(model_widget.displayLineColor().getValue());
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
        {
            jfx_node.setWidth(model_widget.positionWidth().getValue());
            jfx_node.setHeight(model_widget.positionHeight().getValue());
            jfx_node.setArcWidth(model_widget.displayCornerWidth().getValue());
            jfx_node.setArcHeight(model_widget.displayCornerHeight().getValue());
        }
        if (dirty_look.checkAndClear())
        {
            jfx_node.setFill(background);
            jfx_node.setStroke(line_color);
            jfx_node.setStrokeWidth(model_widget.displayLineWidth().getValue());
            jfx_node.setStrokeType(StrokeType.INSIDE);
        }
    }
}
