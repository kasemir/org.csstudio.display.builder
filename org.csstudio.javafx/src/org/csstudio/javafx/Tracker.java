/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx;

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
 *  TODO Check pixel by pixel if tracker has the correct size
 *
 *  @author Kay Kasemir
 */
public class Tracker extends Group
{
    private static final int HANDLE_SIZE = 10;

    private TrackerListener listener;

    /** Main rectangle of tracker */
    protected final Rectangle tracker = new Rectangle();

    /** Handles at corners and edges of tracker */
    private final Rectangle handle_top_left, handle_top, handle_top_right,
                            handle_right, handle_bottom_right, handle_bottom,
                            handle_bottom_left, handle_left;

    /** Mouse position at start of drag. -1 used to indicate 'not active' */
    private double start_x = -1, start_y = -1;

    /** Tracker position at start of drag */
    private Rectangle2D orig;


    public Tracker()
    {
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

    public void setListener(final TrackerListener listener)
    {
        this.listener = listener;
    }

    /** @return 'Handle' type rectangle */
    private Rectangle createHandle()
    {
        final Rectangle handle = new Rectangle(HANDLE_SIZE, HANDLE_SIZE);
        handle.getStyleClass().add("tracker_handle"); // TODO Different class? Color?
        handle.setOnMousePressed(this::startDrag);
        handle.setOnMouseReleased(this::endMouseDrag);
        return handle;
    }

    void hookEvents()
    {
        tracker.setCursor(Cursor.MOVE);
        tracker.addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        tracker.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D pos = constrain(orig.getMinX() + dx, orig.getMinY() + dy);
            setPosition(pos.getX(), pos.getY(), orig.getWidth(), orig.getHeight());
        });
        tracker.addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);

        tracker.setOnKeyPressed(this::handleKeyEvent);

        // Keep the keyboard focus to actually get key events.
        // The RTImagePlot will also listen to mouse moves and try to keep the focus,
        // so the active tracker uses an event filter to have higher priority
        tracker.addEventFilter(MouseEvent.MOUSE_MOVED, event ->
        {
            event.consume();
            tracker.requestFocus();
        });

        handle_top_left.setCursor(Cursor.NW_RESIZE);
        handle_top_left.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D tl = constrain(orig.getMinX() + dx, orig.getMinY() + dy);
            setPosition(tl.getX(), tl.getY(),
                        orig.getWidth() - (tl.getX() - orig.getMinX()),
                        orig.getHeight() - (tl.getY() - orig.getMinY()));
        });
        handle_top.setCursor(Cursor.N_RESIZE);
        handle_top.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dy = event.getY() - start_y;
            final Point2D t = constrain(orig.getMinX(), orig.getMinY() + dy);
            setPosition(t.getX(), t.getY(),
                        orig.getWidth() - (t.getX() - orig.getMinX()),
                        orig.getHeight() - (t.getY() - orig.getMinY()));
        });
        handle_top_right.setCursor(Cursor.NE_RESIZE);
        handle_top_right.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D tr = constrain(orig.getMinX() + orig.getWidth() + dx, orig.getMinY() + dy);
            setPosition(orig.getMinX(), tr.getY(),
                        tr.getX() - orig.getMinX(), orig.getHeight() - (tr.getY() - orig.getMinY()));
        });
        handle_right.setCursor(Cursor.E_RESIZE);
        handle_right.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x;
            final Point2D r = constrain(orig.getMinX() + orig.getWidth() + dx, orig.getMinY());
            setPosition(orig.getMinX(), orig.getMinY(), r.getX() - orig.getMinX(), orig.getHeight());
        });
        handle_bottom_right.setCursor(Cursor.SE_RESIZE);
        handle_bottom_right.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D br = constrain(orig.getMinX() + orig.getWidth() + dx, orig.getMinY() + orig.getHeight() + dy);
            setPosition(orig.getMinX(), orig.getMinY(), br.getX() - orig.getMinX(), br.getY() - orig.getMinY());
        });
        handle_bottom.setCursor(Cursor.S_RESIZE);
        handle_bottom.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dy = event.getY() - start_y;
            final Point2D b = constrain(orig.getMinX(), orig.getMinY() + orig.getHeight() + dy);
            setPosition(orig.getMinX(), orig.getMinY(), orig.getWidth(), b.getY() - orig.getMinY());
        });
        handle_bottom_left.setCursor(Cursor.SW_RESIZE);
        handle_bottom_left.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D bl = constrain(orig.getMinX() + dx, orig.getMinY() + orig.getHeight() + dy);
            setPosition(bl.getX(), orig.getMinY(),
                        orig.getWidth() - (bl.getX() - orig.getMinX()),
                        bl.getY() - orig.getMinY());
        });
        handle_left.setCursor(Cursor.W_RESIZE);
        handle_left.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x;
            final Point2D l = constrain(orig.getMinX() + dx, orig.getMinY());
            setPosition(l.getX(), orig.getMinY(), orig.getWidth() - (l.getX() - orig.getMinX()), orig.getHeight());
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
    protected void mousePressed(final MouseEvent event)
    {
        startDrag(event);
    }

    /** @param event {@link MouseEvent} */
    protected void startDrag(final MouseEvent event)
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
        orig = new Rectangle2D(tracker.getX(), tracker.getY(), tracker.getWidth(), tracker.getHeight());
    }

    /** @param event {@link MouseEvent} */
    protected void mouseReleased(final MouseEvent event)
    {
        endMouseDrag(event);
    }

    /** @param event {@link MouseEvent} */
    protected void endMouseDrag(final MouseEvent event)
    {
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
        // Consume handled event to keep the key focus,
        // which is otherwise lost to the 'tab-order' traversal
        final KeyCode code = event.getCode();
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
        orig = new Rectangle2D(tracker.getX(), tracker.getY(), tracker.getWidth(), tracker.getHeight());
    }

    public final void setPosition(final Rectangle2D position)
    {
        setPosition(position.getMinX(), position.getMinY(), position.getWidth(), position.getHeight());
    }


    /** Update location and size of tracker
     *  @param x
     *  @param y
     *  @param width
     *  @param height
     */
    public void setPosition(final double x, final double y, double width, double height)
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

        handle_top_left.setX(x - HANDLE_SIZE);
        handle_top_left.setY(y - HANDLE_SIZE);

        handle_top.setVisible(width > HANDLE_SIZE);
        handle_top.setX(x + (width - HANDLE_SIZE) / 2);
        handle_top.setY(y - HANDLE_SIZE);

        handle_top_right.setX(x + width);
        handle_top_right.setY(y - HANDLE_SIZE);

        handle_right.setVisible(height > HANDLE_SIZE);
        handle_right.setX(x + width);
        handle_right.setY(y + (height - HANDLE_SIZE)/2);

        handle_bottom_right.setX(x + width);
        handle_bottom_right.setY(y + height);

        handle_bottom.setVisible(width > HANDLE_SIZE);
        handle_bottom.setX(x + (width - HANDLE_SIZE)/2);
        handle_bottom.setY(y + height);

        handle_bottom_left.setX(x - HANDLE_SIZE);
        handle_bottom_left.setY(y + height);

        handle_left.setVisible(height > HANDLE_SIZE);
        handle_left.setX(x - HANDLE_SIZE);
        handle_left.setY(y + (height - HANDLE_SIZE)/2);
    }

    private void notifyListenerOfChange()
    {
        final Rectangle2D current = new Rectangle2D(tracker.getX(), tracker.getY(), tracker.getWidth(), tracker.getHeight());
        if (! current.equals(orig))
            listener.trackerChanged(orig, current);
    }
}
