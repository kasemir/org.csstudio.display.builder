/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.eclipse.jface.action.Action;

/** Action to un-do last operation
 *  @author Kay Kasemir
 */
public class UndoAction extends Action
{   // Used as handler for RetargetAction, so no need for label, icon
    private final UndoableActionManager manager;

    public UndoAction(final UndoableActionManager manager)
    {
        this.manager = manager;
        setEnabled(manager.canUndo());
    }

    @Override
    public void run()
    {
        manager.undoLast();
    }
}
