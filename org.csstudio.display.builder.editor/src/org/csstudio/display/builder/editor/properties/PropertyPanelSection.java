/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import static org.csstudio.display.builder.editor.Plugin.logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.editor.undo.SetMacroizedWidgetPropertyAction;
import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.MacroizedWidgetProperty;
import org.csstudio.display.builder.model.StructuredWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetFactory;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.persist.WidgetClassesService;
import org.csstudio.display.builder.model.properties.ActionsWidgetProperty;
import org.csstudio.display.builder.model.properties.BooleanWidgetProperty;
import org.csstudio.display.builder.model.properties.ColorMapWidgetProperty;
import org.csstudio.display.builder.model.properties.ColorWidgetProperty;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;
import org.csstudio.display.builder.model.properties.FilenameWidgetProperty;
import org.csstudio.display.builder.model.properties.FontWidgetProperty;
import org.csstudio.display.builder.model.properties.MacrosWidgetProperty;
import org.csstudio.display.builder.model.properties.PointsWidgetProperty;
import org.csstudio.display.builder.model.properties.RulesWidgetProperty;
import org.csstudio.display.builder.model.properties.ScriptsWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetClassProperty;
import org.csstudio.display.builder.representation.javafx.AutocompleteMenu;
import org.csstudio.display.builder.representation.javafx.FilenameSupport;
import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.csstudio.javafx.DialogHelper;
import org.csstudio.javafx.MultiLineInputDialog;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/** Section of Property panel
 *  @author Kay Kasemir
 *  @author Claudio Rosati
 */
@SuppressWarnings("nls")
public class PropertyPanelSection extends GridPane
{
    private static final AutocompleteMenu autocomplete_menu = new AutocompleteMenu();
    private static final Tooltip use_class_tooltip = new Tooltip(Messages.UseWidgetClass_TT);
    private static final Tooltip using_class_tooltip = new Tooltip(Messages.UsingWidgetClass_TT);

    private boolean class_mode = false;

    private final List<WidgetPropertyBinding<?,?>> bindings = new ArrayList<>();
    private int next_row = -1;
    private Collection<WidgetProperty<?>> properties = Collections.emptyList();
    private boolean show_categories;

    public void setClassMode(final boolean class_mode)
    {
        this.class_mode = class_mode;
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

            // 'class' is not used for the class definition itself,
            // it's only shown for displays where classes are then applied
            if (property instanceof WidgetClassProperty  &&  class_mode)
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

                final Separator separator = new Separator();
                separator.getStyleClass().add("property_separator");
                add(separator, 0, getNextGridRow(), 2, 1);
            }

            createPropertyUI(undo, property, other, 0);
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

