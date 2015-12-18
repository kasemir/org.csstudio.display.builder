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
import org.csstudio.display.builder.model.widgets.EllipseWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.StrokeType;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class EllipseRepresentation extends JFXBaseRepresentation<Ellipse, EllipseWidget>
{
    private final DirtyFlag dirty_position = new DirtyFlag();
    private final DirtyFlag dirty_look = new DirtyFlag();
    private Color background, line_color;

    @Override
    public Ellipse createJFXNode() throws Exception
    {
        final Ellipse ellipse = new Ellipse();
        updateColors();
        return ellipse;
    }

    @Override
    protected void registerListeners()
    {
        // JFX Ellipse is based on center, not top-left corner,
        // so can't use the default from super.registerListeners();
        model_widget.positionVisible().addUntypedPropertyListener(this::positionChanged);
        model_widget.positionX().addUntypedPropertyListener(this::positionChanged);
        model_widget.positionY().addUntypedPropertyListener(this::positionChanged);
        model_widget.positionWidth().addUntypedPropertyListener(this::positionChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::positionChanged);

        model_widget.displayBackgroundColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.displayTransparent().addUntypedPropertyListener(this::lookChanged);
        model_widget.displayLineColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.displayLineWidth().addUntypedPropertyListener(this::lookChanged);
    }

    private void positionChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_position.mark();
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
        // Not using default handling of X/Y super.updateChanges();
        if (dirty_position.checkAndClear())
        {
            if (model_widget.positionVisible().getValue())
            {
                jfx_node.setVisible(true);
                final int x = model_widget.positionX().getValue();
                final int y = model_widget.positionY().getValue();
                final int w = model_widget.positionWidth().getValue();
                final int h = model_widget.positionHeight().getValue();
                jfx_node.setCenterX(x + w/2);
                jfx_node.setCenterY(y + h/2);
                jfx_node.setRadiusX(w/2);
                jfx_node.setRadiusY(h/2);
            }
            else
                jfx_node.setVisible(false);
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
