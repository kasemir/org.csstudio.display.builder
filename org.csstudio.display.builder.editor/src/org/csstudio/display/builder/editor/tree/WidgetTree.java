/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.tree;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.editor.util.WidgetIcons;
import org.csstudio.display.builder.model.ContainerWidget;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;

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
// TODO Handle changed model: Unsubscribe from previous model
// TODO Fix bug:
//      Group, subgroup, LED
//      Use drag/drop to duplicate the subgroup-with-LED inside the group.
//      Model is OK, but tree fails to represent the LED inside the new subgroup.
@SuppressWarnings("nls")
public class WidgetTree
{
    private final Logger logger = Logger.getLogger(getClass().getName());

    /** Is this class updating the selection of tree or model? */
    private final AtomicBoolean active = new AtomicBoolean();

    /** Handler for setting and tracking the currently selected widgets */
    private final WidgetSelectionHandler selection;

    private final TreeView<Widget> tree_view = new TreeView<>();

    /** Map model widgets to their tree items in <code>tree_view</code>
     *
     *  <p>When model notifies about changed Widget,
     *  this map provides the corresponding TreeItem.
     */
    private Map<Widget, TreeItem<Widget>> widget_items;

    /** Listener to changes in ContainerWidget's children */
    private final PropertyChangeListener children_listener = event ->
    {
        final Widget removed_widget = (Widget) event.getOldValue();
        final Widget added_widget   = (Widget) event.getNewValue();
        // Update must be on UI thread.
        // Even if already on UI thread, decouple.
        Platform.runLater(() ->
        {
            if (removed_widget != null)
                removeWidget(removed_widget);
            if (added_widget != null)
                addWidget(added_widget);
        });
    };

    /** Listener to changes in ContainerWidget's children */
    private final PropertyChangeListener name_listener = event ->
    {
        final Widget widget = (Widget) event.getSource();
        logger.log(Level.FINE, "{0} changed name", widget);

        final TreeItem<Widget> item = widget_items.get(widget);
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
        // Might be called on UI thread, move off
        ForkJoinPool.commonPool().execute(() ->
        {   // Using FJPool as plain executor, not dividing tree generation into sub-tasks
            final TreeItem<Widget> root = new TreeItem<Widget>(model);
            final Map<Widget, TreeItem<Widget>> widget_items = new HashMap<>();
            widget_items.put(model, root);
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

            widget.addPropertyListener(CommonWidgetProperties.widgetName, name_listener);

            if (widget instanceof ContainerWidget)
                addWidgets(item, (ContainerWidget) widget, widget_items);
        }
        container.addPropertyListener(ContainerWidget.CHILDREN_PROPERTY_DESCRIPTOR, children_listener);
    }

    private void addWidget(final Widget added_widget)
    {
        final TreeItem<Widget> item_parent = widget_items.get(added_widget.getParent().get());
        final TreeItem<Widget> item = new TreeItem<>(added_widget);
        widget_items.put(added_widget, item);
        item.setExpanded(true);
        item_parent.getChildren().add(item);

        added_widget.addPropertyListener(CommonWidgetProperties.widgetName, name_listener);

        if (added_widget instanceof ContainerWidget)
            added_widget.addPropertyListener(ContainerWidget.CHILDREN_PROPERTY_DESCRIPTOR, children_listener);
    }

    private void removeWidget(final Widget removed_widget)
    {
        removed_widget.removePropertyListener(CommonWidgetProperties.widgetName, name_listener);

        final TreeItem<Widget> item = widget_items.get(removed_widget);
        item.getParent().getChildren().remove(item);
        widget_items.remove(removed_widget);

        if (removed_widget instanceof ContainerWidget)
            removed_widget.removePropertyListener(ContainerWidget.CHILDREN_PROPERTY_DESCRIPTOR, children_listener);
    }
}
