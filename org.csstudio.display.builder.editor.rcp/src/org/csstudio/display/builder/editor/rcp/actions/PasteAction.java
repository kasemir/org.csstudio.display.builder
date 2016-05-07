/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;

/** Action to paste from clipboard
 *  @author Kay Kasemir
 */
public class PasteAction extends Action
{   // Used as handler for RetargetAction, so no need for label, icon
    private final DisplayEditor editor;
    public PasteAction(final DisplayEditor editor)
    {
        this.editor = editor;
        setActionDefinitionId(ActionFactory.PASTE.getCommandId());
    }

    @Override
    public void run()
    {
        // TODO Determine where the mouse is
        editor.pasteFromClipboard(0, 0);
    }
}
