/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.eclipse.jface.action.Action;

/** Action to re-do last operation
 *  @author Kay Kasemir
 */
public class RedoAction extends Action
{   // Used as handler for RetargetAction, so no need for label, icon
    private final UndoableActionManager manager;

    public RedoAction(final UndoableActionManager manager)
    {
        this.manager = manager;
        setEnabled(manager.canRedo());
    }

    @Override
    public void run()
    {
        manager.redoLast();
    }
}
