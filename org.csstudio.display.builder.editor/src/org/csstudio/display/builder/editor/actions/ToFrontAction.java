/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.actions;

import java.util.List;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.editor.undo.UpdateWidgetOrderAction;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.util.undo.UndoableActionManager;

/** Action to move widget to front
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ToFrontAction extends ActionDescription
{
    private final UndoableActionManager undo;
    private final WidgetSelectionHandler selection_handler;

    public ToFrontAction(final UndoableActionManager undo, final WidgetSelectionHandler selection_handler)
    {
        super("platform:/plugin/org.csstudio.display.builder.editor/icons/tofront.png", Messages.MoveToFront);
        this.undo = undo;
        this.selection_handler = selection_handler;
    }

    @Override
    public void run(final boolean selected)
    {
        final List<Widget> widgets = selection_handler.getSelection();
        for (Widget widget : widgets)
            undo.execute(new UpdateWidgetOrderAction(widget, -1));
    }
}
