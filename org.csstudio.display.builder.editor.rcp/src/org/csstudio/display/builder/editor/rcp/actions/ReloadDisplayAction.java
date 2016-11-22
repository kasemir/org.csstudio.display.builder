/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import org.csstudio.display.builder.editor.rcp.DisplayEditorPart;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Action to re-load currently edited display
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ReloadDisplayAction extends Action
{
    private final DisplayEditorPart editor;

    public ReloadDisplayAction(final DisplayEditorPart editor)
    {
        super(org.csstudio.display.builder.rcp.Messages.ReloadDisplay,
              AbstractUIPlugin.imageDescriptorFromPlugin(org.csstudio.display.builder.rcp.Plugin.ID,
                                                         "icons/refresh.gif"));
        this.editor = editor;
    }

    @Override
    public void run()
    {
        editor.loadModel();
    }
}
