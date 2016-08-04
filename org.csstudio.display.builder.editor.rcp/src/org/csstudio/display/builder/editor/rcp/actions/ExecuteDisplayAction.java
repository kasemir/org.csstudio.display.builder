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
import org.eclipse.core.runtime.NullProgressMonitor;

/** Action to execute currently edited display
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ExecuteDisplayAction extends EditorPartAction
{
    /** Progress monitor that opens display when 'done' */
    private class MonitorSaveThenOpenDisplay extends NullProgressMonitor
    {
        @Override
        public void done()
        {
            if (isCanceled())
                return;
            final DisplayInfo info = edit_part.getDisplayInfo();
            new OpenDisplayAction(info).run();
        }
    }

    public ExecuteDisplayAction()
    {
        super(Messages.ExecuteDisplay, Plugin.getIcon("execute.png"));
    }

    public ExecuteDisplayAction(final DisplayEditorPart editor)
    {
        super(Messages.ExecuteDisplay, Plugin.getIcon("execute.png"));
        setActiveEditor(editor);
    }

    @Override
    public void run()
    {
        edit_part.doSave(new MonitorSaveThenOpenDisplay());
    }
}
