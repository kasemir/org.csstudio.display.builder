/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import java.util.List;

import org.csstudio.display.builder.editor.undo.AddArrayElementAction;
import org.csstudio.display.builder.editor.undo.RemoveArrayElementAction;
import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.util.undo.UndoableActionManager;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Spinner;

/** Bidirectional binding between an ArrayWidgetProperty and Java FX Spinner for number of array elements
 *
 *  <p>In comparison to most {@link WidgetPropertyBinding}s this binding
 *  will not only control a property (the number of array elements)
 *  but also update a sub-panel of the property panel to show the current
 *  list of array elements.
 *
 *  @author Kay Kasemir
 */
public class ArraySizePropertyBinding extends WidgetPropertyBinding<Spinner<Integer>, ArrayWidgetProperty<WidgetProperty<?>>>
{
    private PropertyPanelSection panel_section;

    /** Add/remove elements from array property in response to property UI */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final ChangeListener<? super Integer> ui_listener = (prop, old, value) ->
    {
        final int desired = jfx_node.getValue();

        // Grow/shrink array via undo-able actions
        while (widget_property.size() < desired)
        {
            undo.add(new AddArrayElementAction<>(widget_property, widget_property.addElement()));
            for (Widget w : other)
            {
                final ArrayWidgetProperty other_prop = (ArrayWidgetProperty) w.getProperty(widget_property.getName());
                undo.add(new AddArrayElementAction<>(other_prop, other_prop.addElement()));
            }
        }
        while (widget_property.size() > desired)
        {
            undo.execute(new RemoveArrayElementAction<>(widget_property));
            for (Widget w : other)
            {
                final ArrayWidgetProperty other_prop = (ArrayWidgetProperty) w.getProperty(widget_property.getName());
                undo.add(new RemoveArrayElementAction<>(other_prop));
            }
        }
    };

    /** Update property sub-panel as array elements are added/removed */
    private WidgetPropertyListener<List<WidgetProperty<?>>> prop_listener = (prop, removed, added) ->
    {
        panel_section.fill(undo, widget_property.getValue(), other, false);
    };

    /** @param panel_section Panel section for array elements
     *  @param undo Undo support
     *  @param node JFX node for array element count
     *  @param widget_property {@link ArrayWidgetProperty}
     *  @param other Widgets that also have this array property
     */
    public ArraySizePropertyBinding(final PropertyPanelSection panel_section,
                                    final UndoableActionManager undo,
                                    final Spinner<Integer> node,
                                    final ArrayWidgetProperty<WidgetProperty<?>> widget_property,
                                    final List<Widget> other)
    {
        super(undo, node, widget_property, other);
        this.panel_section = panel_section;
    }

    @Override
    public void bind()
    {
        jfx_node.valueProperty().addListener(ui_listener);
        jfx_node.getValueFactory().setValue(widget_property.size());

        widget_property.addPropertyListener(prop_listener);

        panel_section.fill(undo, widget_property.getValue(), other, false);
    }

    @Override
    public void unbind()
    {
        widget_property.removePropertyListener(prop_listener);
        jfx_node.valueProperty().removeListener(ui_listener);
    }
}
