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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.controlsfx.control.textfield.TextFields;
import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.representation.javafx.AutocompleteMenu;
import org.csstudio.display.builder.util.ResourceUtil;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/** Property UI
 *
 *  @author Kay Kasemir
 *  @author Claudio Rosati
 */
@SuppressWarnings( "nls" )
public class PropertyPanel extends BorderPane
{
    private final DisplayEditor        editor;
    private final PropertyPanelSection section;
    private final ToggleButton         pinSearchButton;

    /** @param selection Selection handler
     *  @param undo 'Undo' manager
     */
    public PropertyPanel (final DisplayEditor editor)
    {

        this.editor = editor;
        this.section = new PropertyPanelSection();
        this.pinSearchButton = new ToggleButton("P");

        try {
            pinSearchButton.setGraphic(new ImageView(new Image(ResourceUtil.openPlatformResource("platform:/plugin/org.csstudio.display.builder.editor/icons/pin.png"))));
        } catch ( Exception ex ) {
            logger.log(Level.WARNING, "Cannot load macro edit image.", ex);
        }
        pinSearchButton.getStyleClass().add("macro_button");
        pinSearchButton.setDisable(true);
        pinSearchButton.setTooltip(new Tooltip(Messages.PinSearchButton));
        HBox.setHgrow(pinSearchButton, Priority.NEVER);

        final TextField searchField = TextFields.createClearableTextField();
        searchField.setPromptText("Search");
        searchField.textProperty().addListener( ( observable, oldValue, newValue ) -> filterProperties(newValue));
        HBox.setHgrow(searchField, Priority.NEVER);

        final HBox toolsPane = new HBox(6);
        toolsPane.setAlignment(Pos.CENTER_RIGHT);
        toolsPane.setPadding(new Insets(6));
        toolsPane.getChildren().addAll(pinSearchButton, searchField);

        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(section);
        scrollPane.setMinHeight(0);

        setTop(toolsPane);
        setCenter(scrollPane);

        // Track currently selected widgets
        editor.getWidgetSelectionHandler().addListener(widgets -> {

            searchField.setDisable(widgets.isEmpty() && editor.getModel() == null);

            if ( !pinSearchButton.isSelected() ) {
                searchField.setText(null);
                filterProperties(null);
            } else {
                filterProperties(searchField.getText());
            }

        });

        setMinHeight(0);
    }

    public AutocompleteMenu getAutocompleteMenu ()
    {
        return section.getAutocompleteMenu();
    }

    /** Determine common properties
     *
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
        // Keep properties shared by other widgets, i.e. remove those _not_ in
        // other
        for (Widget o : other)
            common.removeIf(prop -> !o.checkProperty(prop.getName()).isPresent());

        return common;
    }

    private void filterProperties(final String search)
    {
        final List<Widget> selection = editor.getWidgetSelectionHandler().getSelection();

        if (search == null || search.trim().isEmpty())
        {
            setSelectedWidgets(selection);
            pinSearchButton.setSelected(false);
            pinSearchButton.setDisable(true);
        }
        else
        {

            pinSearchButton.setDisable(false);

            List<Widget> other;
            Set<WidgetProperty<?>> properties;
            final DisplayModel model = editor.getModel();

            if (selection.isEmpty())
            {
                if (model != null)
                {
                    other = Collections.emptyList();
                    properties = model.getProperties();
                }
                else
                    return;
            }
            else
            {
                other = new ArrayList<>(selection);
                final Widget primary = other.remove(0);
                properties = commonProperties(primary, other);
            }

            // Filter properties, but preserve order (LinkedHashSet)
            final Set<WidgetProperty<?>> filtered = new LinkedHashSet<>();
            for (WidgetProperty<?> prop : properties)
                if (prop.getDescription().toLowerCase().contains(search.toLowerCase()))
                    filtered.add(prop);

            updatePropertiesView(filtered, other);

        }
    }

    /** Populate UI with properties of widgets
     *  @param widgets Widgets to configure
     */
    private void setSelectedWidgets(final List<Widget> widgets)
    {
        final DisplayModel model = editor.getModel();

        if (widgets.isEmpty())
        {   // Use the DisplayModel
            if (model != null)
                updatePropertiesView(model.getProperties(), Collections.emptyList());
        }
        else
        {   // Determine common properties
            final List<Widget> other = new ArrayList<>(widgets);
            final Widget primary = other.remove(0);
            final Set<WidgetProperty<?>> properties = commonProperties(primary, other);

            updatePropertiesView(properties, other);
        }
    }

    private void updatePropertiesView(final Set<WidgetProperty<?>> properties, final List<Widget> other)
    {
        final DisplayModel model = editor.getModel();

        section.clear();
        section.setClassMode(model != null && model.isClassModel());
        section.fill(editor.getUndoableActionManager(), properties, other);
    }
}
