/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.util.List;
import java.util.Set;

import org.csstudio.display.builder.model.MacroizedWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Property GUI
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PropertyGUI
{
    private GridPane grid;

    // TODO Monitor properties for change
    // TODO Allow entering values, then update property
    // TODO Editors based on property type
    public Node create()
    {
        final VBox box = new VBox();

        final Label header = new Label("Properties");
        header.setMaxWidth(Double.MAX_VALUE);
        header.getStyleClass().add("header");
        box.getChildren().add(header);

        grid = new GridPane();
        box.getChildren().add(grid);

        final ScrollPane box_scroll = new ScrollPane(box);
        return box_scroll;
    }

    /** Activate the tracker
     *  @param widgets Widgets to control by tracker
     */
    public void setSelectedWidgets(final List<Widget> widgets)
    {
        grid.getChildren().clear();

        // TODO Get common properties
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
            final TextField value = new TextField();
            if (property instanceof MacroizedWidgetProperty)
                value.setText(String.valueOf(((MacroizedWidgetProperty<?>) property).getSpecification()));
            else
                value.setText(String.valueOf(property.getValue()));
            label.getStyleClass().add("property_name");
            value.getStyleClass().add("property_value");

            grid.add(label, 0, row);
            grid.add(value, 1, row++);
        }
    }
}
