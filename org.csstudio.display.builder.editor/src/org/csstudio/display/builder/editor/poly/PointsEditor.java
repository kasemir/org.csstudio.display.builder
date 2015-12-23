/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.poly;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.properties.Points;
import org.csstudio.display.builder.util.UtilPlugin;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/** Editor for interactively adding/moving/removing points
 *
 *  <p>In "APPEND" mode, each mouse click adds another point.
 *  Backspace key deletes the last point.
 *  Space key switches to "EDIT" mode.
 *
 *  <p>In "EDIT" mode, a handle on each point allows moving
 *  the point. Clicking with 'Control' adds a new point.
 *  Clicking with 'Alt' deletes the point.
 *  Space key switches to "APPEND" mode.
 *
 *  <p>Escape key exists the editor from either mode.
 *
 *  @author Kay Kasemir
 */
public class PointsEditor
{
    private static ImageCursor cursor_add, cursor_remove;

    private Points points;
    private PointsEditorListener listener;
    private Group handle_group;

    private Line line = new Line();

    private enum Mode
    {
        APPEND,
        EDIT;
    }

    private Mode mode = Mode.APPEND;

    private final EventHandler<KeyEvent> key_filter = event ->
    {
        // Space to change modes
        if (event.getCode() == KeyCode.SPACE)
        {
            endMode();
            if (mode == Mode.APPEND)
                startMode(Mode.EDIT);
            else
                startMode(Mode.APPEND);
        }

        // Backspace to delete last point in 'APPEND' mode
        int N = points.size();
        if (mode == Mode.APPEND  &&  N > 0  &&
            event.getCode() == KeyCode.BACK_SPACE)
        {
            points.delete(N-1);
            --N;
            if (N > 0)
            {
                line.setStartX(points.getX(N-1));
                line.setStartY(points.getY(N-1));
            }
            else
            {
                line.setVisible(false);
            }
            listener.pointsChanged(points);
        }

        if (event.getCode() == KeyCode.ESCAPE)
            listener.done();
    };

    private EventHandler<MouseEvent> append_mouse_handler = event ->
    {
        if (event.getEventType() == MouseEvent.MOUSE_MOVED)
        {
            if (cursor_add != null)
                handle_group.getScene().setCursor(cursor_add);
            final int N = points.size();
            if (N > 0)
            {
                line.setStartX(points.getX(points.size()-1));
                line.setStartY(points.getY(points.size()-1));
                line.setEndX(event.getX());
                line.setEndY(event.getY());
                line.setVisible(true);
            }
            else
                line.setVisible(false);
        }
        else if (event.getEventType() == MouseEvent.MOUSE_CLICKED)
        {
            if (event.getClickCount() == 1)
            {
                points.add(event.getX(), event.getY());
                line.setStartX(event.getX());
                line.setStartY(event.getY());
                listener.pointsChanged(points);
            }
        }
    };

    /** Static initialization of custom cursors */
    private static synchronized void init()
    {   // Already initialized?
        if (cursor_remove != null)
            return;
        try
        {
            Image image = new Image(UtilPlugin.getStream("platform:/plugin/org.csstudio.display.builder.editor/icons/add_cursor.png"));
            cursor_add = new ImageCursor(image, image.getWidth() / 2, image.getHeight() /2);

            image = new Image(UtilPlugin.getStream("platform:/plugin/org.csstudio.display.builder.editor/icons/remove_cursor.png"));
            cursor_remove = new ImageCursor(image, image.getWidth() / 2, image.getHeight() /2);
        }
        catch (Exception ex)
        {
            Logger.getLogger(PointsEditor.class.getName())
                  .log(Level.WARNING, "Cannot load cursor images", ex);
        }
    }

    /** Create points editor
     *  @param root Parent group where editor can host its UI elements
     *  @param points Points to edit
     *  @param listener Listener to notify
     */
    public PointsEditor(final Group root, final Points points, final PointsEditorListener listener)
    {
        init();

        this.points = points;
        this.listener = listener;
        handle_group = new Group();
        root.getChildren().add(handle_group);

        line.getStyleClass().add("points_edit_line");

        startMode(Mode.APPEND);

        handle_group.getScene().addEventFilter(KeyEvent.KEY_PRESSED, key_filter);
    }

