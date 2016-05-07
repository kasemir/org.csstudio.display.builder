/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.actions;

import java.util.List;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.editor.undo.UpdateWidgetOrderAction;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.util.undo.UndoableActionManager;

/** Description of an action
 *
 *  Wraps the functionality, i.e. icon, tool tip, and what to execute,
 *  for use in a Java FX Button or Eclipse Action.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public abstract class ActionDescription
{
    /** Enable/disable grid */
    public static final ActionDescription ENABLE_GRID =
        new ActionDescription("icons/grid.png", Messages.Grid)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            editor.getSelectedWidgetUITracker().enableGrid(selected);
        }
    };

    /** Enable/disable snapping to other widgets */
    public static final ActionDescription ENABLE_SNAP =
        new ActionDescription("icons/snap.png", Messages.Snap)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            editor.getSelectedWidgetUITracker().enableSnap(selected);
        }
    };

    /** Move widget to back */
    public static final ActionDescription TO_BACK =
        new ActionDescription("icons/toback.png", Messages.MoveToBack)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            final UndoableActionManager undo = editor.getUndoableActionManager();
            for (Widget widget : widgets)
                undo.execute(new UpdateWidgetOrderAction(widget, 0));
        }
    };

    /** Move widget to front */
    public static final ActionDescription TO_FRONT =
        new ActionDescription("icons/tofront.png", Messages.MoveToFront)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            final UndoableActionManager undo = editor.getUndoableActionManager();
            for (Widget widget : widgets)
                undo.execute(new UpdateWidgetOrderAction(widget, -1));
        }
    };

    /** Un-do last change */
    public static final ActionDescription UNDO =
        new ActionDescription("icons/undo.png", Messages.Undo_TT)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final UndoableActionManager undo = editor.getUndoableActionManager();
            undo.undoLast();
        }
    };

    /** Re-do last change */
    public static final ActionDescription REDO =
        new ActionDescription("icons/redo.png", Messages.Redo_TT)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final UndoableActionManager undo = editor.getUndoableActionManager();
            undo.redoLast();
        }
    };

    private final String icon;
    private final String tool_tip;

    /** @param icon Icon path
     *  @param tool_tip Tool tip
     */
    public ActionDescription(final String icon, final String tool_tip)
    {
        this.icon = icon;
        this.tool_tip = tool_tip;
    }

    public String getIcon()
    {
        return icon;
    }

    public String getIconResourcePath()
    {
        return "platform:/plugin/org.csstudio.display.builder.editor/" + icon;
    }

    /** @return Tool tip */
    public String getToolTip()
    {
        return tool_tip;
    }

    /** Execute the action
     *
     *  <p>For plain "do it" actions, 'selected' will always be <code>true</code>.
     *  For actions that are bound to a toggle button because some feature
     *  is enabled or disabled, 'selected' reflects if the button was toggled 'on' or 'off'.
     *
     *  @param editor {@link DisplayEditor}
     *  @param selected Selected?
     */
    abstract public void run(DisplayEditor editor, boolean selected);

    /** Execute the action
     *
     *  @param editor {@link DisplayEditor}
     */
    public void run(final DisplayEditor editor)
    {
        run(editor, true);
    }
}
