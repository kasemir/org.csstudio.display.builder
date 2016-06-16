/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot.internal;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

/** Tracker is a 'rubberband' type rectangle with handles to move or resize.
 *
 *  @author Kay Kasemir
 */
public class Tracker extends Group
{
    private static final int handle_size = 15;

    private final TrackerListener listener;

    /** Main rectangle of tracker */
    private final Rectangle tracker = new Rectangle();

    /** Handles at corners and edges of tracker */
    private final Rectangle handle_top_left, handle_top, handle_top_right,
                            handle_right, handle_bottom_right, handle_bottom,
                            handle_bottom_left, handle_left;

    /** Mouse position at start of drag. -1 used to indicate 'not active' */
    private double start_x = -1, start_y = -1;

    /** Tracker position at start of drag */
    private double orig_x, orig_y, orig_width, orig_height;


    public Tracker(final TrackerListener listener)
    {
        this.listener = listener;
        setAutoSizeChildren(false);

        tracker.getStyleClass().add("tracker");

        handle_top_left = createHandle();
        handle_top = createHandle();
        handle_top_right = createHandle();
        handle_right = createHandle();
        handle_bottom_right = createHandle();
        handle_bottom = createHandle();
        handle_bottom_left = createHandle();
        handle_left = createHandle();

        getChildren().addAll(tracker, handle_top_left, handle_top, handle_top_right,
                handle_right, handle_bottom_right,
                handle_bottom, handle_bottom_left, handle_left);

        hookEvents();
    }

    /** @return 'Handle' type rectangle */
    private Rectangle createHandle()
    {
        final Rectangle handle = new Rectangle(handle_size, handle_size);
        handle.getStyleClass().add("tracker_handle"); // TODO Different class? Color?
        handle.setOnMousePressed(this::startDrag);
        handle.setOnMouseReleased(this::endMouseDrag);
        return handle;
    }

