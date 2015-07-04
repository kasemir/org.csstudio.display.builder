/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionHeight;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeInsets;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetName;

import java.beans.PropertyChangeEvent;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class GroupRepresentation extends JFXBaseRepresentation<Group, GroupWidget>
{
    private final DirtyFlag dirty_border = new DirtyFlag();

    private static final Color border_color = Color.GRAY;
    private static final int inset = 10;
    private static final int border_width = 1;

    /** Border around the group */
    private Rectangle border;

    /** Label on top of border */
    private Label label;

    /** Inner group that holds child widgets */
    private Group inner;

    public GroupRepresentation(final ToolkitRepresentation<Group, Node> toolkit,
                               final GroupWidget model_widget)
    {
        super(toolkit, model_widget);
    }

    @Override
    public Group createJFXNode() throws Exception
    {
        border = new Rectangle();
        border.relocate(border_width, inset);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(border_color);
        border.setStrokeWidth(border_width);
        border.setStrokeType(StrokeType.INSIDE);

        label = new Label();
        label.relocate(inset, 0);

        inner = new Group();
        inner.relocate(inset, 2*inset);

        model_widget.setPropertyValue(runtimeInsets, new int[] { inset, 2*inset });

        // Would be easy to scale the content
        // in case it needs to grow/shrink to fit
        // double scale = 0.5;
        // inner.setScaleX(scale);
        // inner.setScaleY(scale);

        return new Group(border, label, inner);
    }

    @Override
    protected Group getChildParent(final Group parent)
    {
        return inner;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.addPropertyListener(displayBackgroundColor, this::borderChanged);
        model_widget.addPropertyListener(widgetName, this::borderChanged);
        model_widget.addPropertyListener(positionWidth, this::borderChanged);
        model_widget.addPropertyListener(positionHeight, this::borderChanged);

    }

    private void borderChanged(final PropertyChangeEvent event)
    {
        dirty_border.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_border.checkAndClear())
        {
            final Color background = JFXUtil.convert(model_widget.getPropertyValue(displayBackgroundColor));
            label.setBackground(new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY)));
            label.setText(model_widget.getPropertyValue(widgetName));
            border.setWidth(model_widget.getPropertyValue(positionWidth).intValue() - 2*inset);
            border.setHeight(model_widget.getPropertyValue(positionHeight).intValue() - 2*inset);
        }
    }
}
