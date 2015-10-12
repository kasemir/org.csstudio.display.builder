/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import java.util.List;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.util.undo.UndoableActionManager;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;

/** Bidirectional binding between an ArrayWidgetProperty and Java FX TextField for number of array elements
 *  @author Kay Kasemir
 */
public class ArraySizePropertyBinding extends WidgetPropertyBinding<TextField, ArrayWidgetProperty<WidgetProperty<?>>>
{
    private final EventHandler<ActionEvent> action_handler = event ->
    {
        // TODO Support undo for property as well as 'other'
        final int desired = Integer.parseInt(jfx_node.getText().trim());
        while (widget_property.size() < desired)
        {
            widget_property.addElement();
        }
        while (widget_property.size() > desired)
        {
            widget_property.removeElement();
        }

        // TODO Update property panel section
    };

    public ArraySizePropertyBinding(final UndoableActionManager undo,
                                    final TextField node,
                                    final ArrayWidgetProperty<WidgetProperty<?>> widget_property,
                                    final List<Widget> other)
    {
        super(undo, node, widget_property, other);
    }

    @Override
    public void bind()
    {
        jfx_node.setOnAction(action_handler);
        jfx_node.setText(Integer.toString(widget_property.size()));
    }

    @Override
    public void unbind()
    {
        jfx_node.setOnAction(action_handler);
    }
}
