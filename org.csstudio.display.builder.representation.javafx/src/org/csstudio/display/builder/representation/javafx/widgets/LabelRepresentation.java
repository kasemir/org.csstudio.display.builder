/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class LabelRepresentation extends RegionBaseRepresentation<Label, LabelWidget>
{
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();
    private Pos pos;

    @Override
    public Label createJFXNode() throws Exception
    {
        return new Label();
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::styleChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayForegroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayBackgroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayTransparent().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayFont().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayHorizontalAlignment().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayText().addUntypedPropertyListener(this::contentChanged);
    }

    private Pos computePos()
    {
        switch (model_widget.displayHorizontalAlignment().getValue())
        {
        case CENTER: return Pos.TOP_CENTER;
        case RIGHT:  return Pos.TOP_RIGHT;
        default:
        case LEFT:   return Pos.TOP_LEFT;
        }
    }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        pos = computePos();
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_style.checkAndClear())
        {
            Color color = JFXUtil.convert(model_widget.displayForegroundColor().getValue());
            jfx_node.setTextFill(color);

            jfx_node.setPrefSize(model_widget.positionWidth().getValue(),
                                 model_widget.positionHeight().getValue());
            jfx_node.setAlignment(pos);
            if (model_widget.displayTransparent().getValue())
            {   // No fill
                jfx_node.setBackground(null);
            }
            else
            {   // Fill background
                color = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
                jfx_node.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
            }
            jfx_node.setFont(JFXUtil.convert(model_widget.displayFont().getValue()));
        }
        if (dirty_content.checkAndClear())
            jfx_node.setText(model_widget.displayText().getValue());
    }
}
