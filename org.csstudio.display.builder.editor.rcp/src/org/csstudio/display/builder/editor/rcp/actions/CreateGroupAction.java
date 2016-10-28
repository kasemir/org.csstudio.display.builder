/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import java.util.List;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.undo.AddWidgetAction;
import org.csstudio.display.builder.editor.undo.UpdateWidgetLocationAction;
import org.csstudio.display.builder.editor.util.GeometryTools;
import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.ModelPlugin;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import javafx.geometry.Rectangle2D;

public class CreateGroupAction extends Action
{
    private final DisplayEditor editor;
    private final List<Widget> widgets;

    public CreateGroupAction(final DisplayEditor editor, final List<Widget> widgets)
    {
        super("Create Group",
              AbstractUIPlugin.imageDescriptorFromPlugin(ModelPlugin.ID, "icons/group.png"));
        this.editor = editor;
        this.widgets = widgets;
    }

    @Override
    public void run()
    {
        editor.getWidgetSelectionHandler().clear();

        final GroupWidget group = new GroupWidget();

        // Create group that surrounds the original widget boundaries
        final Rectangle2D rect = GeometryTools.getDisplayBounds(widgets);

        // XXX Inset depends on representation and changes with group title, font, ...
        final int inset = 16;

        group.propX().setValue((int) rect.getMinX() - inset);
        group.propY().setValue((int) rect.getMinY() - inset);
        group.propWidth().setValue((int) rect.getWidth() + 2*inset);
        group.propHeight().setValue((int) rect.getHeight() + 2*inset);

        final ChildrenProperty orig_parent_children = ChildrenProperty.getParentsChildren(widgets.get(0));

        // TODO Widget tree (outline) doesn't show the widgets inside new group

        // Add group
        final UndoableActionManager undo = editor.getUndoableActionManager();
        undo.execute(new AddWidgetAction(orig_parent_children, group));

        // Move all widgets into the group
        for (Widget widget : widgets)
        {
            final int orig_x = widget.propX().getValue();
            final int orig_y = widget.propY().getValue();
            final int orig_width = widget.propWidth().getValue();
            final int orig_height = widget.propHeight().getValue();

            orig_parent_children.removeChild(widget);
            group.runtimePropChildren().addChild(widget);

            // Position widget within the group
            widget.propX().setValue((int) (orig_x - rect.getMinX()));
            widget.propY().setValue((int) (orig_y - rect.getMinY()));

            undo.add(new UpdateWidgetLocationAction(widget, orig_parent_children,
                                                    group.runtimePropChildren(),
                                                    orig_x, orig_y, orig_width, orig_height));
        }
    }
}
