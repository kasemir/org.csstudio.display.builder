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

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Spinner;

/** Bidirectional binding between an ArrayWidgetProperty and Java FX Spinner for number of array elements
 *
 *  <p>In comparison to most {@link WidgetPropertyBinding} this binding
 *  will not only control a property (the number of array elements)
 *  but also update a sub-panel of the property panel to show the current
 *  list of array elements.
 *
 *  @author Kay Kasemir
 */
public class ArraySizePropertyBinding extends WidgetPropertyBinding<Spinner<Integer>, ArrayWidgetProperty<WidgetProperty<?>>>
{
    private PropertyPanelSection panel_section;

    private final ChangeListener<? super Integer> listener = (prop, old, value) ->
    {
        // TODO Support undo for property as well as 'other'
        final int desired = jfx_node.getValue();

        // Grow/shrink array
        while (widget_property.size() < desired)
            widget_property.addElement();
        while (widget_property.size() > desired)
            widget_property.removeElement();

        // Update property panel section
        panel_section.fill(undo, widget_property.getValue(), other, false);
    };

    /** @param panel_section Panel section for array elements
     *  @param undo Undo support
     *  @param node JFX node for array element cound
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
        jfx_node.valueProperty().addListener(listener);
        jfx_node.getValueFactory().setValue(widget_property.size());

        panel_section.fill(undo, widget_property.getValue(), other, false);
    }

    @Override
    public void unbind()
    {
        jfx_node.valueProperty().removeListener(listener);
    }
}
