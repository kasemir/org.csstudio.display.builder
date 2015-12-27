/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import java.util.List;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.properties.Points;
import org.csstudio.display.builder.model.properties.PointsWidgetProperty;
import org.csstudio.display.builder.util.undo.UndoableActionManager;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

/** Bidirectional binding between a macro property in model and Java FX Node in the property panel
 *  @author Kay Kasemir
 */
public class PointsPropertyBinding
    extends WidgetPropertyBinding<Button, PointsWidgetProperty>

{
    private WidgetPropertyListener<Points> model_listener = (p, o, n) ->
    {
        jfx_node.setText(widget_property.getValue().size() + " Points");
    };

    private EventHandler<ActionEvent> actionHandler = event ->
    {
        // TODO Table Editor for list of points
        new Alert(AlertType.INFORMATION, "Should open list of points", ButtonType.OK).showAndWait();
    };

    public PointsPropertyBinding(final UndoableActionManager undo,
                                 final Button field,
                                 final PointsWidgetProperty widget_property,
                                 final List<Widget> other)
    {
        super(undo, field, widget_property, other);
    }

    @Override
    public void bind()
    {
        widget_property.addPropertyListener(model_listener);
        jfx_node.setOnAction(actionHandler);
        model_listener.propertyChanged(widget_property, null,  null);
    }

    @Override
    public void unbind()
    {
        jfx_node.setOnAction(null);
        widget_property.removePropertyListener(model_listener);
    }
}
