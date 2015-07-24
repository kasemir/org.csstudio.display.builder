/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.tree;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.util.WidgetIcons;
import org.csstudio.display.builder.model.ContainerWidget;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Tree view of widget hierarchy
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetTree
{
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final TreeView<String> tree_view = new TreeView<>();

    private DisplayModel model;

    public Node create()
    {
        final VBox box = new VBox();

        final Label header = new Label("Widgets");
        header.setMaxWidth(Double.MAX_VALUE);
        header.getStyleClass().add("header");

        tree_view.setShowRoot(false);

        VBox.setVgrow(tree_view, Priority.ALWAYS);
        box.getChildren().addAll(header, tree_view);

        return box;
    }

    // TODO Replace with selection listener
    public void setModel(final DisplayModel model)
    {
        this.model = model;
        model.addPropertyListener(ContainerWidget.CHILDREN_PROPERTY_DESCRIPTOR, this::treeChanged);
        treeChanged(null);
    }

    private void treeChanged(final PropertyChangeEvent event)
    {
        // Always move to non-UI thread, in case we're called on UI thread
        ForkJoinPool.commonPool().execute(() ->
        {   // Using FJPool as plain executor, not dividing tree generation into sub-tasks
            final TreeItem<String> root = new TreeItem<String>(model.getName());
            addWidgets(root, model);
            root.setExpanded(true);

            logger.log(Level.FINE, "Computed new tree on {0}, updating UI", Thread.currentThread().getName());
            Platform.runLater(() -> tree_view.setRoot(root));
        });
    }

    /** Recursively create JavaFX {@link TreeItem}s for model widgets
     *  @param parent Parent tree item
     *  @param container Widgets to add
     */
    private void addWidgets(final TreeItem<String> parent, final ContainerWidget container)
    {
        for (Widget widget : container.getChildren())
        {
            final String type = widget.getType();
            final TreeItem<String> item = new TreeItem<String>(type + " '" + widget.getName());
            item.setExpanded(true);
            final Image icon = WidgetIcons.getIcon(type);
            if (icon != null)
                item.setGraphic(new ImageView(icon));
            parent.getChildren().add(item);
            if (widget instanceof ContainerWidget)
                addWidgets(item, (ContainerWidget) widget);
        }
    }
}
