/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import org.csstudio.display.builder.editor.rcp.DisplayEditorPart;
import org.csstudio.display.builder.editor.rcp.Messages;
import org.csstudio.display.builder.editor.rcp.Plugin;
import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.OpenDisplayAction;
import org.eclipse.jface.action.Action;

/** Action to execute currently edited display
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ExecuteDisplayAction extends Action
{
    private DisplayEditorPart edit_part;

    public ExecuteDisplayAction()
    {
        super(Messages.ExecuteDisplay, Plugin.getIcon("execute.png"));
    }

    public void setActiveEditor(final DisplayEditorPart edit_part)
    {
        this.edit_part = edit_part;
    }

    @Override
    public void run()
    {
        edit_part.doSave(null);
        final DisplayInfo info = edit_part.getDisplayInfo();
        new OpenDisplayAction(info).run();
    }
}
