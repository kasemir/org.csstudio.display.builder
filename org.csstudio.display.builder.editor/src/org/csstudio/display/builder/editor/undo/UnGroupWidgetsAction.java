/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.display.builder.editor.undo;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.util.undo.UndoableAction;

/** Action to un-group widgets
 *  @author Kay Kasemir
 */
public class UnGroupWidgetsAction extends UndoableAction
{
    private final ChildrenProperty parent_children;
    private final GroupWidget group;
    private final List<Widget> widgets;
    private final int x_offset, y_offset;

    public UnGroupWidgetsAction(final GroupWidget group)
    {
        super(Messages.RemoveGroup);
        this.parent_children = ChildrenProperty.getParentsChildren(group);
        this.group = group;
        // Underlying list of group's children changes, so create copy
        this.widgets = new ArrayList<>(group.runtimePropChildren().getValue());
        final int[] insets = group.runtimePropInsets().getValue();
        this.x_offset = group.propX().getValue() + insets[0];
        this.y_offset = group.propY().getValue() + insets[1];
    }

    @Override
    public void run()
    {
        parent_children.removeChild(group);
        for (Widget widget : widgets)
        {
            group.runtimePropChildren().removeChild(widget);
            final int orig_x = widget.propX().getValue();
            final int orig_y = widget.propY().getValue();
            widget.propX().setValue((int) (orig_x + x_offset));
            widget.propY().setValue((int) (orig_y + y_offset));
            parent_children.addChild(widget);
        }
    }

    @Override
    public void undo()
    {
        for (Widget widget : widgets)
        {
            parent_children.removeChild(widget);
            final int orig_x = widget.propX().getValue();
            final int orig_y = widget.propY().getValue();
            widget.propX().setValue((int) (orig_x - x_offset));
            widget.propY().setValue((int) (orig_y - y_offset));
            group.runtimePropChildren().addChild(widget);
        }
        parent_children.addChild(group);
    }
}
