/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.model.widgets.GroupWidget.Style;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class GroupRepresentation extends JFXBaseRepresentation<Group, GroupWidget>
{
    private final DirtyFlag dirty_border = new DirtyFlag();

    private static final int border_width = 1;

    /** Border around the group */
    private Rectangle border;

    /** Label on top of border */
    private Label label;

    /** Inner group that holds child widgets */
    private Pane inner;

    private volatile int inset = 10;
    private volatile Color foreground_color, background_color;

    @Override
    public Group createJFXNode() throws Exception
    {
        border = new Rectangle();
        border.setFill(Color.TRANSPARENT);
        border.setStrokeWidth(border_width);
        border.setStrokeType(StrokeType.INSIDE);

        label = new Label();

        inner = new Pane();

        computeColors();

        return new Group(border, label, inner);
    }

    @Override
    protected Parent getChildParent(final Parent parent)
    {
        return inner;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        final UntypedWidgetPropertyListener listener = this::borderChanged;
        model_widget.displayBackgroundColor().addUntypedPropertyListener(listener);
        model_widget.widgetName().addUntypedPropertyListener(listener);
        model_widget.displayStyle().addUntypedPropertyListener(listener);
        model_widget.displayFont().addUntypedPropertyListener(listener);
        model_widget.positionWidth().addUntypedPropertyListener(listener);
        model_widget.positionHeight().addUntypedPropertyListener(listener);

    }

    private void borderChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        computeColors();
        dirty_border.mark();
        toolkit.scheduleUpdate(this);
    }

    private void computeColors()
    {
        foreground_color = JFXUtil.convert(model_widget.displayForegroundColor().getValue());
        background_color = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_border.checkAndClear())
        {
            final int width = model_widget.positionWidth().getValue();
            final int height = model_widget.positionHeight().getValue();
            final WidgetFont font = model_widget.displayFont().getValue();
            label.setFont(JFXUtil.convert(font));
            label.setText(model_widget.widgetName().getValue());

            final Style style = model_widget.displayStyle().getValue();
            if (style == Style.NONE)
            {
                inset = 0;
                border.relocate(0, 0);
                border.setWidth(width);
                border.setHeight(height);
                border.setStroke(null);

                label.setVisible(false);

                inner.relocate(0, 0);
                model_widget.runtimeInsets().setValue(new int[] { 0, 0 });
            }
            else if (style == Style.TITLE)
            {
                inset = (int) (1.2*font.getSize());
                border.relocate(0, inset);
                border.setWidth(width);
                border.setHeight(height - inset);
                border.setStroke(foreground_color);

                label.setVisible(true);
                label.relocate(0, 0);
                label.setPrefSize(width, inset);
                label.setTextFill(background_color);
                label.setBackground(new Background(new BackgroundFill(foreground_color, CornerRadii.EMPTY, Insets.EMPTY)));

                inner.relocate(border_width, inset+border_width);
                model_widget.runtimeInsets().setValue(new int[] { border_width, inset+border_width });
            }
            else if (style == Style.LINE)
            {
                inset = 0;
                border.relocate(0, 0);
                border.setWidth(width);
                border.setHeight(height);
                border.setStroke(foreground_color);

                label.setVisible(false);

                inner.relocate(border_width, border_width);
                model_widget.runtimeInsets().setValue(new int[] { border_width, border_width });
            }
            else // GROUP
            {
                inset = (int) (1.2*font.getSize());
                final int hi = (inset+1)/2; // round up
                border.relocate(hi, hi);
                border.setWidth(width - inset);
                border.setHeight(height - inset);
                border.setStroke(foreground_color);
                border.setStrokeWidth(border_width);
                border.setStrokeType(StrokeType.INSIDE);

                label.setVisible(true);
                label.relocate(inset, 0);
                label.setPrefWidth(Label.USE_COMPUTED_SIZE);
                label.setTextFill(foreground_color);
                label.setBackground(new Background(new BackgroundFill(background_color, CornerRadii.EMPTY, Insets.EMPTY)));

                inner.relocate(inset, inset);
                model_widget.runtimeInsets().setValue(new int[] { inset, inset });
            }
        }
    }
}
