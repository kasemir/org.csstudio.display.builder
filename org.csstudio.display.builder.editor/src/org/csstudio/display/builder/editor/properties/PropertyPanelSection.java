/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.csstudio.display.builder.editor.undo.SetMacroizedWidgetPropertyAction;
import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.MacroizedWidgetProperty;
import org.csstudio.display.builder.model.StructuredWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.properties.ActionsWidgetProperty;
import org.csstudio.display.builder.model.properties.BooleanWidgetProperty;
import org.csstudio.display.builder.model.properties.ColorMapWidgetProperty;
import org.csstudio.display.builder.model.properties.ColorWidgetProperty;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;
import org.csstudio.display.builder.model.properties.FontWidgetProperty;
import org.csstudio.display.builder.model.properties.MacrosWidgetProperty;
import org.csstudio.display.builder.model.properties.PointsWidgetProperty;
import org.csstudio.display.builder.model.properties.RulesWidgetProperty;
import org.csstudio.display.builder.model.properties.ScriptsWidgetProperty;
import org.csstudio.display.builder.representation.javafx.AutocompleteMenu;
import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.csstudio.javafx.MultiLineInputDialog;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
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
    private int next_row = -1;
    private Collection<WidgetProperty<?>> properties = Collections.emptyList();
    private boolean show_categories;
    private static final AutocompleteMenu autocomplete_menu = new AutocompleteMenu();

    public PropertyPanelSection()
    {

    }

    public void fill(final UndoableActionManager undo,
            final Collection<WidgetProperty<?>> properties,
            final List<Widget> other,
            final boolean show_categories)
    {
        clear();
        this.properties = properties;
        this.show_categories = show_categories;
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

            this.createPropertyUI(undo, property, other);
        }
    }

    public void refill(final UndoableActionManager undo,
            final List<Widget> other)
    {
        fill(undo, this.properties, other, this.show_categories);
    }

    /** @return Next row in grid layout, i.e. row that is not populated */
    private int getNextGridRow()
    {
        next_row++;
        return next_row;
    }

    public static Node bindSimplePropertyField (
            final UndoableActionManager undo,
            List<WidgetPropertyBinding<?,?>> bindings,
            final WidgetProperty<?> property,
            final List<Widget> other)
    {
        Node field = null;

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
        else if (property instanceof EnumWidgetProperty<?>)
        {
            final EnumWidgetProperty<?> enum_prop = (EnumWidgetProperty<?>) property;
            final ComboBox<String> combo = new ComboBox<>();
            combo.setPromptText(property.getDefaultValue().toString());
            combo.setEditable(true);
            combo.getItems().addAll(enum_prop.getLabels());
            final EnumWidgetPropertyBinding binding =
                    new EnumWidgetPropertyBinding(undo, combo, enum_prop, other);
            bindings.add(binding);
            binding.bind();
            field = combo;
        }
        else if (property instanceof BooleanWidgetProperty)
        {
            final ComboBox<String> combo = new ComboBox<>();
            combo.setPromptText(property.getDefaultValue().toString());
            combo.setEditable(true);
            combo.getItems().addAll("true", "false");
            final BooleanWidgetPropertyBinding binding =
                    new BooleanWidgetPropertyBinding(undo, combo, (BooleanWidgetProperty)property, other);
            bindings.add(binding);
            binding.bind();
            field = combo;
        }
        else if (property instanceof ColorMapWidgetProperty)
        {
            final ColorMapWidgetProperty colormap_prop = (ColorMapWidgetProperty) property;
            final Button map_button = new Button();
            map_button.setMaxWidth(Double.MAX_VALUE);
            final ColorMapPropertyBinding binding = new ColorMapPropertyBinding(undo, map_button, colormap_prop, other);
            bindings.add(binding);
            binding.bind();
            field = map_button;
        }
        else if (property instanceof MacroizedWidgetProperty)
        {
            final MacroizedWidgetProperty<?> macro_prop = (MacroizedWidgetProperty<?>)property;
            final TextField text = new TextField();
            text.setPromptText(macro_prop.getDefaultValue().toString());
            final MacroizedWidgetPropertyBinding binding = (property.getName().contains("pv"))
                    ? new MacroizedWidgetPropertyBinding(undo, text, macro_prop, other)
                    {
                        @Override
                        public void bind()
                        {
                            super.bind();
                            autocomplete_menu.attachField(text);
                        }

                        @Override
                        public void unbind()
                        {
                            super.unbind();
                            autocomplete_menu.removeField(text);
                        }
                    } : new MacroizedWidgetPropertyBinding(undo, text, macro_prop, other);
            bindings.add(binding);
            binding.bind();
            if (CommonWidgetProperties.displayText.getName().equals(property.getName()))
            {   // Allow editing multi-line text in dialog
                final Button open_editor = new Button("...");
                open_editor.setOnAction(event ->
                {
                    final MultiLineInputDialog dialog = new MultiLineInputDialog(macro_prop.getSpecification());
                    final Optional<String> result = dialog.showAndWait();
                    if (!result.isPresent())
                        return;
                    undo.execute(new SetMacroizedWidgetPropertyAction(macro_prop, result.get()));
                    for (Widget w : other)
                    {
                        final MacroizedWidgetProperty<?> other_prop = (MacroizedWidgetProperty<?>) w.getProperty(macro_prop.getName());
                        undo.execute(new SetMacroizedWidgetPropertyAction(other_prop, result.get()));
                    }
                });
                field = new HBox(text, open_editor);
            }
            else
                field = text;
        }
        else if (property instanceof PointsWidgetProperty)
        {
            final PointsWidgetProperty points_prop = (PointsWidgetProperty) property;
            final Button points_field = new Button();
            points_field.setMaxWidth(Double.MAX_VALUE);
            final PointsPropertyBinding binding = new PointsPropertyBinding(undo, points_field, points_prop, other);
            bindings.add(binding);
            binding.bind();
            field = points_field;
        }
        return field;

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
        label.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(label, Priority.ALWAYS);
        //this.setGridLinesVisible(true);

        Node field = bindSimplePropertyField(undo, bindings, property, other);
        if (field != null)
        {
            //do nothing
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
            final ActionsPropertyBinding binding = new ActionsPropertyBinding(undo, actions_field, actions_prop, other,
                    autocomplete_menu);
            bindings.add(binding);
            binding.bind();
            field = actions_field;
        }
        else if (property instanceof ScriptsWidgetProperty)
        {
            final ScriptsWidgetProperty scripts_prop = (ScriptsWidgetProperty) property;
            final Button scripts_field = new Button();
            scripts_field.setMaxWidth(Double.MAX_VALUE);
            final ScriptsPropertyBinding binding = new ScriptsPropertyBinding(undo, scripts_field, scripts_prop, other,
                    autocomplete_menu);
            bindings.add(binding);
            binding.bind();
            field = scripts_field;
        }
        else if (property instanceof RulesWidgetProperty)
        {
            final RulesWidgetProperty rules_prop = (RulesWidgetProperty) property;
            final Button rules_field = new Button();
            rules_field.setMaxWidth(Double.MAX_VALUE);
            final RulesPropertyBinding binding = new RulesPropertyBinding(undo, rules_field, rules_prop, other, autocomplete_menu);
            bindings.add(binding);
            binding.bind();
            field = rules_field;
        }
        else if (property instanceof StructuredWidgetProperty)
        {
            final StructuredWidgetProperty struct = (StructuredWidgetProperty) property;
            final Label header = new Label(struct.getDescription());
            header.getStyleClass().add("structure_property_name");
            header.setMaxWidth(Double.MAX_VALUE);
            add(header, 0, getNextGridRow(), 2, 1);
            for (WidgetProperty<?> elem : struct.getValue())
                this.createPropertyUI(undo, elem, other);
            return;
        }
        else if (property instanceof ArrayWidgetProperty)
        {
            @SuppressWarnings("unchecked")
            final ArrayWidgetProperty<WidgetProperty<?>> array = (ArrayWidgetProperty<WidgetProperty<?>>) property;

            // UI for changing array size
            final Spinner<Integer> spinner = new Spinner<>(array.getMinimumSize(), 100, 0);
            final ArraySizePropertyBinding count_binding = new ArraySizePropertyBinding(this, undo, spinner, array, other);
            bindings.add(count_binding);
            count_binding.bind();

            // set size of array
            final int row = getNextGridRow();
            label.getStyleClass().add("array_property_name");
            spinner.getStyleClass().add("array_property_value");
            add(label, 0, row);
            add(spinner, 1, row);

            // array elements
            for (WidgetProperty<?> elem : array.getValue())
                this.createPropertyUI(undo, elem, other);

            // mark end of array
            final Label endlabel = new Label();
            endlabel.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(endlabel, Priority.ALWAYS);
            endlabel.getStyleClass().add("array_property_name");
            add(endlabel, 0, getNextGridRow(), 2, 1);

            return;
        }
        // As new property types are added, they might need to be handled:
        // else if (property instanceof SomeNewWidgetProperty) { ... }
        else
        {   // Fallback for unknown property: read-only
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

    public AutocompleteMenu getAutocompleteMenu()
    {
        return autocomplete_menu;
    }

    /** Clear the property UI */
    public void clear()
    {
        bindings.forEach(WidgetPropertyBinding::unbind);
        bindings.clear();
        getChildren().clear();
    }
}