    /** Activate mode
     *
     *  <p>Display required UI elements, hook event handlers
     *  @param mode Desired mode
     */
    private void startMode(final Mode mode)
    {
        this.mode = mode;
        if (mode == Mode.APPEND)
        {
            handle_group.getScene().addEventHandler(MouseEvent.MOUSE_CLICKED, append_mouse_handler);
            handle_group.getScene().addEventHandler(MouseEvent.MOUSE_MOVED, append_mouse_handler);
            handle_group.getChildren().setAll(line);
            line.setVisible(false);
            if (cursor_add != null)
                handle_group.getScene().setCursor(cursor_add);
        }
        else
        {
            final ObservableList<Node> parent = handle_group.getChildren();
            parent.clear();
            for (int i = 0; i < points.size(); ++i)
                parent.add(new Handle(i));
            handle_group.getScene().setCursor(Cursor.HAND);
        }
    }

    /** De-activate mode */
    private void endMode()
    {
        if (mode == Mode.APPEND)
        {
            handle_group.getScene().removeEventHandler(MouseEvent.MOUSE_CLICKED, append_mouse_handler);
            handle_group.getScene().removeEventHandler(MouseEvent.MOUSE_MOVED, append_mouse_handler);
            handle_group.getChildren().clear();
        }
        else
        {
            for (Node node : handle_group.getChildren())
                ((Handle)node).dispose();
            handle_group.getChildren().clear();
        }
        handle_group.getScene().setCursor(Cursor.DEFAULT);
    }

    /** Must be called to remove UI elements and detach event handlers */
    public void dispose()
    {
        endMode();
        handle_group.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, key_filter);
    }

    /** Handle attached to a point in 'EDIT' mode */
    private class Handle extends Rectangle
    {   // Inner class that accesses 'points', '*_cursor' and 'listener'
        private static final double SIZE = 10;
        private final int index;
        private double x_offset, y_offset;

        Handle(final int index)
        {
            super(points.getX(index)-SIZE/2, points.getY(index)-SIZE/2, SIZE, SIZE);
            this.index = index;
            getStyleClass().add("points_edit_handle");
            hookListeners();
        }

        private void hookListeners()
        {
            setOnMousePressed(event ->
            {
                x_offset = getX()+SIZE/2 - event.getX();
                y_offset = getY()+SIZE/2 - event.getY();
                getScene().setCursor(Cursor.CLOSED_HAND);

                if (event.isControlDown())
                {
                    final double x, y;
                    if (index < points.size() - 1)
                    {   // Insert new point midway to next handle
                        x = (points.getX(index) + points.getX(index+1))/2;
                        y = (points.getY(index) + points.getY(index+1))/2;
                    }
                    else if (index > 0)
                    {   // Extend line from previous point
                        x = 2*points.getX(index) - points.getX(index-1);
                        y = 2*points.getY(index) - points.getY(index-1);
                    }
                    else
                    {   // Append point at end
                        x = points.getX(index) + 2*SIZE;
                        y = points.getY(index) + 2*SIZE;
                    }
                    points.insert(index+1, x, y);
                    listener.pointsChanged(points);
                    // Re-create handers
                    endMode();
                    startMode(mode);
                }
                else if (event.isAltDown())
                {
                    // Don't delete last point
                    if (points.size() <= 1)
                        return;
                    points.delete(index);
                    listener.pointsChanged(points);
                    // Re-create handers
                    endMode();
                    startMode(mode);
                }
            });
            setOnMouseMoved(event ->
            {
                if (event.isControlDown()  &&  cursor_add != null)
                    setCursor(cursor_add);
                else if (event.isAltDown()  &&  cursor_remove != null)
                    setCursor(cursor_remove);
                else
                    setCursor(Cursor.HAND);
            });
            setOnMouseDragged(event ->
            {
                final double x = event.getX() + x_offset;
                final double y = event.getY() + y_offset;
                points.set(index, x, y);
                setX(x - SIZE/2);
                setY(y - SIZE/2);
                listener.pointsChanged(points);
                getScene().setCursor(Cursor.CLOSED_HAND);
            });
            setOnMouseReleased(event -> getScene().setCursor(Cursor.HAND));
        }

        void dispose()
        {
            setOnMouseReleased(null);
            setOnMouseDragged(null);
            setOnMouseMoved(null);
            setOnMousePressed(null);
        }
    }
}
