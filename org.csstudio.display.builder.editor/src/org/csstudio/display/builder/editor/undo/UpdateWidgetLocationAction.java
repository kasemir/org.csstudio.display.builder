/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.display.builder.editor.undo;

import java.util.Objects;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.util.undo.UndoableAction;

/** Action to update widget location
 *  @author Kay Kasemir
 */
public class UpdateWidgetLocationAction extends UndoableAction
{
    private final Widget widget;
    final ChildrenProperty orig_parent_children, parent_children;
    private final int orig_x, orig_y, orig_width, orig_height;
    private final int x, y, width, height;

    /** @param widget      Widget that already has desired parent, location, size
     *  @param orig_parent Original parent (may be the same as now)
     *  @param orig_x      Original location
     *  @param orig_y      ..
     *  @param orig_width  .. and size
     *  @param orig_height
     */
    public UpdateWidgetLocationAction(final Widget widget,
                                      final ChildrenProperty orig_parent_children,
                                      final ChildrenProperty parent_children,
                                      final int orig_x, final int orig_y,
                                      final int orig_width, final int orig_height)
    {
        super(Messages.UpdateWidgetLocation);
        this.widget = Objects.requireNonNull(widget);
        this.orig_parent_children = Objects.requireNonNull(orig_parent_children);
        this.parent_children = Objects.requireNonNull(parent_children);
        this.orig_x = orig_x;
        this.orig_y = orig_y;
        this.orig_width = orig_width;
        this.orig_height = orig_height;
        x = widget.positionX().getValue();
        y = widget.positionY().getValue();
        width = widget.positionWidth().getValue();
        height = widget.positionHeight().getValue();
    }

    @Override
    public void run()
    {
        if (orig_parent_children != parent_children)
        {
            orig_parent_children.removeChild(widget);
            parent_children.addChild(widget);
        }
        widget.positionX().setValue(x);
        widget.positionY().setValue(y);
        widget.positionWidth().setValue(width);
        widget.positionHeight().setValue(height);
    }

    @Override
    public void undo()
    {
        if (orig_parent_children != parent_children)
        {
            parent_children.removeChild(widget);
            orig_parent_children.addChild(widget);
        }
        widget.positionX().setValue(orig_x);
        widget.positionY().setValue(orig_y);
        widget.positionWidth().setValue(orig_width);
        widget.positionHeight().setValue(orig_height);
    }
}
