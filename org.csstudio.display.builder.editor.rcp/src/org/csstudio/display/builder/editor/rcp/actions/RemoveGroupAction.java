/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.editor.undo.UnGroupWidgetsAction;
import org.csstudio.display.builder.model.ModelPlugin;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** SWT Action to remove a group
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RemoveGroupAction extends Action
{
    private final DisplayEditor editor;

    public RemoveGroupAction(final DisplayEditor editor)
    {
        super(Messages.RemoveGroup,
              AbstractUIPlugin.imageDescriptorFromPlugin(ModelPlugin.ID, "icons/group.png"));
        this.editor = editor;
    }

    @Override
    public void run()
    {
        final GroupWidget group = (GroupWidget) editor.getWidgetSelectionHandler().getSelection().get(0);

        editor.getWidgetSelectionHandler().clear();
        // Group's children list will be empty, create copy to select la
        final List<Widget> widgets = new ArrayList<>(group.runtimeChildren().getValue());

        final UndoableActionManager undo = editor.getUndoableActionManager();
        undo.execute(new UnGroupWidgetsAction(group));

        editor.getWidgetSelectionHandler().setSelection(widgets);
    }
}
