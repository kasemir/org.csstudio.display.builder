/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetFactory;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

/** Palette of all available widgets
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Palette
{
    // TODO Allow 'dragging' new widgets into the editor
    public Node create()
    {
        final VBox palette = new VBox();

        final Label header = new Label("Palette");
        header.setMaxWidth(Double.MAX_VALUE);
        header.getStyleClass().add("header");
        palette.getChildren().add(header);

        final Map<WidgetCategory, Pane> palette_groups = new HashMap<>();
        for (final WidgetCategory category : WidgetCategory.values())
        {
            final TilePane palette_group = new TilePane();
            palette_group.getStyleClass().add("palette_group");
            palette_group.setPrefColumns(1);
            palette_group.setMaxWidth(Double.MAX_VALUE);
            palette_groups.put(category, palette_group);
            final TitledPane pane = new TitledPane(category.getDescription(), palette_group);
            pane.getStyleClass().add("palette_category");
            palette.getChildren().add(pane);
        }
        for (final WidgetDescriptor desc : WidgetFactory.getInstance().getWidgetDescriptions())
        {
            final Button button = new Button(desc.getName());
            try
            {
                button.setGraphic(new ImageView(new Image(desc.getIconStream())));
            }
            catch (final Exception ex)
            {
                Logger.getLogger(getClass().getName())
                      .log(Level.WARNING, "Icon failed for " + desc, ex);
            }
            button.setPrefWidth(150);
            button.setAlignment(Pos.BASELINE_LEFT);
            palette_groups.get(desc.getCategory()).getChildren().add(button);
        }

        final ScrollPane palette_scroll = new ScrollPane(palette);
        palette_scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        return palette_scroll;
    }
}
