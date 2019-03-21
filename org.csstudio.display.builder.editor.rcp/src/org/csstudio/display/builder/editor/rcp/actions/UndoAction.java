/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.rcp.Messages;
import org.csstudio.display.builder.editor.undo.AddWidgetAction;
import org.csstudio.display.builder.editor.undo.GroupWidgetsAction;
import org.csstudio.display.builder.editor.undo.UnGroupWidgetsAction;
import org.csstudio.display.builder.util.undo.UndoableAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/** Action to un-do last operation
 *  @author Kay Kasemir
 */
public class UndoAction extends RetargetActionHandler
{
    public UndoAction(final DisplayEditor editor)
    {
        super(editor, ActionFactory.UNDO.getCommandId());
        setText(Messages.Undo);
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
        setEnabled(editor.getUndoableActionManager().canUndo());
    }

    @Override
    public void run()
    {
        final UndoableAction action = editor.getUndoableActionManager().undoLast();
        if (action instanceof AddWidgetAction  ||
            action instanceof GroupWidgetsAction ||
            action instanceof UnGroupWidgetsAction)
            editor.getWidgetSelectionHandler().clear();
    }
}
