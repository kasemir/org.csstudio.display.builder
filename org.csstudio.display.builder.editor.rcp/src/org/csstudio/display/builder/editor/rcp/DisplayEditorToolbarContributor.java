/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.EditorActionBarContributor;

/** Tool bar for Display Editor
 *
 *  <p>Adds 'RetargetAction' entries to tool bar.
 *  When editor becomes active, it registers handler for the action IDs
 *  http://wiki.eclipse.org/FAQ_How_do_I_enable_global_actions_such_as_Cut,_Paste,_and_Print_in_my_editor%3F
 *
 *  @author Kay Kasemir
 */
public class DisplayEditorToolbarContributor extends EditorActionBarContributor
{
    private final List<IWorkbenchAction> actions = new ArrayList<>();

    @Override
    public void contributeToToolBar(IToolBarManager manager)
    {
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        addAction(manager, ActionFactory.UNDO.create(window));
        addAction(manager, ActionFactory.REDO.create(window));
    }

    private void addAction(final IToolBarManager manager, final IWorkbenchAction action)
    {
        manager.add(action);
        actions.add(action);
    }

    @Override
    public void setActiveEditor(final IEditorPart part)
    {
        if (! (part instanceof DisplayEditorPart))
            return;
        final DisplayEditorPart editor = (DisplayEditorPart) part;

        final IActionBars bars = getActionBars();
        if (bars == null)
            return;

        for (IAction action : actions)
            bars.setGlobalActionHandler(action.getId(), editor.getAction(action.getId()));

        bars.updateActionBars();
    }

    @Override
    public void dispose()
    {
        for (IWorkbenchAction action : actions)
            action.dispose();
        actions.clear();
    }
}
