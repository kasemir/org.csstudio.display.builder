/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.actions;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.util.undo.UndoableActionManager;

/** Action to perform 'Undo'
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class UndoAction extends ActionDescription
{
    private final UndoableActionManager undo;

    /** @param undo {@link UndoableActionManager} */
    public UndoAction(final UndoableActionManager undo)
    {
        super("platform:/plugin/org.csstudio.display.builder.util/icons/undo.png",
              Messages.Undo_TT);
        this.undo = undo;
    }

    @Override
    public void run(final boolean selected)
    {
        undo.undoLast();
    }
}
