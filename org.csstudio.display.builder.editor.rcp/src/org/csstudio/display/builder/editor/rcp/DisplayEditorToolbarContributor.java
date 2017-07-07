/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;
import java.util.ArrayList;
import java.util.List;

import org.csstudio.display.builder.editor.actions.ActionDescription;
import org.csstudio.display.builder.editor.rcp.actions.EditorPartAction;
import org.csstudio.display.builder.editor.rcp.actions.ExecuteDisplayAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
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
    // Actions that act on active editor
    private final EditorPartAction[] editor_actions = new EditorPartAction[]
    {
        new ExecuteDisplayAction(),
        null, // Marker for Separator
        EditorPartAction.forToggledActionDescription(ActionDescription.ENABLE_GRID),
        EditorPartAction.forToggledActionDescription(ActionDescription.ENABLE_SNAP),
        EditorPartAction.forToggledActionDescription(ActionDescription.ENABLE_COORDS),
        null, // Marker for Separator
        EditorPartAction.forActionDescription(ActionDescription.TO_BACK),
        EditorPartAction.forActionDescription(ActionDescription.MOVE_UP),
        EditorPartAction.forActionDescription(ActionDescription.MOVE_DOWN),
        EditorPartAction.forActionDescription(ActionDescription.TO_FRONT),
        null, // Marker for Separator
        EditorPartAction.forActionDescription(ActionDescription.ALIGN_LEFT),
        EditorPartAction.forActionDescription(ActionDescription.ALIGN_CENTER),
        EditorPartAction.forActionDescription(ActionDescription.ALIGN_RIGHT),
        EditorPartAction.forActionDescription(ActionDescription.ALIGN_TOP),
        EditorPartAction.forActionDescription(ActionDescription.ALIGN_MIDDLE),
        EditorPartAction.forActionDescription(ActionDescription.ALIGN_BOTTOM),
        EditorPartAction.forActionDescription(ActionDescription.MATCH_WIDTH),
        EditorPartAction.forActionDescription(ActionDescription.MATCH_HEIGHT),
        EditorPartAction.forActionDescription(ActionDescription.DIST_HORIZ),
        EditorPartAction.forActionDescription(ActionDescription.DIST_VERT),
    };

    // Global actions defined by RCP
    // Holds actions for toolbar created by RCP ActionFactory.
    // Active editor then registers a handler.
    private final List<IWorkbenchAction> global_actions = new ArrayList<>();

    @Override
    public void contributeToToolBar(final IToolBarManager manager)
    {
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        // Add copy/paste to the toolbar? (call addGlobalAction() for these?)
        // -> No, since Eclipse text editor also doesn't to this.
        //    Reduce number of toolbar icons.
        global_actions.add(ActionFactory.CUT.create(window));
        global_actions.add(ActionFactory.COPY.create(window));
        global_actions.add(ActionFactory.PASTE.create(window));
        global_actions.add(ActionFactory.DELETE.create(window));
        global_actions.add(ActionFactory.SELECT_ALL.create(window));

        for (EditorPartAction epa : editor_actions)
            if (epa == null)
                manager.add(new Separator());
            else
                manager.add(epa);
        manager.add(new Separator());
        addGlobalAction(manager, ActionFactory.UNDO.create(window));
        addGlobalAction(manager, ActionFactory.REDO.create(window));
        manager.add(new Separator());
    }

    private void addGlobalAction(final IToolBarManager manager, final IWorkbenchAction action)
    {
        manager.add(action);
        global_actions.add(action);
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

        for (EditorPartAction epa : editor_actions)
            if (epa != null)
                epa.setActiveEditor(editor);

        // RCP defines global actions for copy, undo, ..
        // in the menu, including key bindings.
        // Bind to the handlers ('actions', but really used as handler)
        // of active editor.
        // Note that these handler 'actions' need to have called
        //   setActionDefinitionId(ActionFactory.XXXX.getCommandId()),
        // otherwise the global action remains disabled.
        for (IAction action : global_actions)
            bars.setGlobalActionHandler(action.getId(), editor.getRetargetActionHandler(action.getId()));

        bars.updateActionBars();
    }

    @Override
    public void dispose()
    {
        for (EditorPartAction epa : editor_actions)
            if (epa != null)
                epa.setActiveEditor(null);
        for (IWorkbenchAction action : global_actions)
            action.dispose();
        global_actions.clear();
    }
}
