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
import org.csstudio.display.builder.editor.undo.SetWidgetPropertyAction;
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

    // Alignment icons from GEF (https://github.com/eclipse/gef/tree/master/org.eclipse.gef/src/org/eclipse/gef/internal/icons)
    /** Align widgets on left edge */
    public static final ActionDescription ALIGN_LEFT =
        new ActionDescription("icons/alignleft.gif", Messages.AlignLeft)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            final UndoableActionManager undo = editor.getUndoableActionManager();
            if (widgets.size() < 2)
                return;
            final int dest = widgets.get(0).positionX().getValue();
            for (int i=1; i<widgets.size(); ++i)
                undo.execute(new SetWidgetPropertyAction<Integer>(widgets.get(i).positionX(), dest));
        }
    };

    /** Align widgets on (vertical) center line */
    public static final ActionDescription ALIGN_CENTER =
        new ActionDescription("icons/aligncenter.gif", Messages.AlignCenter)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            final UndoableActionManager undo = editor.getUndoableActionManager();
            if (widgets.size() < 2)
                return;
            final int dest = widgets.get(0).positionX().getValue() + widgets.get(0).positionWidth().getValue() / 2;
            for (int i=1; i<widgets.size(); ++i)
                undo.execute(new SetWidgetPropertyAction<Integer>(widgets.get(i).positionX(),
                                                                  dest - widgets.get(i).positionWidth().getValue()/2));
        }
    };

    /** Align widgets on right edge */
    public static final ActionDescription ALIGN_RIGHT =
        new ActionDescription("icons/alignright.gif", Messages.AlignRight)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            final UndoableActionManager undo = editor.getUndoableActionManager();
            if (widgets.size() < 2)
                return;
            final int dest = widgets.get(0).positionX().getValue() + widgets.get(0).positionWidth().getValue();
            for (int i=1; i<widgets.size(); ++i)
                undo.execute(new SetWidgetPropertyAction<Integer>(widgets.get(i).positionX(),
                                                                  dest - widgets.get(i).positionWidth().getValue()));
        }
    };

    /** Align widgets on top edge */
    public static final ActionDescription ALIGN_TOP =
        new ActionDescription("icons/aligntop.gif", Messages.AlignTop)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            final UndoableActionManager undo = editor.getUndoableActionManager();
            if (widgets.size() < 2)
                return;
            final int dest = widgets.get(0).positionY().getValue();
            for (int i=1; i<widgets.size(); ++i)
                undo.execute(new SetWidgetPropertyAction<Integer>(widgets.get(i).positionY(), dest));
        }
    };

    /** Align widgets on (horizontal) middle line */
    public static final ActionDescription ALIGN_MIDDLE =
        new ActionDescription("icons/alignmid.gif", Messages.AlignMiddle)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            final UndoableActionManager undo = editor.getUndoableActionManager();
            if (widgets.size() < 2)
                return;
            final int dest = widgets.get(0).positionY().getValue() + widgets.get(0).positionHeight().getValue()/2;
            for (int i=1; i<widgets.size(); ++i)
                undo.execute(new SetWidgetPropertyAction<Integer>(widgets.get(i).positionY(),
                                                                  dest - widgets.get(i).positionHeight().getValue()/2));
        }
    };

    /** Align widgets on bottom edge */
    public static final ActionDescription ALIGN_BOTTOM =
        new ActionDescription("icons/alignbottom.gif", Messages.AlignBottom)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            final UndoableActionManager undo = editor.getUndoableActionManager();
            if (widgets.size() < 2)
                return;
            final int dest = widgets.get(0).positionY().getValue() + widgets.get(0).positionHeight().getValue();
            for (int i=1; i<widgets.size(); ++i)
                undo.execute(new SetWidgetPropertyAction<Integer>(widgets.get(i).positionY(),
                                                                  dest - widgets.get(i).positionHeight().getValue()));
        }
    };

    /** Set widgets to same width */
    public static final ActionDescription MATCH_WIDTH =
        new ActionDescription("icons/matchwidth.gif", Messages.MatchWidth)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            final UndoableActionManager undo = editor.getUndoableActionManager();
            if (widgets.size() < 2)
                return;
            final int dest = widgets.get(0).positionWidth().getValue();
            for (int i=1; i<widgets.size(); ++i)
                undo.execute(new SetWidgetPropertyAction<Integer>(widgets.get(i).positionWidth(), dest));
        }
    };

    /** Set widgets to same height */
    public static final ActionDescription MATCH_HEIGHT =
        new ActionDescription("icons/matchheight.gif", Messages.MatchHeight)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            final UndoableActionManager undo = editor.getUndoableActionManager();
            if (widgets.size() < 2)
                return;
            final int dest = widgets.get(0).positionHeight().getValue();
            for (int i=1; i<widgets.size(); ++i)
                undo.execute(new SetWidgetPropertyAction<Integer>(widgets.get(i).positionHeight(), dest));
        }
    };

    /** Distribute widgets horizontally */
    public static final ActionDescription DIST_HORIZ =
        new ActionDescription("icons/distribute_hc.png", Messages.DistributeHorizontally)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            final UndoableActionManager undo = editor.getUndoableActionManager();
            if (widgets.size() < 3)
                return;

            // TODO Get left/right


            // TODO Set widget's X coord to distribute horizontally
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
