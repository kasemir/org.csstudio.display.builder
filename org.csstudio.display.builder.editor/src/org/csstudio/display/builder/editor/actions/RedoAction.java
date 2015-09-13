/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.actions;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.util.undo.UndoableActionManager;

/** Action to perform 'Redo'
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RedoAction extends ActionDescription
{
    private final UndoableActionManager undo;

    /** @param undo {@link UndoableActionManager} */
    public RedoAction(final UndoableActionManager undo)
    {
        super("platform:/plugin/org.csstudio.display.builder.util/icons/redo.png",
              Messages.Redo_TT);
        this.undo = undo;
    }

    @Override
    public void run(final boolean selected)
    {
        undo.redoLast();
    }
}
