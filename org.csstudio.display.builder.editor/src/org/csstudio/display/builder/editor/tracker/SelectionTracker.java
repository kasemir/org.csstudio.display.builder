/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.tracker;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.editor.undo.UndoableActionManager;
import org.csstudio.display.builder.editor.undo.UpdateWidgetLocationAction;
import org.csstudio.display.builder.editor.util.GeometryTools;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.representation.ToolkitRepresentation;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Rectangle;

/** Rubber-band-type tracker.
 *
 *  <p>Allows moving and resizing several widgets.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class SelectionTracker extends Group
{
    // Would be nice to treat every tracker move as a 'move-drag'.
    // Ctrl-drag would naturally become a 'copy-drag'.
    // But drag/drop feedback image is size-limited on GTK,
    // resulting in very poor representation when 'moving' widgets.
    //
    // -> Only using drag/drop for a 'copy' drag.

    // TODO Groups: Highlight groups on move-over,
    //              move widgets in and out of groups

    private final Logger logger = Logger.getLogger(getClass().getName());

    private static final int handle_size = 15;

    private final ToolkitRepresentation<Group, Node> toolkit;
    private final UndoableActionManager undo;

    private final TrackerGridConstraint grid_constraint = new TrackerGridConstraint(10);
    private final TrackerSnapConstraint snap_constraint = new TrackerSnapConstraint(this);

    private final GroupHandler group_handler;

    /** Main rectangle of tracker */
    private final Rectangle tracker = new Rectangle();

    /** Handles at corners and edges of tracker */
    private final Rectangle handle_top_left, handle_top, handle_top_right,
                            handle_right, handle_bottom_right, handle_bottom,
                            handle_bottom_left, handle_left;

    /** Widgets to track */
    private List<Widget> widgets = Collections.emptyList();

    /** Mouse position at start of drag. -1 used to indicate 'not active' */
    private double start_x = -1, start_y = -1;

    /** Tracker position at start of drag */
    private double orig_x, orig_y, orig_width, orig_height;

    /** Original widget position at start of a move/resize */
    private List<Rectangle2D> orig_position = Collections.emptyList();

    /** Break update loops JFX change -> model change -> JFX change -> ... */
    private boolean updating = false;

    /** Update tracker to match changed widget position */
    private final PropertyChangeListener position_listener = (event) ->
    {
        updateTrackerFromWidgets();
    };


    /** Construct a tracker.
     *
     *  <p>It remains invisible until it is asked to track widgets
     *  @param toolkit Toolkit
     *  @param selection Selection handler
     *  @param undo 'Undo' manager
     */
    public SelectionTracker(final ToolkitRepresentation<Group, Node> toolkit,
                            final WidgetSelectionHandler selection,
                            final UndoableActionManager undo)
    {
        this.toolkit = toolkit;
        this.undo = undo;

        setVisible(false);
        setAutoSizeChildren(false);

        group_handler = new GroupHandler(this, selection);

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

        // Track currently selected widgets
        selection.addListener(this::setSelectedWidgets);
    }

    private void hookEvents()
    {
        tracker.setCursor(Cursor.MOVE);
        tracker.addEventHandler(MouseEvent.MOUSE_PRESSED, this::startDrag);
        tracker.addEventHandler(MouseEvent.MOUSE_RELEASED, this::endMouseDrag);
        tracker.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D point = constrain(orig_x + dx, orig_y + dy);
            updateTracker(point.getX(), point.getY(), orig_width, orig_height);
        });
        tracker.setOnKeyPressed(this::handleKeyEvent);

        tracker.setOnDragDetected(event ->
        {
            if (! event.isControlDown())
                return;

            logger.log(Level.FINE, "Starting to drag {0}", widgets);
            final String xml;
            try
            {
                xml = ModelWriter.getXML(widgets);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot drag-serialize", ex);
                return;
            }
            final Dragboard db = tracker.startDragAndDrop(TransferMode.COPY);
            final ClipboardContent content = new ClipboardContent();
            content.putString(xml);
            db.setContent(content);

            // Would like to set tacker snapshot as drag image, but GTK error
            // as soon as the snapshot width is >= 128 pixel:
            // "GdkPixbuf-CRITICAL **: gdk_pixbuf_new_from_data: assertion `width > 0' failed"
            //
            //  final WritableImage snapshot = tracker.snapshot(null, null);
            //  System.out.println(snapshot.getWidth() + " x " + snapshot.getHeight());
            //  db.setDragView(snapshot);
            event.consume();
        });


        handle_top_left.setCursor(Cursor.NW_RESIZE);
        handle_top_left.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D tl = constrain(orig_x + dx, orig_y + dy);
            updateTracker(tl.getX(), tl.getY(),
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
            updateTracker(t.getX(), t.getY(),
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
            updateTracker(orig_x, tr.getY(),
                          tr.getX() - orig_x, orig_height - (tr.getY() - orig_y));
        });
        handle_right.setCursor(Cursor.W_RESIZE);
        handle_right.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x;
            final Point2D r = constrain(orig_x + orig_width + dx, orig_y);
            updateTracker(orig_x, orig_y, r.getX() - orig_x, orig_height);
        });
        handle_bottom_right.setCursor(Cursor.SE_RESIZE);
        handle_bottom_right.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D br = constrain(orig_x + orig_width + dx, orig_y + orig_height + dy);
            updateTracker(orig_x, orig_y, br.getX() - orig_x, br.getY() - orig_y);
        });
        handle_bottom.setCursor(Cursor.S_RESIZE);
        handle_bottom.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dy = event.getY() - start_y;
            final Point2D b = constrain(orig_x, orig_y + orig_height + dy);
            updateTracker(orig_x, orig_y, orig_width, b.getY() - orig_y);
        });
        handle_bottom_left.setCursor(Cursor.SW_RESIZE);
        handle_bottom_left.setOnMouseDragged((MouseEvent event) ->
        {
            if (start_x < 0)
                return;
            final double dx = event.getX() - start_x,  dy = event.getY() - start_y;
            final Point2D bl = constrain(orig_x + dx, orig_y + orig_height + dy);
            updateTracker(bl.getX(), orig_y,
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
            updateTracker(l.getX(), orig_y, orig_width - (l.getX() - orig_x), orig_height);
        });
    }

    /** Apply enabled constraints to requested position
     *  @param x Requested X position
     *  @param y Requested Y position
     *  @return Constrained coordinate
     */
    private Point2D constrain(final double x, final double y)
    {
        Point2D result = new Point2D(x, y);
        if (grid_constraint.isEnabled())
            result = grid_constraint.constrain(result.getX(), result.getY());
        if (snap_constraint.isEnabled())
            result = snap_constraint.constrain(result.getX(), result.getY());
        return result;
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

        // Take snapshot of widget coords relative to parent, not absolute
        orig_position = widgets.stream().map(GeometryTools::getBounds).collect(Collectors.toList());

        // Get focus to allow use of arrow keys
        tracker.requestFocus();

        logger.fine("Mouse pressed in tracker, starting a move");
    }

    /** Tracker is in front of the widgets that it handles,
     *  so it receives all mouse clicks.
     *  When 'Control' key is down, that event should be passed
     *  down to the widgets under the tracker, but JFX blocks them.
     *  This method locates all widgets that contain the mouse coord.
     *  and fires a 'click' on them.
     *  @param event Mouse event that needs to be passed down
     */
    private void passEventToWidgets(final MouseEvent event)
    {
        for (Widget widget : widgets)
            if (GeometryTools.getDisplayBounds(widget).contains(event.getX(), event.getY()))
            {
                logger.log(Level.FINE, "Tracker passes click through to {0}", widget);
                toolkit.fireClick(widget, event.isControlDown());
            }
    }

    private void endMouseDrag(final MouseEvent event)
    {
        // Get focus to allow use of arrow keys
        tracker.requestFocus();
        if (start_x < 0)
            return;
        if (event != null)
        {
            event.consume();
            if (event.isControlDown())
            {
                passEventToWidgets(event);
                return;
            }
        }
        updateWidgetsFromTracker();
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
            break;
        case DOWN:
            if (event.isShiftDown())
                updateTracker(tracker.getX(), tracker.getY(), tracker.getWidth(), tracker.getHeight()+1);
            else
                updateTracker(tracker.getX(), tracker.getY()+1, tracker.getWidth(), tracker.getHeight());
            break;
        case LEFT:
            if (event.isShiftDown())
                updateTracker(tracker.getX(), tracker.getY(), tracker.getWidth()-1, tracker.getHeight());
            else
                updateTracker(tracker.getX()-1, tracker.getY(), tracker.getWidth(), tracker.getHeight());
            break;
        case RIGHT:
            if (event.isShiftDown())
                updateTracker(tracker.getX(), tracker.getY(), tracker.getWidth()+1, tracker.getHeight());
            else
                updateTracker(tracker.getX()+1, tracker.getY(), tracker.getWidth(), tracker.getHeight());
            break;
        default:
            return;
        }
        updateWidgetsFromTracker();

        // Reset tracker as if we started at this position.
        // That way, a sequence of cursor key moves turns into individual undo-able actions.
        orig_x = tracker.getX();
        orig_y = tracker.getY();
        orig_width = tracker.getWidth();
        orig_height = tracker.getHeight();
        orig_position = widgets.stream().map(GeometryTools::getBounds).collect(Collectors.toList());

        event.consume();
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

        group_handler.locateGroup(x, y, width, height);
    }

    /** Updates widgets to current tracker size */
    private void updateWidgetsFromTracker()
    {
        if (updating  ||  widgets == null  ||  orig_position == null)
            return;
        updating = true;
        try
        {
            group_handler.hide();

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
                undo.add(new UpdateWidgetLocationAction(widget,
                               (int) orig.getMinX(),  (int) orig.getMinY(),
                               (int) orig.getWidth(), (int) orig.getHeight()));
            }
        }
        finally
        {
            updating = false;
        }
    }

    private void updateTrackerFromWidgets()
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

    /** @param enable Enable grid? */
    public void enableGrid(final boolean enable)
    {
        grid_constraint.setEnabled(enable);
    }

    /** @param enable Enable snap? */
    public void enableSnap(final boolean enable)
    {
        snap_constraint.setEnabled(enable);
    }

    /** Activate the tracker
     *  @param widgets Widgets to control by tracker,
     *                 empty to de-select
     */
    public void setSelectedWidgets(final List<Widget> widgets)
    {
        unbindFromWidgets();

        this.widgets = widgets;
        if (widgets.size() <= 0)
        {
            setVisible(false);
            return;
        }

        try
        {
            snap_constraint.configure(widgets.get(0).getDisplayModel(), widgets);
        }
        catch (final Exception ex)
        {
            logger.log(Level.WARNING, "Cannot obtain widget model", ex);
        }

        setVisible(true);

        updateTrackerFromWidgets();

        startDrag(null);

        bindToWidgets();

        // Get focus to allow use of arrow keys
        tracker.requestFocus();
    }

    private void bindToWidgets()
    {
        for (final Widget widget : widgets)
        {
            widget.positionX().addPropertyListener(position_listener);
            widget.positionY().addPropertyListener(position_listener);
            widget.positionWidth().addPropertyListener(position_listener);
            widget.positionHeight().addPropertyListener(position_listener);
        }
    }

    private void unbindFromWidgets()
    {
        for (final Widget widget : widgets)
        {
            widget.positionX().removePropertyListener(position_listener);
            widget.positionY().removePropertyListener(position_listener);
            widget.positionWidth().removePropertyListener(position_listener);
            widget.positionHeight().removePropertyListener(position_listener);
        }
    }
}
