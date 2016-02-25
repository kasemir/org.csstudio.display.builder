/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.display.builder.editor.undo;

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
    private final Widget orig_parent, parent;
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
                                      final Widget orig_parent,
                                      final int orig_x, final int orig_y,
                                      final int orig_width, final int orig_height)
    {
        super(Messages.UpdateWidgetLocation);
        this.widget = widget;
        this.orig_parent = orig_parent;
        this.orig_x = orig_x;
        this.orig_y = orig_y;
        this.orig_width = orig_width;
        this.orig_height = orig_height;
        parent = widget.getParent().get();
        x = widget.positionX().getValue();
        y = widget.positionY().getValue();
        width = widget.positionWidth().getValue();
        height = widget.positionHeight().getValue();
    }

    @Override
    public void run()
    {
        final Widget current_parent = widget.getParent().get();
        if (parent != current_parent)
        {
            ChildrenProperty.getChildren(current_parent).removeChild(widget);
            ChildrenProperty.getChildren(parent).addChild(widget);
        }
        widget.positionX().setValue(x);
        widget.positionY().setValue(y);
        widget.positionWidth().setValue(width);
        widget.positionHeight().setValue(height);
    }

    @Override
    public void undo()
    {
        final Widget current_parent = widget.getParent().get();
        if (orig_parent != current_parent)
        {
            ChildrenProperty.getChildren(current_parent).removeChild(widget);
            ChildrenProperty.getChildren(orig_parent).addChild(widget);
        }
        widget.positionX().setValue(orig_x);
        widget.positionY().setValue(orig_y);
        widget.positionWidth().setValue(orig_width);
        widget.positionHeight().setValue(orig_height);
    }
}
