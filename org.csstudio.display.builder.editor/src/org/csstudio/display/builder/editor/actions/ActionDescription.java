/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.editor.undo.GroupWidgetsAction;
import org.csstudio.display.builder.editor.undo.SetWidgetPropertyAction;
import org.csstudio.display.builder.editor.undo.UnGroupWidgetsAction;
import org.csstudio.display.builder.editor.undo.UpdateWidgetOrderAction;
import org.csstudio.display.builder.editor.util.GeometryTools;
import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.model.widgets.GroupWidget.Style;
import org.csstudio.display.builder.util.undo.CompoundUndoableAction;
import org.csstudio.display.builder.util.undo.UndoableActionManager;

import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

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

    /** Enable/disable showing widget coordinates in tracker */
    public static final ActionDescription ENABLE_COORDS =
        new ActionDescription("icons/coords.png", Messages.ShowCoordinates)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            editor.getSelectedWidgetUITracker().showLocationAndSize(selected);
        }
    };

    /** Order widgets by their index in the parent's list of children
     *  <p>Original list will not be modified
     *  @param widgets Widgets in any order, because user may have selected them in random order
     *  @return Widgets sorted by their location in parent
     */
    private static List<Widget> orderWidgetsByIndex(final List<Widget> widgets)
    {
        final List<Widget> sorted = new ArrayList<>(widgets);
        sorted.sort((a, b) -> ChildrenProperty.getParentsChildren(a).getValue().indexOf(a) -
                              ChildrenProperty.getParentsChildren(b).getValue().indexOf(b));
        return sorted;
    }

    /** Move widget one step to the back (backward) */
    public static final ActionDescription MOVE_UP =
        new ActionDescription("Alt+B", "icons/up.png", Messages.MoveUp)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            // When multiple widgets are selected, they are moved 'up'
            // in their original order:
            // Move 'b, c' up in [ a, b, c ]
            // -- move 'b' up -> [ b, a, c ]
            // -- move 'c' up -> [ b, c, a ]
            // Doing this in reverse original order would leave the list unchanged.
            final List<Widget> widgets = orderWidgetsByIndex(editor.getWidgetSelectionHandler().getSelection());
            if (widgets.isEmpty())
                return;
            final CompoundUndoableAction compound = new CompoundUndoableAction(Messages.MoveUp);
            for (Widget widget : widgets)
            {
                final List<Widget> children = ChildrenProperty.getParentsChildren(widget).getValue();
                int orig = children.indexOf(widget);
                if (orig > 0)
                    compound.add(new UpdateWidgetOrderAction(widget, orig, orig-1));
                else
                    compound.add(new UpdateWidgetOrderAction(widget, orig, -1));
            }
            editor.getUndoableActionManager().execute(compound);
        }
    };

    /** Move widget to back */
    public static final ActionDescription TO_BACK =
        new ActionDescription("Alt+Shift+B", "icons/toback.png", Messages.MoveToBack)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            if (widgets.isEmpty())
                return;
            final CompoundUndoableAction compound = new CompoundUndoableAction(Messages.MoveToBack);
            for (Widget widget : widgets)
                compound.add(new UpdateWidgetOrderAction(widget, 0));
            editor.getUndoableActionManager().execute(compound);
        }
    };

    /** Move widget one step to the front (forward) */
    public static final ActionDescription MOVE_DOWN =
        new ActionDescription("Alt+F", "icons/down.png", Messages.MoveDown)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            // When multiple widgets are selected, they are moved 'down'
            // in reverse original order:
            // Move 'a, b' down in [ a, b, c ]
            // -- move 'b' down -> [ a, c, b ]
            // -- move 'a' down -> [ c, a, b ]
            // Doing this in the original order would leave the list unchanged.
            List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            if (widgets.isEmpty())
                return;
            widgets = orderWidgetsByIndex(widgets);
            Collections.reverse(widgets);

            final CompoundUndoableAction compound = new CompoundUndoableAction(Messages.MoveDown);
            for (Widget widget : widgets)
            {
                final List<Widget> children = ChildrenProperty.getParentsChildren(widget).getValue();
                int orig = children.indexOf(widget);
                if (orig < children.size()-1)
                    compound.add(new UpdateWidgetOrderAction(widget, orig, orig+1));
                else
                    compound.add(new UpdateWidgetOrderAction(widget, orig, 0));
            }
            editor.getUndoableActionManager().execute(compound);
        }
    };

    /** Move widget to front */
    public static final ActionDescription TO_FRONT =
        new ActionDescription("Alt+Shift+F", "icons/tofront.png", Messages.MoveToFront)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            if (widgets.isEmpty())
                return;
            // Same reasoning as in MOVE_DOWN
            // Without reversing, widgets would actually end up in front,
            // but un-doing the operation would them misplace them.
            widgets = orderWidgetsByIndex(widgets);
            Collections.reverse(widgets);
            final CompoundUndoableAction compound = new CompoundUndoableAction(Messages.MoveToFront);
            for (Widget widget : widgets)
                compound.add(new UpdateWidgetOrderAction(widget, -1));
            editor.getUndoableActionManager().execute(compound);
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
            final int min = widgets.stream()
                                   .mapToInt(w -> w.propX().getValue())
                                   .min()
                                   .orElseThrow(NoSuchElementException::new);
            widgets.stream().forEach(w -> undo.execute(new SetWidgetPropertyAction<Integer>(w.propX(), min)));
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
            final int min = widgets.stream()
                                   .mapToInt(w -> w.propX().getValue())
                                   .min()
                                   .orElseThrow(NoSuchElementException::new);
            final int max = widgets.stream()
                                   .mapToInt(w -> w.propX().getValue() + w.propWidth().getValue())
                                   .max()
                                   .orElseThrow(NoSuchElementException::new);
            final int center = ( min + max ) / 2;
            for (int i=0; i<widgets.size(); ++i)
                undo.execute(new SetWidgetPropertyAction<Integer>(widgets.get(i).propX(),
                                                                  center - widgets.get(i).propWidth().getValue()/2));
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
            final int max = widgets.stream()
                                   .mapToInt(w -> w.propX().getValue() + w.propWidth().getValue())
                                   .max()
                                   .orElseThrow(NoSuchElementException::new);
            widgets.stream().forEach(w -> undo.execute(new SetWidgetPropertyAction<Integer>(w.propX(), max - w.propWidth().getValue())));
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
            final int min = widgets.stream()
                                   .mapToInt(w -> w.propY().getValue())
                                   .min()
                                   .orElseThrow(NoSuchElementException::new);
            for (int i=0; i<widgets.size(); ++i)
                undo.execute(new SetWidgetPropertyAction<Integer>(widgets.get(i).propY(), min));
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
            final int min = widgets.stream()
                                   .mapToInt(w -> w.propY().getValue())
                                   .min()
                                   .orElseThrow(NoSuchElementException::new);
            final int max = widgets.stream()
                                   .mapToInt(w -> w.propY().getValue() + w.propHeight().getValue())
                                   .max()
                                   .orElseThrow(NoSuchElementException::new);
            final int middle = ( min + max ) / 2;
            for (int i=0; i<widgets.size(); ++i)
                undo.execute(new SetWidgetPropertyAction<Integer>(widgets.get(i).propY(),
                                                                  middle - widgets.get(i).propHeight().getValue()/2));
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
            final int max = widgets.stream()
                                   .mapToInt(w -> w.propY().getValue() + w.propHeight().getValue())
                                   .max()
                                   .orElseThrow(NoSuchElementException::new);
            for (int i=0; i<widgets.size(); ++i)
                undo.execute(new SetWidgetPropertyAction<Integer>(widgets.get(i).propY(),
                                                                  max - widgets.get(i).propHeight().getValue()));
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
            final int dest = widgets.get(0).propWidth().getValue();
            for (int i=1; i<widgets.size(); ++i)
                undo.execute(new SetWidgetPropertyAction<Integer>(widgets.get(i).propWidth(), dest));
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
            final int dest = widgets.get(0).propHeight().getValue();
            for (int i=1; i<widgets.size(); ++i)
                undo.execute(new SetWidgetPropertyAction<Integer>(widgets.get(i).propHeight(), dest));
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
            final int N = widgets.size();

            if ( N < 3 )
                return;

            final int min = widgets.stream()
                                   .mapToInt(w -> w.propX().getValue())
                                   .min()
                                   .orElseThrow(NoSuchElementException::new);
            final int max = widgets.stream()
                                   .mapToInt(w -> w.propX().getValue() + w.propWidth().getValue())
                                   .max()
                                   .orElseThrow(NoSuchElementException::new);
            final int totalWidth = widgets.stream()
                                          .mapToInt(w -> w.propWidth().getValue())
                                          .sum();
            final int offset = ( max - min - totalWidth ) / ( N - 1 );
            final int center = ( min + max ) / 2;
            final List<Widget> sortedWidgets = widgets.stream()
                .sorted(( w1, w2 ) -> {

                    final int w1x = w1.propX().getValue().intValue();
                    final int w1w = w1.propWidth().getValue().intValue();
                    final int w2x = w2.propX().getValue().intValue();
                    final int w2w = w2.propWidth().getValue().intValue();

                    if ( w1x <= w2x && w1x + w1w <= w2x + w2w ) {
                        //  +------+        |   +------+
                        //  |  w1  |            |  w1  |
                        //  +------+        |   +------+
                        //     +--------+                  +--------+
                        //     |   w2   |   |              |   w2   |
                        //     +--------+                  +--------+
                        return -1;
                    } else if ( w1x >= w2x && w1x + w1w >= w2x + w2w ) {
                        //  +------+        |   +------+
                        //  |  w2  |            |  w2  |
                        //  +------+        |   +------+
                        //     +--------+                  +--------+
                        //     |   w1   |   |              |   w1   |
                        //     +--------+                  +--------+
                        return 1;
                    } else {
                        //  +--------------------+  |  +--------------------+
                        //  |         w1         |     |         w1         |
                        //  +--------------------+  |  +--------------------+
                        //          +--------+         +--------------------+
                        //          |   w2   |      |  |         w2         |
                        //          +--------+         +--------------------+
                        return ( w1x + w1w / 2 ) - ( w2x + w2w / 2 );
                    }

                })
                .collect(Collectors.toList());

            if ( offset >= 0 ) {

                //  Equal gap distribution...
                //  ------------------------------------------------------------
                Widget widget = sortedWidgets.get(0);
                int location = widget.propX().getValue();
                int width = widget.propWidth().getValue();

                for ( int i = 1; i < N - 1; i++ ) {

                    widget = sortedWidgets.get(i);
                    location += width + offset;

                    undo.execute(new SetWidgetPropertyAction<Integer>(widget.propX(), location));

                    width = widget.propWidth().getValue();

                }

            } else if ( offset < 0 ) {

                //  Centers distribution...
                //  ------------------------------------------------------------
                //  First (leftmost) and last (rightmost) elements of the list
                //  are kept at their original position, the other widgets'
                //  centers are equally distributed between the centers of first
                //  and last widgets.
                Widget widget = sortedWidgets.get(N - 1);
                int location = widget.propX().getValue();
                int width = widget.propWidth().getValue();

                final int rightCenter = location + width / 2;

                widget = sortedWidgets.get(0);
                location = widget.propX().getValue();
                width = widget.propWidth().getValue();

                final int leftCenter = location + width / 2;
                final int coffset = ( rightCenter - leftCenter ) /  ( N - 1 );

                for ( int i = 1; i < N - 1; i++ ) {

                    widget = sortedWidgets.get(i);
                    width = widget.propWidth().getValue();

                    undo.execute(new SetWidgetPropertyAction<Integer>(widget.propX(), ( leftCenter + i * coffset ) - width / 2));

                }

            }

        }
    };

    /** Distribute widgets horizontally */
    public static final ActionDescription DIST_VERT =
        new ActionDescription("icons/distribute_vc.png", Messages.DistributeVertically)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {

            final List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();
            final UndoableActionManager undo = editor.getUndoableActionManager();
            final int N = widgets.size();

            if ( N < 3 )
                return;

            final int min = widgets.stream()
                                   .mapToInt(w -> w.propY().getValue())
                                   .min()
                                   .orElseThrow(NoSuchElementException::new);
            final int max = widgets.stream()
                                   .mapToInt(w -> w.propY().getValue() + w.propHeight().getValue())
                                   .max()
                                   .orElseThrow(NoSuchElementException::new);
            final int totalHeight = widgets.stream()
                                           .mapToInt(w -> w.propHeight().getValue())
                                           .sum();
            final int offset = ( max - min - totalHeight ) / ( N - 1 );
            final int center = ( min + max ) / 2;
            final List<Widget> sortedWidgets = widgets.stream()
                .sorted(( w1, w2 ) -> {

                    final int w1y = w1.propY().getValue().intValue();
                    final int w1h = w1.propHeight().getValue().intValue();
                    final int w2y = w2.propY().getValue().intValue();
                    final int w2h = w2.propHeight().getValue().intValue();

                    if ( w1y <= w2y && w1y + w1h <= w2y + w2h ) {
                        //  +----+          |  +----+
                        //  |    |             |    |
                        //  | w1 |  +----+  |  | w1 |
                        //  |    |  |    |     |    |
                        //  +----+  |    |  |  +----+
                        //          | w2 |
                        //          |    |  |          +----+
                        //          |    |             |    |
                        //          +----+  |          |    |
                        //                             | w2 |
                        //                  |          |    |
                        //                             |    |
                        //                  |          +----+
                        return -1;
                    } else if ( w1y >= w2y && w1y + w1h >= w2y + w2h ) {
                        //  +----+          |  +----+
                        //  |    |             |    |
                        //  | w2 |  +----+  |  | w2 |
                        //  |    |  |    |     |    |
                        //  +----+  |    |  |  +----+
                        //          | w1 |
                        //          |    |  |          +----+
                        //          |    |             |    |
                        //          +----+  |          |    |
                        //                             | w1 |
                        //                  |          |    |
                        //                             |    |
                        //                  |          +----+
                        return 1;
                    } else {
                        //  +----+          |  +----+  +----+
                        //  |    |          |  |    |  |    |
                        //  |    |          |  |    |  |    |
                        //  |    |  +----+  |  |    |  |    |
                        //  | w1 |  |    |  |  | w1 |  | w2 |
                        //  |    |  | w2 |  |  |    |  |    |
                        //  |    |  |    |  |  |    |  |    |
                        //  |    |  +----+  |  |    |  |    |
                        //  +----+          |  +----+  +----+
                        return ( w1y + w1h / 2 ) - ( w2y + w2h / 2 );
                    }

                })
                .collect(Collectors.toList());

            if ( offset >= 0 ) {

                //  Equal gap distribution...
                //  ------------------------------------------------------------
                Widget widget = sortedWidgets.get(0);
                int location = widget.propY().getValue();
                int height = widget.propHeight().getValue();

                for ( int i = 1; i < N - 1; i++ ) {

                    widget = sortedWidgets.get(i);
                    location += height + offset;

                    undo.execute(new SetWidgetPropertyAction<Integer>(widget.propY(), location));

                    height = widget.propHeight().getValue();

                }

            } else if ( offset < 0 ) {

                //  Centers distribution...
                //  ------------------------------------------------------------
                //  First (topmost) and last (bottom-most) elements of the list
                //  are kept at their original position, the other widgets'
                //  centers are equally distributed between the centers of first
                //  and last widgets.
                Widget widget = sortedWidgets.get(N - 1);
                int location = widget.propY().getValue();
                int height = widget.propHeight().getValue();

                final int bottomCenter = location + height / 2;

                widget = sortedWidgets.get(0);
                location = widget.propY().getValue();
                height = widget.propHeight().getValue();

                final int topCenter = location + height / 2;
                final int coffset = ( bottomCenter - topCenter ) /  ( N - 1 );

                for ( int i = 1; i < N - 1; i++ ) {

                    widget = sortedWidgets.get(i);
                    height = widget.propHeight().getValue();

                    undo.execute(new SetWidgetPropertyAction<Integer>(widget.propY(), ( topCenter + i * coffset ) - height / 2));

                }

            }

        }
    };

    /** Un-do last change */
    public static final ActionDescription UNDO =
        new ActionDescription("Shortcut+Z", "icons/undo.png", Messages.Undo_TT)
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
        new ActionDescription("Shortcut+Shift+Z", "icons/redo.png", Messages.Redo_TT)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final UndoableActionManager undo = editor.getUndoableActionManager();
            undo.redoLast();
        }
    };

    /** Create group */
    public static final ActionDescription GROUP =
            new ActionDescription("Shortcut+Shift+G", "icons/group.png", Messages.CreateGroup)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final List<Widget> widgets = editor.getWidgetSelectionHandler().getSelection();

            editor.getWidgetSelectionHandler().clear();

            // Create group that surrounds the original widget boundaries
            final GroupWidget group = new GroupWidget();

            // Get bounds of widgets relative to their container,
            // which might be a group within the display
            // or the display itself
            final Rectangle2D rect = GeometryTools.getBounds(widgets);

            // Inset depends on representation and changes with group style and font.
            // Can be obtained via group.runtimePropInsets() _after_ the group has
            // been represented. For this reason Style.NONE is used, where the inset
            // is always 0. An alternative could be Style.LINE, with an inset of 1.
            final int inset = 0;
            group.propStyle().setValue(Style.NONE);
            group.propTransparent().setValue(true);
            group.propX().setValue((int) rect.getMinX() - inset);
            group.propY().setValue((int) rect.getMinY() - inset);
            group.propWidth().setValue((int) rect.getWidth() + 2*inset);
            group.propHeight().setValue((int) rect.getHeight() + 2*inset);
            group.propName().setValue(org.csstudio.display.builder.model.Messages.GroupWidget_Name);

            final ChildrenProperty parent_children = ChildrenProperty.getParentsChildren(widgets.get(0));
            final UndoableActionManager undo = editor.getUndoableActionManager();
            undo.execute(new GroupWidgetsAction(parent_children, group, widgets, (int)rect.getMinX(), (int)rect.getMinY()));

            editor.getWidgetSelectionHandler().toggleSelection(group);
        }
    };

    /** Remove group */
    public static final ActionDescription UNGROUP =
            new ActionDescription("Shortcut+Shift+U", "icons/group.png", Messages.CreateGroup)
    {
        @Override
        public void run(final DisplayEditor editor, final boolean selected)
        {
            final GroupWidget group = (GroupWidget) editor.getWidgetSelectionHandler().getSelection().get(0);

            editor.getWidgetSelectionHandler().clear();
            // Group's children list will be empty, create copy to select la
            final List<Widget> widgets = new ArrayList<>(group.runtimeChildren().getValue());

            final UndoableActionManager undo = editor.getUndoableActionManager();
            undo.execute(new UnGroupWidgetsAction(group));

            editor.getWidgetSelectionHandler().setSelection(widgets);
        }
    };

    private final KeyCombination accelerator;
    private final String icon;
    private final String tool_tip;

    /** @param icon Icon path
     *  @param tool_tip Tool tip
     */
    public ActionDescription(final String icon, final String tool_tip)
    {
        this(null, icon, tool_tip);
    }

    /** @accelerator Keyboard accelerator (e.g. SWT.COMMAND | SWT.SHIFT | 'K').
     *  @param icon Icon path
     *  @param tool_tip Tool tip
     */
    public ActionDescription(final String accelerator, final String icon, final String tool_tip)
    {
        this.accelerator = ( accelerator != null && !accelerator.isEmpty() )
                         ? KeyCombination.keyCombination(accelerator)
                         : null;
        this.icon = icon;
        this.tool_tip = tool_tip;
    }

    public KeyCombination getAccelerator()
    {
        return accelerator;
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

    /**
     * @param event The {@link KeyEvent} under test.
     * @return {@code true} if this description contains an accelerator and it
     *         matches the given {@code event}, {@code false} otherwise.
     */
    public boolean match ( final KeyEvent event ) {
        if ( accelerator != null ) {
            return accelerator.match(event);
        } else {
            return false;
        }
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
