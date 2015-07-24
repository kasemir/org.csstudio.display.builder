/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.tree;

import java.beans.PropertyChangeEvent;
import java.util.List;
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

    final private Callback<TreeView<Widget>, TreeCell<Widget>> cell_factory =
        new Callback<TreeView<Widget>, TreeCell<Widget>>()
    {
        @Override
        public TreeCell<Widget> call(final TreeView<Widget> param)
        {
            return new TreeCell<Widget>()
            {
                @Override
                public void updateItem(Widget widget, boolean empty)
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
        }
    };


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
            final TreeItem<Widget> root = new TreeItem<Widget>(model);
            addWidgets(root, model);
            root.setExpanded(true);

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
     */
    private void addWidgets(final TreeItem<Widget> parent, final ContainerWidget container)
    {
        for (Widget widget : container.getChildren())
        {
            final String type = widget.getType();
            final TreeItem<Widget> item = new TreeItem<>(widget);
            item.setExpanded(true);
            parent.getChildren().add(item);
            if (widget instanceof ContainerWidget)
                addWidgets(item, (ContainerWidget) widget);
        }
    }

    public void setSelectedWidgets(final List<Widget> widgets)
    {
        // TODO Show selected widgets
        System.out.println("To select in tree: " + widgets);
    }
}
