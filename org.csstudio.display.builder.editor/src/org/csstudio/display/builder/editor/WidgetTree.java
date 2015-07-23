/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.beans.PropertyChangeEvent;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.ContainerWidget;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetFactory;

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

    public WidgetTree()
    {
    }

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

    public void setModel(final DisplayModel model)
    {
        this.model = model;
        model.addPropertyListener(ContainerWidget.CHILDREN_PROPERTY_DESCRIPTOR, this::treeChanged);
        treeChanged(null);
    }

    private void treeChanged(final PropertyChangeEvent event)
    {
        final TreeItem<String> root = new TreeItem<String>(model.getName());
        addWidgets(root, model);
        root.setExpanded(true);
        // TODO Tree does not show until manual expand/collapse.
        // See this on using FXCollections.observableArrayList(treeItems)
        // http://stackoverflow.com/questions/21911773/javafx-weird-behavior-of-treeview-on-removal-of-the-selected-item
        tree_view.setRoot(root);
    }

    private void addWidgets(final TreeItem<String> parent, final ContainerWidget container)
    {
        for (Widget widget : container.getChildren())
        {
            final TreeItem<String> item = new TreeItem<String>(widget.getType() + " '" + widget.getName());
            item.setExpanded(true);
            final Optional<WidgetDescriptor> descriptor = WidgetFactory.getInstance().getWidgetDescriptor(widget.getType());
            try
            {
                if (descriptor.isPresent())
                    item.setGraphic(new ImageView(new Image(descriptor.get().getIconStream())));
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot obtain widget for " + widget, ex);
            }
            parent.getChildren().add(item);
            if (widget instanceof ContainerWidget)
                addWidgets(item, (ContainerWidget) widget);
        }
    }
}
