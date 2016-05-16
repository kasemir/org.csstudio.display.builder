/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.eclipse.ui.actions.ActionFactory;

/** Action to cut to clipboard
 *  @author Kay Kasemir
 */
public class CutDeleteAction extends RetargetActionHandler
{
    public CutDeleteAction(final DisplayEditor editor, final boolean cut_or_delete)
    {
        super(editor, cut_or_delete
                      ? ActionFactory.CUT.getCommandId()
                      : ActionFactory.DELETE.getCommandId());
    }

    @Override
    public void run()
    {
        editor.cutToClipboard();
    }
}
