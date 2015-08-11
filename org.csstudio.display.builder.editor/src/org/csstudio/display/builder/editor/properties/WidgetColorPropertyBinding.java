/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.csstudio.display.builder.editor.undo.UndoableActionManager;
import org.csstudio.display.builder.model.properties.ColorWidgetProperty;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/** Bidirectional binding between a WidgetColor and Java FX Property
 *  @author Kay Kasemir
 */
public class WidgetColorPropertyBinding
       extends WidgetPropertyBinding<WidgetColorPropertyField, ColorWidgetProperty>
{
    private final PropertyChangeListener model_listener = new PropertyChangeListener()
    {
        @Override
        public void propertyChange(final PropertyChangeEvent evt)
        {
            if (updating)
                return;
            updating = true;
            try
            {
                jfx_node.setColor(widget_property.getValue());
            }
            finally
            {
                updating = false;
            }
        }
    };
    private EventHandler<ActionEvent> action_handler = event ->
    {
        Alert alert = new Alert(AlertType.INFORMATION,
                                "Should open dialog for " + widget_property);
        alert.setHeaderText("Not implemented");
        alert.show();

        // TODO Show color dialog:
        // WidgetColor new_color = TBDDialog();
        // if (new_color != null)
        //     undo.execute(new SetColorWidgetProperty(widget_property,new_color));
    };

    public WidgetColorPropertyBinding(final UndoableActionManager undo,
                                      final WidgetColorPropertyField field,
                                      final ColorWidgetProperty widget_property)
    {
        super(undo, field, widget_property);
    }

    @Override
    public void bind()
    {
        widget_property.addPropertyListener(model_listener);
        jfx_node.setOnAction(action_handler);
        jfx_node.setColor(widget_property.getValue());
    }

    @Override
    public void unbind()
    {
        jfx_node.setOnAction(null);
        widget_property.removePropertyListener(model_listener);
    }

}
