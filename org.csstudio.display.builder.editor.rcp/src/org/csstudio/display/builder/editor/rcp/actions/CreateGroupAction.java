/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.editor.actions.ActionDescription;
import org.csstudio.display.builder.model.ModelPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** SWT Action to create group
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class CreateGroupAction extends Action
{
    private final DisplayEditor editor;

    public CreateGroupAction(final DisplayEditor editor)
    {
        super(Messages.CreateGroup,
              AbstractUIPlugin.imageDescriptorFromPlugin(ModelPlugin.ID, "icons/group.png"));
        this.editor = editor;
        setAccelerator(SWT.COMMAND | SWT.SHIFT | 'G');
    }

    @Override
    public void run()
    {
        ActionDescription.GROUP.run(editor);
    }
}
