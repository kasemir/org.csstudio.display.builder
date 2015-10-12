/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.csstudio.display.builder.editor.WidgetSelectionHandler;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Property UI
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PropertyPanel
{
    // TODO Extract 'PropertyPanelSection' with grid and bindings
    // so that elements of Array can have their own section,
    // which can clear & re-populate as array size changes

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

    /** Populate UI with properties of widgets
     *  @param widgets Widgets to configure
     */
    public void setSelectedWidgets(final List<Widget> widgets)
    {
        clear();

        if (widgets.size() < 1)
            return;

        // Determine common properties
        final List<Widget> other = new ArrayList<>(widgets);
        final Widget primary = other.remove(0);
        final Set<WidgetProperty<?>> properties = commonProperties(primary, other);

        // Add UI items for each property
        WidgetPropertyCategory category = null;
        for (final WidgetProperty<?> property : properties)
        {
            // Skip runtime properties
            if (property.getCategory() == WidgetPropertyCategory.RUNTIME)
                continue;

            // Start new category?
            if (property.getCategory() != category)
            {
                category = property.getCategory();

                final Label header = new Label(category.getDescription());
                header.getStyleClass().add("property_category");
                header.setMaxWidth(Double.MAX_VALUE);
                grid.add(header, 0, getNextGridRow(grid), 2, 1);
            }

            createPropertyUI(grid, property, other);
        }
    }

    /** Determine common properties
     *  @param primary Primary widget, the one selected first
     *  @param other Zero or more 'other' widgets
     *  @return Common properties
     */
    private Set<WidgetProperty<?>> commonProperties(final Widget primary, final List<Widget> other)
    {
        if (other.contains(primary))
            throw new IllegalArgumentException("Primary widget " + primary + " included in 'other'");

        // Start with properties of primary widget
        final Set<WidgetProperty<?>> common = new LinkedHashSet<>(primary.getProperties());
        // Keep properties shared by other widgets
        for (Widget w : other)
            common.removeIf(prop  ->  ! w.hasProperty(prop.getName()));
        return common;
    }

    /** @param grid GridPane
     *  @return Next row in grid layout, i.e. row that is not populated
     */
    private int getNextGridRow(final GridPane grid)
    {
        // Goal was to avoid a separate 'row' counter.
        // Depends on nodes being added by rows,
        // so last node reflects index of last populated row.
        final List<Node> nodes = grid.getChildren();
        final int n = nodes.size();
        if (n <= 0)
            return 0;
        final Integer row = GridPane.getRowIndex(nodes.get(n-1));
        return row == null ? 0 : row.intValue() + 1;
    }

    /** Add UI items for displaying or editing propery
     *  @param grid GridPane
     *  @param property Property (on primary widget)
     *  @param other Zero or more additional widgets that have same type of property
     */
    private void createPropertyUI(final GridPane grid, final WidgetProperty<?> property, final List<Widget> other)
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
            final StructuredWidgetProperty struct = (StructuredWidgetProperty) property;
            final Label header = new Label(struct.getDescription());
            header.getStyleClass().add("structure_property_name");
            header.setMaxWidth(Double.MAX_VALUE);
            grid.add(header, 0, getNextGridRow(grid), 2, 1);
            for (WidgetProperty<?> elem : struct.getValue())
                createPropertyUI(grid, elem, other);
            return;
        }
        else if (property instanceof ArrayWidgetProperty)
        {
            @SuppressWarnings("unchecked")
            final ArrayWidgetProperty<WidgetProperty<?>> array = (ArrayWidgetProperty<WidgetProperty<?>>) property;

            // Header for the array property
            final Label header = new Label(array.getDescription());
            header.getStyleClass().add("array_property_name");
            header.setMaxWidth(Double.MAX_VALUE);
            int row = getNextGridRow(grid);
            grid.add(header, 0, row++, 2, 1);

            // UI for changing array size
            grid.add(new Label("Axis Count"), 0, row);
            final TextField count = new TextField();
            grid.add(count, 1, row);

            final GridPane array_grid = new GridPane();

            final ArraySizePropertyBinding count_binding = new ArraySizePropertyBinding(undo, count, array, other);
            bindings.add(count_binding);
            count_binding.bind();

            createArrayElementUI(array_grid, array, other);

            grid.add(array_grid, 0, row+1, 2, 1);

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

        final int row = getNextGridRow(grid);
        grid.add(label, 0, row);
        grid.add(field, 1, row);
    }

    private void createArrayElementUI(final GridPane array_grid,
            final ArrayWidgetProperty<WidgetProperty<?>> array, final List<Widget> other)
    {
        // TODO 'bindings' of the array elements need to be separate so they can unbind/rebind as array size changes
        array_grid.getChildren().clear();
        for (WidgetProperty<?> elem : array.getValue())
            createPropertyUI(array_grid, elem, other);
    }

    /** Clear the property UI */
    private void clear()
    {
        bindings.forEach(WidgetPropertyBinding::unbind);
        bindings.clear();
        grid.getChildren().clear();
    }
}
