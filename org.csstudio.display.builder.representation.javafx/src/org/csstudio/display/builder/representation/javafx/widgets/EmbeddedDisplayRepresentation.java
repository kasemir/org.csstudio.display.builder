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
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class EmbeddedDisplayRepresentation extends JFXBaseRepresentation<Group, EmbeddedDisplayWidget>
{
    private final DirtyFlag dirty_sizes = new DirtyFlag();

    private static final Color color = Color.CADETBLUE;
    private static final int border_width = 1;

    /** Border around the group */
    private Rectangle border;

    /** Inner group that holds child widgets */
    private Group inner;

    @Override
    public Group createJFXNode() throws Exception
    {
        border = new Rectangle();
        border.setFill(Color.TRANSPARENT);
        border.setStroke(color);
        border.setStrokeWidth(border_width);
        border.setStrokeType(StrokeType.INSIDE);

        inner = new Group();
        // inner.relocate(inset, 2*inset);

        model_widget.setUserData(EmbeddedDisplayWidget.USER_DATA_EMBEDDED_DISPLAY_CONTAINER, inner);

        return new Group(border, inner);
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
        model_widget.positionWidth().addUntypedPropertyListener(this::sizesChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::sizesChanged);
        model_widget.runtimeScale().addUntypedPropertyListener(this::sizesChanged);
    }

    private void sizesChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_sizes.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_sizes.checkAndClear())
        {
            border.setWidth(model_widget.positionWidth().getValue());
            border.setHeight(model_widget.positionHeight().getValue());
            final double zoom = model_widget.runtimeScale().getValue();
            inner.setScaleX(zoom);
            inner.setScaleY(zoom);
        }
    }
}
