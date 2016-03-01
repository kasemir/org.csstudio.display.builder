/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.EditorUtil;
import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.editor.util.WidgetIcons;
import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetPropertyListener;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
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

    /** Is this class updating the selection of tree or model? */
    private final AtomicBoolean active = new AtomicBoolean();

    /** Handler for setting and tracking the currently selected widgets */
    private final WidgetSelectionHandler selection;

    private final TreeView<Widget> tree_view = new TreeView<>();

    private DisplayModel model = null;

    /** Map model widgets to their tree items in <code>tree_view</code>
     *
     *  <p>When model notifies about changed Widget,
     *  this map provides the corresponding TreeItem.
     */
    private volatile Map<Widget, TreeItem<Widget>> widget_items = new ConcurrentHashMap<>();

    /** Listener to changes in ContainerWidget's children */
    private final WidgetPropertyListener<List<Widget>> children_listener;

    /** Listener to changes in ContainerWidget's children */
    private final WidgetPropertyListener<String> name_listener = (property, old, new_name) ->
    {
        final Widget widget = property.getWidget();
        logger.log(Level.FINE, "{0} changed name", widget);

        final TreeItem<Widget> item = Objects.requireNonNull(widget_items.get(widget));
        // 'setValue' triggers a refresh of the item,
        // but only if value is different..
        Platform.runLater(() ->
        {
            item.setValue(null);
            item.setValue(widget);
        });
    };

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


        children_listener = (p, removed, added) ->
        {
            // Update must be on UI thread.
            // Even if already on UI thread, decouple.
            Platform.runLater(() ->
            {
                active.set(true);
                try
                {
                    if (removed != null)
                        for (Widget removed_widget : removed)
                            removeWidget(removed_widget);
                    if (added != null)
                        for (Widget added_widget : added)
                            addWidget(added_widget, widget_items);
                }
                finally
                {
                    active.set(false);
                }
                // Restore tree's selection to match model
                // after removing/adding items may have changed it.
                setSelectedWidgets(selection.getSelection());
            });
        };
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

        bindSelections();

        return box;
    }

    /** Link selections in tree view and model */
    private void bindSelections()
    {
        // Update selected widgets in model from selection in tree_view
        final ObservableList<TreeItem<Widget>> tree_selection = tree_view.getSelectionModel().getSelectedItems();
        InvalidationListener listener = (Observable observable) ->
        {
            if (! active.compareAndSet(false, true))
                return;
            try
            {
                final List<Widget> widgets = new ArrayList<>(tree_selection.size());
                tree_selection.forEach(item -> widgets.add(item.getValue()));
                logger.log(Level.FINE, "Selected in tree: {0}", widgets);
                selection.setSelection(widgets);
            }
            finally
            {
                active.set(false);
            }
        };
        tree_selection.addListener(listener);

        // Update selection in tree_view from selected widgets in model
        selection.addListener(this::setSelectedWidgets);
    }

    /** @param model Model to display as widget tree */
    public void setModel(final DisplayModel model)
    {
        // Could recursively remove all old model tree elements,
        // on UI thread, one by one.
        // Faster: Unlink listeners and then replace the whole
        // tree model which was created in background.
        final DisplayModel old_model = this.model;
        if (old_model != null)
        {
            old_model.runtimeChildren().removePropertyListener(children_listener);
            for (Widget widget : old_model.runtimeChildren().getValue())
                removeWidgetListeners(widget);
        }
        this.model = model;

        // Might be called on UI thread, move off
        EditorUtil.getExecutor().execute(() ->
        {
            final TreeItem<Widget> root = new TreeItem<Widget>(model);
            final Map<Widget, TreeItem<Widget>> widget_items = new ConcurrentHashMap<>();
            if (model != null)
            {
                widget_items.put(model, root);
                for (Widget widget : model.runtimeChildren().getValue())
                    addWidget(widget, widget_items);
                root.setExpanded(true);
                model.runtimeChildren().addPropertyListener(children_listener);
                this.widget_items = widget_items;
            }
            logger.log(Level.FINE, "Computed new tree on {0}, updating UI", Thread.currentThread().getName());
            Platform.runLater(() ->
            {
                tree_view.setRoot(root);
                setSelectedWidgets(selection.getSelection());
            });
        });
    }

    /** Called by selection handler when selected widgets have changed, or on new model
     *  @param widgets Widgets to select in tree
     */
    public void setSelectedWidgets(final List<Widget> widgets)
    {
        if (! active.compareAndSet(false, true))
            return;
        try
        {
            final MultipleSelectionModel<TreeItem<Widget>> selection = tree_view.getSelectionModel();
            selection.clearSelection();
            for (Widget widget : widgets)
                selection.select(widget_items.get(widget));
        }
        finally
        {
            active.set(false);
        }
    }

    /** Add widget to existing model & tree
     *  @param added_widget Widget to add
     *  @param widget_items Map of widget to tree item
     */
    private void addWidget(final Widget added_widget, final Map<Widget, TreeItem<Widget>> widget_items)
    {   // Determine location of widget within parent of model
        final Widget widget_parent = added_widget.getParent().get();
        final int index = ChildrenProperty.getChildren(widget_parent).getValue().indexOf(added_widget);

        // Create Tree item, add at same index into Tree
        final TreeItem<Widget> item_parent = widget_items.get(widget_parent);
        final TreeItem<Widget> item = new TreeItem<>(added_widget);
        widget_items.put(added_widget, item);
        item.setExpanded(true);
        item_parent.getChildren().add(index, item);

        added_widget.widgetName().addPropertyListener(name_listener);

        final ChildrenProperty children = ChildrenProperty.getChildren(added_widget);
        if (children != null)
        {
            children.addPropertyListener(children_listener);
            for (Widget child : children.getValue())
                addWidget(child, widget_items);
        }
    }

    /** Remove widget from existing model & tree
     *  @param removed_widget
     */
    private void removeWidget(final Widget removed_widget)
    {
        removed_widget.widgetName().removePropertyListener(name_listener);

        final ChildrenProperty children = ChildrenProperty.getChildren(removed_widget);
        if (children != null)
        {
            children.removePropertyListener(children_listener);
            for (Widget child : children.getValue())
                removeWidget(child);
        }

        final TreeItem<Widget> item = widget_items.get(removed_widget);
        item.getParent().getChildren().remove(item);
        widget_items.remove(removed_widget);
    }

    /** Recursively remove model widget listeners
     *  @param container Widgets to unlink
     */
    private void removeWidgetListeners(final Widget widget)
    {
        widget.widgetName().removePropertyListener(name_listener);
        final ChildrenProperty children = ChildrenProperty.getChildren(widget);
        if (children != null)
        {
            children.removePropertyListener(children_listener);
            for (Widget child : children.getValue())
                removeWidgetListeners(child);
        }
    }
}
