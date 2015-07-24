/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.tree;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.editor.util.WidgetIcons;
import org.csstudio.display.builder.model.ContainerWidget;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/** Tree view of widget hierarchy
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetTree
{
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final WidgetSelectionHandler selection;

    private final TreeView<Widget> tree_view = new TreeView<>();

    private DisplayModel model;

    /** Map model widgets to their tree items in <code>tree_view</code> */
    // Used to select widgets in tree since API needs the TreeItems to select
    // while we have the Widgets to select.
    private Map<Widget, TreeItem<Widget>> widget_items;

    /** Tree cell that displays {@link Widget} (name, icon, ..) */
    private static class WidgetTreeCell extends TreeCell<Widget>
    {
        @Override
        public void updateItem(final Widget widget, final boolean empty)
        {
            super.updateItem(widget, empty);
            if (empty || widget == null)
            {
                setText(null);
                setGraphic(null);
            }
            else
            {
                 final String type = widget.getType();
                 setText(type + " '" + widget.getName() + "'");
                 final Image icon = WidgetIcons.getIcon(type);
                 if (icon != null)
                     setGraphic(new ImageView(icon));
            }
        }
    };

    /** Cell factory that displays {@link Widget} info in tree cell */
    private final Callback<TreeView<Widget>, TreeCell<Widget>> cell_factory = (final TreeView<Widget> param) ->  new WidgetTreeCell();

    /** Construct widget tree
     *  @param selection Handler of selected widgets
     */
    public WidgetTree(final WidgetSelectionHandler selection)
    {
        this.selection = selection;
    }

    /** Create UI components
     *  @return Root {@link Node}
     */
    public Node create()
    {
        final VBox box = new VBox();

        final Label header = new Label("Widgets");
        header.setMaxWidth(Double.MAX_VALUE);
        header.getStyleClass().add("header");

        tree_view.setShowRoot(false);
        tree_view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tree_view.setCellFactory(cell_factory);

        VBox.setVgrow(tree_view, Priority.ALWAYS);
        box.getChildren().addAll(header, tree_view);

        selection.addListener(this::setSelectedWidgets);

        return box;
    }

    /** @param model Model to display as widget tree */
    public void setModel(final DisplayModel model)
    {
        this.model = model;
        model.addPropertyListener(ContainerWidget.CHILDREN_PROPERTY_DESCRIPTOR, this::treeChanged);
        treeChanged(null);
    }

    /** Invoked when tree (widgets in model) have changed
     *  @param event Ignored
     */
    private void treeChanged(final PropertyChangeEvent event)
    {
        // Might be called on UI thread, move off
        ForkJoinPool.commonPool().execute(() ->
        {   // Using FJPool as plain executor, not dividing tree generation into sub-tasks
            final TreeItem<Widget> root = new TreeItem<Widget>(model);
            final Map<Widget, TreeItem<Widget>> widget_items = new HashMap<>();
            addWidgets(root, model, widget_items);
            root.setExpanded(true);
            this.widget_items = widget_items;

            logger.log(Level.FINE, "Computed new tree on {0}, updating UI", Thread.currentThread().getName());
            Platform.runLater(() ->
            {
                tree_view.setRoot(root);
                setSelectedWidgets(selection.getSelection());
            });
        });
    }

    /** Recursively create JavaFX {@link TreeItem}s for model widgets
     *  @param parent Parent tree item
     *  @param container Widgets to add
     *  @param widget_items Map for storing TreeItem for each Widget
     */
    private void addWidgets(final TreeItem<Widget> parent, final ContainerWidget container, final Map<Widget, TreeItem<Widget>> widget_items)
    {
        for (Widget widget : container.getChildren())
        {
            final TreeItem<Widget> item = new TreeItem<>(widget);
            widget_items.put(widget, item);
            item.setExpanded(true);
            parent.getChildren().add(item);
            if (widget instanceof ContainerWidget)
                addWidgets(item, (ContainerWidget) widget, widget_items);
        }
    }

    /** Called by selection handler when selected widgets have changed, or on new model
     *  @param widgets Widgets to select in tree
     */
    public void setSelectedWidgets(final List<Widget> widgets)
    {
        final MultipleSelectionModel<TreeItem<Widget>> selection = tree_view.getSelectionModel();
        selection.clearSelection();
        for (Widget widget : widgets)
            selection.select(widget_items.get(widget));
    }
}