    /** Some 'simple' properties are handled
     *  in static method to allow use in the
     *  RulesDialog
     *  @param undo
     *  @param bindings
     *  @param property
     *  @param other
     *  @return
     */
    public static Node bindSimplePropertyField (
            final UndoableActionManager undo,
            final List<WidgetPropertyBinding<?,?>> bindings,
            final WidgetProperty<?> property,
            final List<Widget> other)
    {
        final Widget widget = property.getWidget();
        Node field = null;

        if (property.isReadonly())
        {
            //  If "Type", use a label with an icon.
            if (property.getName().equals(CommonWidgetProperties.propType.getName()))
            {
                final String type = widget.getType();
                try
                {
                    final Image image = new Image(WidgetFactory.getInstance().getWidgetDescriptor(type).getIconStream());
                    final ImageView icon = new ImageView(image);

                    field = new Label(String.valueOf(property.getValue()), icon);
                }
                catch (Exception ex)
                {   //  Some widgets have no icon (e.g. DisplayModel).
                    field = new Label(String.valueOf(property.getValue()));
                }
            }
            else
            {
                final TextField text = new TextField();
                text.setText(String.valueOf(property.getValue()));
                text.setDisable(true);
                field = text;
            }
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
            combo.setMaxWidth(Double.MAX_VALUE);
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
            combo.setMaxWidth(Double.MAX_VALUE);
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
        else if (property instanceof FilenameWidgetProperty)
        {
            final FilenameWidgetProperty file_prop = (FilenameWidgetProperty)property;
            final TextField text = new TextField();
            text.setPromptText(file_prop.getDefaultValue().toString());
            text.setMaxWidth(Double.MAX_VALUE);
            final Button select_file = new Button("...");
            select_file.setOnAction(event ->
            {
                try
                {
                    final String filename = FilenameSupport.promptForRelativePath(widget, file_prop.getValue());
                    if (filename != null)
                        undo.execute(new SetMacroizedWidgetPropertyAction(file_prop, filename));
                }
                catch (Exception ex)
                {
                    logger.log(Level.WARNING, "Cannot prompt for " + file_prop, ex);
                }
            });
            final MacroizedWidgetPropertyBinding binding = new MacroizedWidgetPropertyBinding(undo, text, file_prop, other);
            bindings.add(binding);
            binding.bind();
            field = new HBox(text, select_file);
            HBox.setHgrow(text, Priority.ALWAYS);
        }
        else if (property instanceof WidgetClassProperty)
        {
            final WidgetClassProperty widget_class_prop = (WidgetClassProperty) property;
            final ComboBox<String> combo = new ComboBox<>();
            combo.setPromptText(property.getDefaultValue().toString());
            combo.setEditable(true);
            // List classes of this widget
            final String type = widget.getType();
            final Collection<String> classes = WidgetClassesService.getWidgetClasses().getWidgetClasses(type);
            combo.getItems().addAll(classes);
            combo.setMaxWidth(Double.MAX_VALUE);
            final WidgetClassBinding binding = new WidgetClassBinding(undo, combo, widget_class_prop, other);
            bindings.add(binding);
            binding.bind();
            field = combo;
        }
        else if (property instanceof MacroizedWidgetProperty)
        {
            final MacroizedWidgetProperty<?> macro_prop = (MacroizedWidgetProperty<?>)property;
            final TextField text = new TextField();
            text.setPromptText(macro_prop.getDefaultValue().toString());
            // For now properties that hold a PV name contain "pv" in their name:
            // "pv_name", "selection_value_pv", "x_pv", "y_pv", ...
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
                }
                : new MacroizedWidgetPropertyBinding(undo, text, macro_prop, other);
            bindings.add(binding);
            binding.bind();
            if (CommonWidgetProperties.propText.getName().equals(property.getName())  ||
                CommonWidgetProperties.propTooltip.getName().equals(property.getName()))
            {   // Allow editing multi-line text in dialog
                final Button open_editor = new Button("...");
                open_editor.setOnAction(event ->
                {
                    final MultiLineInputDialog dialog = new MultiLineInputDialog(macro_prop.getSpecification());
                    DialogHelper.positionDialog(dialog, open_editor, -600, 0);
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
                HBox.setHgrow(text, Priority.ALWAYS);
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
     *  @param structureIndex Index of the array structure (element) being added. It is meaningful
     *                        only for properties instance of {@link StructuredWidgetProperty}.
     */
    private void createPropertyUI(final UndoableActionManager undo,
            final WidgetProperty<?> property,
            final List<Widget> other,
            final int structureIndex)
    {
        // Skip runtime properties
        if (property.getCategory() == WidgetPropertyCategory.RUNTIME)
            return;

        final Label label = new Label(property.getDescription());
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinWidth(100);
        final String tooltip = property.getDescription() + " (" + property.getName() + ")";
        label.setTooltip(new Tooltip(tooltip));
        // setGridLinesVisible(true); // For debugging the layout

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
        {   // Don't allow editing structures and their elements in class mode
            if (class_mode)
                return;
            final StructuredWidgetProperty struct = (StructuredWidgetProperty) property;
            final Label header = new Label(struct.getDescription() + ( structureIndex > 0 ? " " + String.valueOf(1 + structureIndex) : ""));
            header.getStyleClass().add("structure_property_name");
            header.setMaxWidth(Double.MAX_VALUE);

            add(header, 0, getNextGridRow(), 1, 1);

            final Separator separator = new Separator();
            separator.getStyleClass().add("property_separator");
            add(separator, 0, getNextGridRow(), 1, 1);

            for (WidgetProperty<?> elem : struct.getValue())
                this.createPropertyUI(undo, elem, other, -1);
            return;
        }
        else if (property instanceof ArrayWidgetProperty)
        {   // Don't allow editing arrays and their elements in class mode
            if (class_mode)
                return;
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
            label.setMaxWidth(Double.MAX_VALUE);
            label.setMaxHeight(Double.MAX_VALUE);
            spinner.getStyleClass().add("array_property_value");
            // Place array size spinner in 'label' section
            HBox.setHgrow(label, Priority.ALWAYS);
            add(new HBox(label, spinner), 0, row);

            Separator separator = new Separator();

            separator.getStyleClass().add("property_separator");
            add(separator, 0, getNextGridRow(), 1, 1);

            // array elements
            final List<WidgetProperty<?>> wpeList = array.getValue();
            for (int i = 0; i < wpeList.size(); i++)
            {
                final WidgetProperty<?> elem = wpeList.get(i);
                createPropertyUI(undo, elem, other, i);
            }

            // mark end of array
            final Label endlabel = new Label();
            endlabel.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(endlabel, Priority.ALWAYS);
            endlabel.getStyleClass().add("array_property_end");
            add(endlabel, 0, getNextGridRow(), 1, 1);

            separator = new Separator();

            separator.getStyleClass().add("property_separator");
            add(separator, 0, getNextGridRow(), 1, 1);

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
        // Allow label to shrink (can use tooltip to see),
        // but show the value
        // GridPane.setHgrow(label, Priority.ALWAYS);
        GridPane.setHgrow(field, Priority.ALWAYS);
        add(label, 0, row);
        add(field, 1, row);

        final Widget widget = property.getWidget();
        if (! (property == widget.getProperty("type")  ||
               property == widget.getProperty("name")))
        {
            if (class_mode)
            {   // Class definition mode:
                // Check box for 'use_class'
                final CheckBox check = new CheckBox();
                check.setTooltip(use_class_tooltip);
                final WidgetPropertyBinding<?,?> binding = new UseWidgetClassBinding(undo, check, field, property, other);
                bindings.add(binding);
                binding.bind();
                add(check, 1, row);
            }
            else
            {   // Display file mode:
                // Show if property is set by the class, not editable.
                final Label indicator = new Label();
                indicator.setTooltip(using_class_tooltip);
                final WidgetPropertyBinding<?,?> binding = new ShowWidgetClassBinding(field, property, indicator);
                bindings.add(binding);
                binding.bind();
                add(indicator, 1, row);
            }
        }

        final Separator separator = new Separator();
        separator.getStyleClass().add("property_separator");
        add(separator, 0, getNextGridRow(), 2, 1);
    }

    public AutocompleteMenu getAutocompleteMenu()
    {
        return autocomplete_menu;
    }

    /** Clear the property UI
     *  <P>Removes all property bindings and their UI
     */
    public void clear()
    {
        bindings.forEach(WidgetPropertyBinding::unbind);
        bindings.clear();
        getChildren().clear();
    }
}
