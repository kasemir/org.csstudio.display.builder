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
import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.widgets.TabWidget;
import org.csstudio.display.builder.model.widgets.TabWidget.TabItemProperty;

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

    private final TreeView<WidgetOrTab> tree_view = new TreeView<>();

    private DisplayModel model = null;

    /** Map model widgets to their tree items in <code>tree_view</code>
     *
     *  <p>When model notifies about changed Widget,
     *  this map provides the corresponding TreeItem.
     */
    private final Map<Widget, TreeItem<WidgetOrTab>> widget2tree = new ConcurrentHashMap<>();

    /** Map of tab's name.. to TreeItem */
    private final Map<WidgetProperty<String>, TreeItem<WidgetOrTab>> tab_name2tree = new ConcurrentHashMap<>();

    /** Listener to changes in Widget's children */
    private final WidgetPropertyListener<List<Widget>> children_listener;

    /** Listener to changes in Widget's children */
    private final WidgetPropertyListener<String> name_listener = (property, old, new_name) ->
    {
        final Widget widget = property.getWidget();
        logger.log(Level.FINE, "{0} changed name", widget);

        final TreeItem<WidgetOrTab> item = Objects.requireNonNull(widget2tree.get(widget));
        // 'setValue' triggers a refresh of the item,
        // but only if value is different..
        Platform.runLater(() ->
        {
            item.setValue(null);
            item.setValue(WidgetOrTab.of(widget));
        });
    };

    /** Listener to changes in a TabWidget's tabs */
    private final WidgetPropertyListener<List<TabItemProperty>> tabs_property_listener = (tabs, removed, added) ->
    {
        if (added != null)
            addTabs(added);
        if (removed != null)
            removeTabs(removed);
    };

    /** Update the name of a tab item in the tree */
    private final WidgetPropertyListener<String> tab_name_listener = (tab_name, old_name, new_name) ->
    {
        final TreeItem<WidgetOrTab> tab_item = Objects.requireNonNull(tab_name2tree.get(tab_name));
        final WidgetOrTab wot = tab_item.getValue();
        tab_item.setValue(null);
        tab_item.setValue(wot);
    };

    /** Tree cell that displays {@link Widget} (name, icon, ..) */
    private static class WidgetTreeCell extends TreeCell<WidgetOrTab>
    {
        @Override
        public void updateItem(final WidgetOrTab item, final boolean empty)
        {
            super.updateItem(item, empty);
            if (empty || item == null)
            {
                setText(null);
                setGraphic(null);
            }
            else if (item.isWidget())
            {
                final Widget widget = item.getWidget();
                final String type = widget.getType();
                setText(type + " '" + widget.getName() + "'");
                final Image icon = WidgetIcons.getIcon(type);
                if (icon != null)
                    setGraphic(new ImageView(icon));
            }
            else
            {
                setText(item.getTab().name().getValue());
                setGraphic(null);
            }
        }
    };

    /** Cell factory that displays {@link WidgetOrTab} info in tree cell */
    private final Callback<TreeView<WidgetOrTab>, TreeCell<WidgetOrTab>> cell_factory = cell ->  new WidgetTreeCell();


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
                            addWidget(added_widget);
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
        final ObservableList<TreeItem<WidgetOrTab>> tree_selection = tree_view.getSelectionModel().getSelectedItems();
        InvalidationListener listener = (Observable observable) ->
        {
            if (! active.compareAndSet(false, true))
                return;
            try
            {
                final List<Widget> widgets = new ArrayList<>(tree_selection.size());

                for (TreeItem<WidgetOrTab> item : tree_selection)
                {
                    final WidgetOrTab wot = item.getValue();
                    final Widget widget = wot.isWidget()
                        ? wot.getWidget()
                        : wot.getTab().getWidget();
                    if (! widgets.contains(widget))
                        widgets.add(widget);
                };
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
            widget2tree.clear();
            tab_name2tree.clear();
        }
        this.model = model;

        // Might be called on UI thread, move off
        EditorUtil.getExecutor().execute(() ->
        {
            final TreeItem<WidgetOrTab> root = new TreeItem<WidgetOrTab>(WidgetOrTab.of(model));
            if (model != null)
            {
                widget2tree.put(model, root);
                for (Widget widget : model.runtimeChildren().getValue())
                    addWidget(widget);
                root.setExpanded(true);
                model.runtimeChildren().addPropertyListener(children_listener);
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
            final MultipleSelectionModel<TreeItem<WidgetOrTab>> selection = tree_view.getSelectionModel();
            selection.clearSelection();
            for (Widget widget : widgets)
                selection.select(widget2tree.get(widget));
        }
        finally
        {
            active.set(false);
        }
    }

    /** Add widget to existing model & tree
     *  @param added_widget Widget to add
     */
    private void addWidget(final Widget added_widget)
    {   // Determine location of widget within parent of model
        final Widget widget_parent = added_widget.getParent().get();
        int index = -1;
        TreeItem<WidgetOrTab> item_parent = null;
        if (widget_parent instanceof TabWidget)
        {
            // TODO Track changes to the tabs & their children
            for (TabItemProperty tab : ((TabWidget)widget_parent).displayTabs().getValue())
            {
                index = tab.children().getValue().indexOf(added_widget);
                if (index >= 0)
                {
                    item_parent = tab_name2tree.get(tab.name());
                    break;
                }
            }
        }
        else
        {
            index = ChildrenProperty.getChildren(widget_parent).getValue().indexOf(added_widget);
            item_parent = widget2tree.get(widget_parent);
        }

        Objects.requireNonNull(item_parent, "Cannot obtain parent item for " + added_widget);

        // Create Tree item, add at same index into Tree
        final TreeItem<WidgetOrTab> item = new TreeItem<>(WidgetOrTab.of(added_widget));
        widget2tree.put(added_widget, item);
        item.setExpanded(true);
        item_parent.getChildren().add(index, item);

        added_widget.widgetName().addPropertyListener(name_listener);

        final ChildrenProperty children = ChildrenProperty.getChildren(added_widget);
        if (children != null)
        {
            children.addPropertyListener(children_listener);
            for (Widget child : children.getValue())
                addWidget(child);
        }

        if (added_widget instanceof TabWidget)
        {
            final ArrayWidgetProperty<TabItemProperty> tabs = ((TabWidget)added_widget).displayTabs();
            addTabs(tabs.getValue());
            tabs.addPropertyListener(tabs_property_listener);
        }
    }

    private void addTabs(final List<TabItemProperty> added)
    {
        for (TabItemProperty tab : added)
        {
            final TreeItem<WidgetOrTab> widget_item = widget2tree.get(tab.getWidget());
            final TreeItem<WidgetOrTab> tab_item = new TreeItem<>(WidgetOrTab.of(tab));
            widget_item.getChildren().add(tab_item);
            tab_name2tree.put(tab.name(), tab_item);
            tab.name().addPropertyListener(tab_name_listener);

            for (Widget child : tab.children().getValue())
                addWidget(child);
            // TODO tab.children().addPropertyListener(tab_children_listener);
        }
    }

    private void removeTabs(final List<TabItemProperty> removed)
    {
        for (TabItemProperty tab : removed)
        {
            // TODO tab.children().removePropertyListener(tab_children_listener);
            tab.name().removePropertyListener(tab_name_listener);
            final TreeItem<WidgetOrTab> tab_item = tab_name2tree.remove(tab.name());
            tab_item.getParent().getChildren().remove(tab_item);
        }
    }

    /** Remove widget from existing model & tree
     *  @param removed_widget
     */
    private void removeWidget(final Widget removed_widget)
    {
        if (removed_widget instanceof TabWidget)
        {
            final ArrayWidgetProperty<TabItemProperty> tabs = ((TabWidget)removed_widget).displayTabs();
            tabs.removePropertyListener(tabs_property_listener);
            removeTabs(tabs.getValue());
        }

        removed_widget.widgetName().removePropertyListener(name_listener);

        final ChildrenProperty children = ChildrenProperty.getChildren(removed_widget);
        if (children != null)
        {
            children.removePropertyListener(children_listener);
            for (Widget child : children.getValue())
                removeWidget(child);
        }

        final TreeItem<WidgetOrTab> item = widget2tree.remove(removed_widget);
        item.getParent().getChildren().remove(item);
    }

    /** Recursively remove model widget listeners
     *  @param container Widgets to unlink
     */
    private void removeWidgetListeners(final Widget widget)
    {
        if (widget instanceof TabWidget)
        {
            final ArrayWidgetProperty<TabItemProperty> tabs = ((TabWidget)widget).displayTabs();
            tabs.removePropertyListener(tabs_property_listener);
            for (TabItemProperty tab : tabs.getValue())
            {
                // TODO tab.children().removePropertyListener(tab_children_listener);
                tab.name().removePropertyListener(tab_name_listener);
            }
        }

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
