/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import java.util.List;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.model.Widget;
import org.eclipse.ui.actions.ActionFactory;

/** Action to select all widgets
 *  @author Kay Kasemir
 */
public class SelectAllAction extends RetargetActionHandler
{
    public SelectAllAction(final DisplayEditor editor)
    {
        super(editor, ActionFactory.SELECT_ALL.getCommandId());
    }

    @Override
    public void run()
    {
        // Selects all direct child widgets, not widgets inside group etc.
        // because copying or deleting a group already affects all sub-widgets
        final List<Widget> widgets = editor.getModel().getChildren();
        editor.getWidgetSelectionHandler().setSelection(widgets);
    }
}
