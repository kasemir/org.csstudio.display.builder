/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.csstudio.display.builder.model.Widget;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

/** Rubber-band-type tracker.
 *
 *  <p>Allows moving and resizing several widgets.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class SelectionTracker extends Group
{
    private static final int handle_size = 15;

    // TODO Set/change constraint from toolbar button
    // TODO Implement TrackerSnapConstraint that snaps to nearby corners
//    private final TrackerConstraint constraint = new TrackerNullConstraint();
    private final TrackerConstraint constraint = new TrackerGridConstraint(10);

    /** Main rectangle of tracker */
    private final Rectangle tracker;

    /** Handles at corners and edges of tracker */
    private final Rectangle handle_top_left, handle_top, handle_top_right,
                            handle_right, handle_bottom_right, handle_bottom,
                            handle_bottom_left, handle_left;

    /** Widgets to track */
    private List<Widget> widgets = Collections.emptyList();

    /** Mouse position at start of drag */
    private double start_x, start_y;

    /** Tracker position at start of drag */
    private double orig_x, orig_y, orig_width, orig_height;

    /** Original widget position at start of a move/resize */
    private List<Rectangle2D> orig_position = Collections.emptyList();

    /** Construct a tracker.
     *
     *  <p>It remains invisible until it is asked to track widgets
     */
    public SelectionTracker()
    {
        setVisible(false);
        setAutoSizeChildren(false);

        tracker = new Rectangle();
        tracker.getStyleClass().add("tracker");

        handle_top_left = createHandle();
        handle_top = createHandle();
        handle_top_right = createHandle();
        handle_right = createHandle();
        handle_bottom_right = createHandle();
        handle_bottom = createHandle();
        handle_bottom_left = createHandle();
        handle_left = createHandle();

        hookEvents();

        getChildren().addAll(tracker, handle_top_left, handle_top, handle_top_right,
                             handle_right, handle_bottom_right,
                             handle_bottom, handle_bottom_left, handle_left);
    }

    private void hookEvents()
    {
        tracker.setCursor(Cursor.MOVE);
        tracker.setOnMousePressed(this::startDrag);
        tracker.setOnMouseReleased(this::endMouseDrag);
        tracker.setOnMouseDragged((MouseEvent event) ->
        {
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D point = constraint.constrain(orig_x + dx, orig_y + dy);
            updateTracker(point.getX(), point.getY(), orig_width, orig_height);
        });
        tracker.setOnKeyPressed(this::handleKeyEvent);

        handle_top_left.setCursor(Cursor.NW_RESIZE);
        handle_top_left.setOnMouseDragged((MouseEvent event) ->
        {
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D tl = constraint.constrain(orig_x + dx, orig_y + dy);
            updateTracker(tl.getX(), tl.getY(),
                          orig_width - (tl.getX() - orig_x),
                          orig_height - (tl.getY() - orig_y));
        });
        handle_top.setCursor(Cursor.N_RESIZE);
        handle_top.setOnMouseDragged((MouseEvent event) ->
        {
            final double dy = event.getY() - start_y;
            final Point2D t = constraint.constrain(orig_x, orig_y + dy);
            updateTracker(t.getX(), t.getY(),
                          orig_width - (t.getX() - orig_x),
                          orig_height - (t.getY() - orig_y));
        });
        handle_top_right.setCursor(Cursor.NE_RESIZE);
        handle_top_right.setOnMouseDragged((MouseEvent event) ->
        {
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D tr = constraint.constrain(orig_x + orig_width + dx, orig_y + dy);
            updateTracker(orig_x, tr.getY(),
                          tr.getX() - orig_x, orig_height - (tr.getY() - orig_y));
        });
        handle_right.setCursor(Cursor.W_RESIZE);
        handle_right.setOnMouseDragged((MouseEvent event) ->
        {
            final double dx = event.getX() - start_x;
            final Point2D r = constraint.constrain(orig_x + orig_width + dx, orig_y);
            updateTracker(orig_x, orig_y, r.getX() - orig_x, orig_height);
        });
        handle_bottom_right.setCursor(Cursor.SE_RESIZE);
        handle_bottom_right.setOnMouseDragged((MouseEvent event) ->
        {
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D br = constraint.constrain(orig_x + orig_width + dx, orig_y + orig_height + dy);
            updateTracker(orig_x, orig_y, br.getX() - orig_x, br.getY() - orig_y);
        });
        handle_bottom.setCursor(Cursor.S_RESIZE);
        handle_bottom.setOnMouseDragged((MouseEvent event) ->
        {
            final double dy = event.getY() - start_y;
            final Point2D b = constraint.constrain(orig_x, orig_y + orig_height + dy);
            updateTracker(orig_x, orig_y, orig_width, b.getY() - orig_y);
        });
        handle_bottom_left.setCursor(Cursor.SW_RESIZE);
        handle_bottom_left.setOnMouseDragged((MouseEvent event) ->
        {
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D bl = constraint.constrain(orig_x + dx, orig_y + orig_height + dy);
            updateTracker(bl.getX(), orig_y,
                          orig_width - (bl.getX() - orig_x),
                          bl.getY() - orig_y);
        });
        handle_left.setCursor(Cursor.W_RESIZE);
        handle_left.setOnMouseDragged((MouseEvent event) ->
        {
            final double dx = event.getX() - start_x;
            final Point2D l = constraint.constrain(orig_x + dx, orig_y);
            updateTracker(l.getX(), orig_y, orig_width - (l.getX() - orig_x), orig_height);
        });
    }

    /** @return 'Handle' type rectangle */
    private Rectangle createHandle()
    {
        final Rectangle handle = new Rectangle(handle_size, handle_size);
        handle.getStyleClass().add("tracker_handle");
        handle.setOnMousePressed(this::startDrag);
        handle.setOnMouseReleased(this::endMouseDrag);
        return handle;
    }

    /** @param event {@link MouseEvent}; <code>null</code> if not triggered by mouse */
    private void startDrag(final MouseEvent event)
    {
        // Take snapshot of current positions
        if (event == null)
        {
            start_x = 0;
            start_y = 0;
        }
        else
        {
            start_x = event.getX();
            start_y = event.getY();
        }
        orig_x = tracker.getX();
        orig_y = tracker.getY();
        orig_width = tracker.getWidth();
        orig_height = tracker.getHeight();

        // Take snapshot of widget coords relative to parent, not absolute
        orig_position = widgets.stream().map(GeometryTools::getBounds).collect(Collectors.toList());

        // Get focus to allow use of arrow keys
        tracker.requestFocus();
    }

    private void endMouseDrag(final MouseEvent event)
    {
        // Get focus to allow use of arrow keys
        tracker.requestFocus();
    }

    /** Allow move/resize with cursor keys.
     *
     *  <p>Shift: Resize
     *  @param event {@link KeyEvent}
     */
    private void handleKeyEvent(final KeyEvent event)
    {
        // Consume handled event to keep the key focus,
        // which is otherwise lost to the 'tab-order' traversal
        switch (event.getCode())
        {
        case UP:
            if (event.isShiftDown())
                updateTracker(tracker.getX(), tracker.getY(), tracker.getWidth(), tracker.getHeight()-1);
            else
                updateTracker(tracker.getX(), tracker.getY()-1, tracker.getWidth(), tracker.getHeight());
             event.consume();
            break;
        case DOWN:
            if (event.isShiftDown())
                updateTracker(tracker.getX(), tracker.getY(), tracker.getWidth(), tracker.getHeight()+1);
            else
                updateTracker(tracker.getX(), tracker.getY()+1, tracker.getWidth(), tracker.getHeight());
            event.consume();
            break;
        case LEFT:
            if (event.isShiftDown())
                updateTracker(tracker.getX(), tracker.getY(), tracker.getWidth()-1, tracker.getHeight());
            else
                updateTracker(tracker.getX()-1, tracker.getY(), tracker.getWidth(), tracker.getHeight());
            event.consume();
            break;
        case RIGHT:
            if (event.isShiftDown())
                updateTracker(tracker.getX(), tracker.getY(), tracker.getWidth()+1, tracker.getHeight());
            else
                updateTracker(tracker.getX()+1, tracker.getY(), tracker.getWidth(), tracker.getHeight());
            event.consume();
            break;
        default:
        }
    }

    /** Update tracker to provided location and size
     *  @param x
     *  @param y
     *  @param width
     *  @param height
     */
    private void updateTracker(final double x, final double y, double width, double height)
    {
        if (width < 0)
            width = 0;
        if (height < 0)
            height = 0;
        // relocate() will _not_ update Rectangle.x, y!
        tracker.setX(x);
        tracker.setY(y);
        tracker.setWidth(width);
        tracker.setHeight(height);

        handle_top_left.setX(x - handle_size);
        handle_top_left.setY(y - handle_size);

        handle_top.setVisible(width > handle_size);
        handle_top.setX(x + (width - handle_size) / 2);
        handle_top.setY(y - handle_size);

        handle_top_right.setX(x + width);
        handle_top_right.setY(y - handle_size);

        handle_right.setVisible(height > handle_size);
        handle_right.setX(x + width);
        handle_right.setY(y + (height - handle_size)/2);

        handle_bottom_right.setX(x + width);
        handle_bottom_right.setY(y + height);

        handle_bottom.setVisible(width > handle_size);
        handle_bottom.setX(x + (width - handle_size)/2);
        handle_bottom.setY(y + height);

        handle_bottom_left.setX(x - handle_size);
        handle_bottom_left.setY(y + height);

        handle_left.setVisible(height > handle_size);
        handle_left.setX(x - handle_size);
        handle_left.setY(y + (height - handle_size)/2);

        updateWidgetsFromTracker();
    }

    /** Break update loops JFX change -> model change -> JFX change -> ... */
    private boolean updating = false;

    /** Updates widgets to current tracker size */
    private void updateWidgetsFromTracker()
    {
        if (updating  ||  widgets == null  ||  orig_position == null)
            return;
        updating = true;
        try
        {
            final double dx = tracker.getX() - orig_x;
            final double dy = tracker.getY() - orig_y;
            final double dw = tracker.getWidth() - orig_width;
            final double dh = tracker.getHeight() - orig_height;
            final int N = Math.min(widgets.size(), orig_position.size());
            for (int i=0; i<N; ++i)
            {
                final Widget widget = widgets.get(i);
                final Rectangle2D orig = orig_position.get(i);
                widget.positionX().setValue((int) (orig.getMinX() + dx));
                widget.positionY().setValue((int) (orig.getMinY() + dy));
                widget.positionWidth().setValue((int) (orig.getWidth() + dw));
                widget.positionHeight().setValue((int) (orig.getHeight() + dh));
            }
        }
        finally
        {
            updating = false;
        }
    }

    private void updateTrackerFromWidgets(final PropertyChangeEvent event)
    {
        if (updating)
            return;
        updating = true;
        try
        {
            final Rectangle2D rect = widgets.stream()
                                            .map(GeometryTools::getDisplayBounds)
                                            .reduce(null, GeometryTools::join);
            updateTracker(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
        }
        finally
        {
            updating = false;
        }
    }

    /** Activate the tracker
     *  @param widgets Widgets to control by tracker
     */
    public void setSelectedWidgets(final List<Widget> widgets)
    {
        unbindFromWidgets();

        this.widgets = widgets;
        setVisible(true);

        updateTrackerFromWidgets(null);

        startDrag(null);

        bindToWidgets();

        // Get focus to allow use of arrow keys
        tracker.requestFocus();
    }

    private void bindToWidgets()
    {
        for (final Widget widget : widgets)
        {
            widget.positionX().addPropertyListener(this::updateTrackerFromWidgets);
            widget.positionY().addPropertyListener(this::updateTrackerFromWidgets);
            widget.positionWidth().addPropertyListener(this::updateTrackerFromWidgets);
            widget.positionHeight().addPropertyListener(this::updateTrackerFromWidgets);
        }
    }

    private void unbindFromWidgets()
    {
        for (final Widget widget : widgets)
        {
            widget.positionX().removePropertyListener(this::updateTrackerFromWidgets);
            widget.positionY().removePropertyListener(this::updateTrackerFromWidgets);
            widget.positionWidth().removePropertyListener(this::updateTrackerFromWidgets);
            widget.positionHeight().removePropertyListener(this::updateTrackerFromWidgets);
        }
    }
}
