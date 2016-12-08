/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import org.csstudio.display.builder.editor.undo.UseClassAction;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.util.undo.UndoableActionManager;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;

/** Bind checkbox and field for property's value to 'use_class' attribute
 *  @author Kay Kasemir
 */
public class UseWidgetClassBinding extends WidgetPropertyBinding<CheckBox, WidgetProperty<?>>
{
    private final Node field;

    public UseWidgetClassBinding(UndoableActionManager undo, CheckBox node,
            WidgetProperty<?> widget_property, Node field)
    {
        super(undo, node, widget_property, null);
        this.field = field;
    }

    @Override
    public void bind()
    {
        jfx_node.setSelected(widget_property.isUsingWidgetClass());
        field.setDisable(jfx_node.isSelected());
        jfx_node.setOnAction(event ->
        {
            field.setDisable(jfx_node.isSelected());
            undo.execute(new UseClassAction(widget_property, jfx_node.isSelected()));
        });
        // TODO Monitor widget_property for changes of isUsingWidgetClass
        //      .. but don't want to add another listener list to each property just for that..
        //         use existing property _value_ listener?
    }

    @Override
    public void unbind()
    {
        jfx_node.setOnAction(null);
        // TODO Unbind other listeners..
    }
}
