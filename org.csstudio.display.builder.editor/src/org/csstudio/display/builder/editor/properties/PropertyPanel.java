/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.editor.undo.UndoableActionManager;
import org.csstudio.display.builder.model.MacroizedWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.properties.ActionsWidgetProperty;
import org.csstudio.display.builder.model.properties.BooleanWidgetProperty;
import org.csstudio.display.builder.model.properties.ColorWidgetProperty;
import org.csstudio.display.builder.model.properties.FontWidgetProperty;
import org.csstudio.display.builder.model.properties.MacrosWidgetProperty;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Property GUI
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PropertyPanel
{
    private final WidgetSelectionHandler selection;
    private final UndoableActionManager undo;
    private final List<WidgetPropertyBinding<?,?>> bindings = new ArrayList<>();
    private GridPane grid;

    /** @param selection Selection handler
     *  @param undo 'Undo' manager
     */
    public PropertyPanel(final WidgetSelectionHandler selection, final UndoableActionManager undo)
    {
        this.selection = selection;
        this.undo = undo;
    }

    /** Create UI components
     *  @return Root {@link Node}
     */
    public Node create()
    {
        final VBox box = new VBox();

        final Label header = new Label("Properties");
        header.setMaxWidth(Double.MAX_VALUE);
        header.getStyleClass().add("header");
        box.getChildren().add(header);

        grid = new GridPane();
        box.getChildren().add(grid);

        // Track currently selected widgets
        selection.addListener(this::setSelectedWidgets);

        return new ScrollPane(box);
    }

    /** Populate GUI with properties of widgets
     *  @param widgets Widgets to configure
     */
    public void setSelectedWidgets(final List<Widget> widgets)
    {
        clear();

        // TODO Get common properties
        // Then display actual property of first (or last?) widget.
        // On change, update _all_ widgets.
        if (widgets.size() != 1)
            return;
        final Set<WidgetProperty<?>> properties = widgets.get(0).getProperties();

        int row = 0;
        WidgetPropertyCategory category = null;
        for (final WidgetProperty<?> property : properties)
        {
            if (property.getCategory() != category)
            {
                category = property.getCategory();

                // Skip runtime properties
                if (category == WidgetPropertyCategory.RUNTIME)
                    continue;

                final Label header = new Label(category.getDescription());
                header.getStyleClass().add("property_category");
                header.setMaxWidth(Double.MAX_VALUE);
                grid.add(header, 0, row++, 2, 1);
            }

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
                final WidgetColorPropertyBinding binding = new WidgetColorPropertyBinding(undo, color_field, color_prop);
                bindings.add(binding);
                binding.bind();
                field = color_field;
            }
            else if (property instanceof FontWidgetProperty)
            {
                final FontWidgetProperty font_prop = (FontWidgetProperty) property;
                final Button font_field = new Button();
                final WidgetFontPropertyBinding binding = new WidgetFontPropertyBinding(undo, font_field, font_prop);
                bindings.add(binding);
                binding.bind();
                field = font_field;
            }
            else if (property instanceof MacrosWidgetProperty)
            {
                final MacrosWidgetProperty macros_prop = (MacrosWidgetProperty) property;
                final Button macros_field = new Button();
                final MacrosPropertyBinding binding = new MacrosPropertyBinding(undo, macros_field, macros_prop);
                bindings.add(binding);
                binding.bind();
                field = macros_field;
            }
            else if (property instanceof ActionsWidgetProperty)
            {
                final ActionsWidgetProperty actions_prop = (ActionsWidgetProperty) property;
                final Button actions_field = new Button();
                final ActionsPropertyBinding binding = new ActionsPropertyBinding(undo, actions_field, actions_prop);
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
                        new BooleanWidgetPropertyBinding(undo, check, (BooleanWidgetProperty)property);
                bindings.add(binding);
                binding.bind();
                field = check;
            }
            else if (property instanceof MacroizedWidgetProperty)
            {
                final TextField text = new TextField();
                final MacroizedWidgetPropertyBinding binding =
                        new MacroizedWidgetPropertyBinding(undo, text,
                                                           (MacroizedWidgetProperty<?>)property);
                bindings.add(binding);
                binding.bind();
                field = text;
            }
            else
            {
                // TODO Provide editor for other property types
                // Defaulting to same as read-only
                final TextField text = new TextField();
                text.setText(String.valueOf(property.getValue()));
                text.setEditable(false);
                field = text;
            }

            label.getStyleClass().add("property_name");
            field.getStyleClass().add("property_value");

            grid.add(label, 0, row);
            grid.add(field, 1, row++);
        }
    }

    /** Clear the property GUI */
    private void clear()
    {
        bindings.forEach(WidgetPropertyBinding::unbind);
        bindings.clear();
        grid.getChildren().clear();
    }
}
