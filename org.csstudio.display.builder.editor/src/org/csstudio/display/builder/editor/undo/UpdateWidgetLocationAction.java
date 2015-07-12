/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.display.builder.editor.undo;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.Widget;

/** Action to update widget location
 *  @author Kay Kasemir
 */
public class UpdateWidgetLocationAction extends UndoableAction
{
    private final Widget widget;
    private final int orig_x, orig_y, orig_width, orig_height;
    private final int x, y, width, height;

    public UpdateWidgetLocationAction(final Widget widget,
                                      final int orig_x, final int orig_y,
                                      final int orig_width, final int orig_height)
    {
        super(Messages.UpdateWidgetLocation);
        this.widget = widget;
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
        widget.positionX().setValue(x);
        widget.positionY().setValue(y);
        widget.positionWidth().setValue(width);
        widget.positionHeight().setValue(height);
    }

    @Override
    public void undo()
    {
        widget.positionX().setValue(orig_x);
        widget.positionY().setValue(orig_y);
        widget.positionWidth().setValue(orig_width);
        widget.positionHeight().setValue(orig_height);
    }
}