    private void hookEvents()
    {
        tracker.setCursor(Cursor.MOVE);
        tracker.addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        tracker.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D pos = constrain(orig_x + dx, orig_y + dy);
            setPosition(pos.getX(), pos.getY(), orig_width, orig_height);
        });
        tracker.addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        tracker.setOnKeyPressed(this::handleKeyEvent);

        handle_top_left.setCursor(Cursor.NW_RESIZE);
        handle_top_left.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D tl = constrain(orig_x + dx, orig_y + dy);
            setPosition(tl.getX(), tl.getY(),
                        orig_width - (tl.getX() - orig_x),
                        orig_height - (tl.getY() - orig_y));
        });
        handle_top.setCursor(Cursor.N_RESIZE);
        handle_top.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dy = event.getY() - start_y;
            final Point2D t = constrain(orig_x, orig_y + dy);
            setPosition(t.getX(), t.getY(),
                        orig_width - (t.getX() - orig_x),
                        orig_height - (t.getY() - orig_y));
        });
        handle_top_right.setCursor(Cursor.NE_RESIZE);
        handle_top_right.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D tr = constrain(orig_x + orig_width + dx, orig_y + dy);
            setPosition(orig_x, tr.getY(),
                        tr.getX() - orig_x, orig_height - (tr.getY() - orig_y));
        });
        handle_right.setCursor(Cursor.E_RESIZE);
        handle_right.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x;
            final Point2D r = constrain(orig_x + orig_width + dx, orig_y);
            setPosition(orig_x, orig_y, r.getX() - orig_x, orig_height);
        });
        handle_bottom_right.setCursor(Cursor.SE_RESIZE);
        handle_bottom_right.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D br = constrain(orig_x + orig_width + dx, orig_y + orig_height + dy);
            setPosition(orig_x, orig_y, br.getX() - orig_x, br.getY() - orig_y);
        });
        handle_bottom.setCursor(Cursor.S_RESIZE);
        handle_bottom.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dy = event.getY() - start_y;
            final Point2D b = constrain(orig_x, orig_y + orig_height + dy);
            setPosition(orig_x, orig_y, orig_width, b.getY() - orig_y);
        });
        handle_bottom_left.setCursor(Cursor.SW_RESIZE);
        handle_bottom_left.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D bl = constrain(orig_x + dx, orig_y + orig_height + dy);
            setPosition(bl.getX(), orig_y,
                        orig_width - (bl.getX() - orig_x),
                        bl.getY() - orig_y);
        });
        handle_left.setCursor(Cursor.W_RESIZE);
        handle_left.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x;
            final Point2D l = constrain(orig_x + dx, orig_y);
            setPosition(l.getX(), orig_y, orig_width - (l.getX() - orig_x), orig_height);
        });

    }

    /** Allow derived class to constrain positions
     *  @param x Requested X position
     *  @param y Requested Y position
     *  @return Actual position
     */
    protected Point2D constrain(final double x, final double y)
    {
        return new Point2D(x, y);
    }

    /** @param event {@link MouseEvent} */
    private void mousePressed(final MouseEvent event)
    {
        if (event.getClickCount() == 1)
            startDrag(event);
    }

    /** @param event {@link MouseEvent} */
    private void startDrag(final MouseEvent event)
    {
        // Take snapshot of current positions
        if (event == null)
        {
            start_x = -1;
            start_y = -1;
        }
        else
        {
            event.consume();
            start_x = event.getX();
            start_y = event.getY();
        }
        orig_x = tracker.getX();
        orig_y = tracker.getY();
        orig_width = tracker.getWidth();
        orig_height = tracker.getHeight();

        // TODO Call listener that drag starts

        // Get focus to allow use of arrow keys
        tracker.requestFocus();
    }

    /** @param event {@link MouseEvent} */
    private void mouseReleased(final MouseEvent event)
    {
        endMouseDrag(event);
    }

    /** @param event {@link MouseEvent} */
    private void endMouseDrag(final MouseEvent event)
    {
        // Get focus to allow use of arrow keys
        tracker.requestFocus();
        if (start_x < 0)
            return;
        if (event != null)
            event.consume();
        notifyListenerOfChange();
    }

    /** Allow move/resize with cursor keys.
     *
     *  <p>Shift: Resize
     *  @param event {@link KeyEvent}
     */
    private void handleKeyEvent(final KeyEvent event)
    {
        // XXX There is a strange loss of key focus:
        // Tracker does not receive key events unless one click/release
        // in the tracker rectangle.
        // Any mouse move after the click results in no more key events.

        // Consume handled event to keep the key focus,
        // which is otherwise lost to the 'tab-order' traversal
        final KeyCode code = event.getCode();
        System.out.println(code);
        switch (code)
        {
        case UP:
            if (event.isShiftDown())
                setPosition(tracker.getX(), tracker.getY(), tracker.getWidth(), tracker.getHeight()-1);
            else
                setPosition(tracker.getX(), tracker.getY()-1, tracker.getWidth(), tracker.getHeight());
            break;
        case DOWN:
            if (event.isShiftDown())
                setPosition(tracker.getX(), tracker.getY(), tracker.getWidth(), tracker.getHeight()+1);
            else
                setPosition(tracker.getX(), tracker.getY()+1, tracker.getWidth(), tracker.getHeight());
            break;
        case LEFT:
            if (event.isShiftDown())
                setPosition(tracker.getX(), tracker.getY(), tracker.getWidth()-1, tracker.getHeight());
            else
                setPosition(tracker.getX()-1, tracker.getY(), tracker.getWidth(), tracker.getHeight());
            break;
        case RIGHT:
            if (event.isShiftDown())
                setPosition(tracker.getX(), tracker.getY(), tracker.getWidth()+1, tracker.getHeight());
            else
                setPosition(tracker.getX()+1, tracker.getY(), tracker.getWidth(), tracker.getHeight());
            break;
        default:
            return;
        }
        event.consume();

        notifyListenerOfChange();

        // Reset tracker as if we started at this position.
        // That way, a sequence of cursor key moves turns into individual undo-able actions.
        orig_x = tracker.getX();
        orig_y = tracker.getY();
        orig_width = tracker.getWidth();
        orig_height = tracker.getHeight();
    }

    /** Update location and size of tracker
     *  @param rect
     */
    public void setPosition(final Rectangle2D rect)
    {
        setPosition(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }

    /** Update location and size of tracker
     *  @param x
     *  @param y
     *  @param width
     *  @param height
     */
    private void setPosition(final double x, final double y, double width, double height)
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

        // Get focus to allow use of arrow keys
        tracker.requestFocus();
    }

    private void notifyListenerOfChange()
    {
        final double dx = tracker.getX()      - orig_x;
        final double dy = tracker.getY()      - orig_y;
        final double dw = tracker.getWidth()  - orig_width;
        final double dh = tracker.getHeight() - orig_height;

        if (dx == 0.0  &&  dy == 0.0  &&  dw == 0.0  &&  dh == 0.0)
            return;

        listener.trackerChanged(dx, dy, dw, dh);
    }
}
