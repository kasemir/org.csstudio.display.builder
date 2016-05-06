/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.actions.ToFrontAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Action to move widget to front
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ToFrontEditorAction extends EditorAction
{
    public ToFrontEditorAction()
    {
        super(org.csstudio.display.builder.editor.Messages.MoveToFront,
              AbstractUIPlugin.imageDescriptorFromPlugin(org.csstudio.display.builder.editor.Plugin.ID, "icons/tofront.png"));
    }

    @Override
    public void run()
    {
        final DisplayEditor editor = edit_part.getDisplayEditor();
        new ToFrontAction(editor).run(true);
    }
}
