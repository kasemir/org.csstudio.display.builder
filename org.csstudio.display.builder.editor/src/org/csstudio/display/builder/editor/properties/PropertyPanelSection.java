/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.MacroizedWidgetProperty;
import org.csstudio.display.builder.model.StructuredWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.properties.ActionsWidgetProperty;
import org.csstudio.display.builder.model.properties.BooleanWidgetProperty;
import org.csstudio.display.builder.model.properties.ColorWidgetProperty;
import org.csstudio.display.builder.model.properties.FontWidgetProperty;
import org.csstudio.display.builder.model.properties.MacrosWidgetProperty;
import org.csstudio.display.builder.util.undo.UndoableActionManager;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/** Section of Property panel
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PropertyPanelSection extends GridPane
{
    private final List<WidgetPropertyBinding<?,?>> bindings = new ArrayList<>();

    public PropertyPanelSection()
    {

    }

    public void fill(final UndoableActionManager undo,
		    		 final Collection<WidgetProperty<?>> properties,
		    		 final List<Widget> other,
		    		 final boolean show_categories)
    {
        // Add UI items for each property
        WidgetPropertyCategory category = null;
        for (final WidgetProperty<?> property : properties)
        {
            // Skip runtime properties
            if (property.getCategory() == WidgetPropertyCategory.RUNTIME)
                continue;

            // Start of new category that needs to be shown?
            if (show_categories &&
            	property.getCategory() != category)
            {
                category = property.getCategory();

                final Label header = new Label(category.getDescription());
                header.getStyleClass().add("property_category");
                header.setMaxWidth(Double.MAX_VALUE);
                add(header, 0, getNextGridRow(), 2, 1);
            }

            createPropertyUI(undo, property, other);
        }
    }

    /** @return Next row in grid layout, i.e. row that is not populated */
    private int getNextGridRow()
    {
        // Goal was to avoid a separate 'row' counter.
        // Depends on nodes being added by rows,
        // so last node reflects index of last populated row.
        final List<Node> nodes = getChildren();
        final int n = nodes.size();
        if (n <= 0)
            return 0;
        final Integer row = GridPane.getRowIndex(nodes.get(n-1));
        return row == null ? 0 : row.intValue() + 1;
    }

    /** Add UI items for displaying or editing property
     *  @param property Property (on primary widget)
     *  @param other Zero or more additional widgets that have same type of property
     */
    private void createPropertyUI(final UndoableActionManager undo,
    							  final WidgetProperty<?> property,
    							  final List<Widget> other)
    {
        // Skip runtime properties
        if (property.getCategory() == WidgetPropertyCategory.RUNTIME)
            return;

        final Label label = new Label(property.getDescription());
        final Node field;
        if (property.isReadonly())
        {
            final TextField text = new TextField();
            text.setText(String.valueOf(property.getValue()));
            text.setEditable(false);
            field = text;
        }
        else if (property instanceof ColorWidgetProperty)
        {
            final ColorWidgetProperty color_prop = (ColorWidgetProperty) property;
            final WidgetColorPropertyField color_field = new WidgetColorPropertyField();
            final WidgetColorPropertyBinding binding = new WidgetColorPropertyBinding(undo, color_field, color_prop, other);
            bindings.add(binding);
            binding.bind();
            field = color_field;
        }
        else if (property instanceof FontWidgetProperty)
        {
            final FontWidgetProperty font_prop = (FontWidgetProperty) property;
            final Button font_field = new Button();
            font_field.setMaxWidth(Double.MAX_VALUE);
            final WidgetFontPropertyBinding binding = new WidgetFontPropertyBinding(undo, font_field, font_prop, other);
            bindings.add(binding);
            binding.bind();
            field = font_field;
        }
        else if (property instanceof MacrosWidgetProperty)
        {
            final MacrosWidgetProperty macros_prop = (MacrosWidgetProperty) property;
            final Button macros_field = new Button();
            macros_field.setMaxWidth(Double.MAX_VALUE);
            final MacrosPropertyBinding binding = new MacrosPropertyBinding(undo, macros_field, macros_prop, other);
            bindings.add(binding);
            binding.bind();
            field = macros_field;
        }
        else if (property instanceof ActionsWidgetProperty)
        {
            final ActionsWidgetProperty actions_prop = (ActionsWidgetProperty) property;
            final Button actions_field = new Button();
            actions_field.setMaxWidth(Double.MAX_VALUE);
            final ActionsPropertyBinding binding = new ActionsPropertyBinding(undo, actions_field, actions_prop, other);
            bindings.add(binding);
            binding.bind();
            field = actions_field;
        }
        else if (property instanceof BooleanWidgetProperty)
        {
            final ComboBox<String> check = new ComboBox<>();
            check.setEditable(true);
            check.getItems().addAll("true", "false");
            final BooleanWidgetPropertyBinding binding =
                    new BooleanWidgetPropertyBinding(undo, check, (BooleanWidgetProperty)property, other);
            bindings.add(binding);
            binding.bind();
            field = check;
        }
        else if (property instanceof MacroizedWidgetProperty)
        {
            final TextField text = new TextField();
            final MacroizedWidgetPropertyBinding binding =
                    new MacroizedWidgetPropertyBinding(undo, text,
                                                       (MacroizedWidgetProperty<?>)property, other);
            bindings.add(binding);
            binding.bind();
            field = text;
        }
        else if (property instanceof StructuredWidgetProperty)
        {
        	// TODO recurse into another PropertyPanelSection?
            final StructuredWidgetProperty struct = (StructuredWidgetProperty) property;
            final Label header = new Label(struct.getDescription());
            header.getStyleClass().add("structure_property_name");
            header.setMaxWidth(Double.MAX_VALUE);
            add(header, 0, getNextGridRow(), 2, 1);
            for (WidgetProperty<?> elem : struct.getValue())
                createPropertyUI(undo, elem, other);
            return;
        }
        else if (property instanceof ArrayWidgetProperty)
        {
            @SuppressWarnings("unchecked")
            final ArrayWidgetProperty<WidgetProperty<?>> array = (ArrayWidgetProperty<WidgetProperty<?>>) property;

            // TODO Create new 'grid' node for the array elements
            // Array elements cannot be in the original grid
            // because otherwise grid items that follow the array
            // elements would need to move to new grid rows
            // as array is resized.

            label.setMaxWidth(Double.MAX_VALUE);
            // TODO UI for changing array size
            final Button add = new Button("+");
            final Button remove = new Button("-");
            final HBox header = new HBox(label, add, remove);
            HBox.setHgrow(label, Priority.ALWAYS);
            header.getStyleClass().add("array_property_name");

            int row = getNextGridRow();
            add(header, 0, row++, 2, 1);

            // TODO Update array section, remove it, ..
            PropertyPanelSection array_section = new  PropertyPanelSection();
            array_section.fill(undo, array.getValue(), other, false);

//            final ArraySizePropertyBinding count_binding = new ArraySizePropertyBinding(undo, count, array, other);
//            bindings.add(count_binding);
//            count_binding.bind();

            array_section.getStyleClass().add("debug");

            add(array_section, 0, row+1, 2, 1);

            return;
        }
        else
        {
            // TODO Provide editor for other property types: Script.
            // Defaulting to same as read-only
            final TextField text = new TextField();
            text.setText(String.valueOf(property.getValue()));
            text.setEditable(false);
            field = text;
        }

        label.getStyleClass().add("property_name");
        field.getStyleClass().add("property_value");

        final int row = getNextGridRow();
        add(label, 0, row);
        add(field, 1, row);
    }

    /** Clear the property UI */
    public void clear()
    {
        bindings.forEach(WidgetPropertyBinding::unbind);
        bindings.clear();
		getChildren().clear();
    }
}
