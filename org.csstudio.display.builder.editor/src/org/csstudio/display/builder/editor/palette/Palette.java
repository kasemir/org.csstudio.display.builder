/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.palette;

import java.util.HashMap;
import java.util.Map;

import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.editor.util.WidgetIcons;
import org.csstudio.display.builder.editor.util.WidgetTransfer;
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
import javafx.scene.control.Tooltip;
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
    // TODO better auto-sizing
    // All palette entries should have the same size,
    // but since each Button is in a separate TilePane,
    // it's not obvious how to get them to the same size
    // unless that's set to a fixed pixel value.
    private final static int PREFERRED_WIDTH = 180;

    private final WidgetSelectionHandler selection;

    /** @param selection Selection handler */
    public Palette(final WidgetSelectionHandler selection)
    {
        this.selection = selection;
    }

    /** Create UI elements
     *  @return Top-level Node of the UI
     */
    public Node create()
    {
        final VBox palette = new VBox();

        final Label header = new Label("Palette");
        header.setMaxWidth(Double.MAX_VALUE);
        header.getStyleClass().add("header");
        palette.getChildren().add(header);

        final Map<WidgetCategory, Pane> palette_groups = createWidgetCategoryPanes(palette);
        createWidgetEntries(palette_groups);

        final ScrollPane palette_scroll = new ScrollPane(palette);
        palette_scroll.setHbarPolicy(ScrollBarPolicy.NEVER);

        // TODO Determine the correct size for the main node
        // Using 2*PREFERRED_WIDTH was determined by trial and error
        palette_scroll.setPrefWidth(2*PREFERRED_WIDTH);
        return palette_scroll;
    }

    /** Create a TilePane for each WidgetCategory
     *  @param parent Parent Pane
     *  @return Map of panes for each category
     */
    private Map<WidgetCategory, Pane> createWidgetCategoryPanes(final Pane parent)
    {
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
            parent.getChildren().add(pane);
        }
        return palette_groups;
    }

    /** Create entry for each widget type
      * @param palette_groups Map with parent panes for each widget category
     */
    private void createWidgetEntries(final Map<WidgetCategory, Pane> palette_groups)
    {
        for (final WidgetDescriptor desc : WidgetFactory.getInstance().getWidgetDescriptions())
        {
            final Button button = new Button(desc.getName());
            final Image icon = WidgetIcons.getIcon(desc.getType());
            if (icon != null)
                button.setGraphic(new ImageView(icon));
            button.setPrefWidth(PREFERRED_WIDTH);
            button.setAlignment(Pos.BASELINE_LEFT);
            button.setTooltip(new Tooltip(desc.getDescription()));
            palette_groups.get(desc.getCategory()).getChildren().add(button);

            WidgetTransfer.addDragSupport(button, selection, desc, icon);
        }
    }
}
