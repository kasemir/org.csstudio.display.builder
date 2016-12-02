/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot.internal;

import static org.csstudio.javafx.rtplot.Activator.logger;

import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.csstudio.javafx.rtplot.util.RTPlotUpdateThrottle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/** Base Canvas for plots
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
abstract class PlotCanvasBase extends Canvas
{
    protected static final int ARROW_SIZE = 8;

    protected static final double ZOOM_FACTOR = 1.5;

    /** When using 'rubberband' to zoom in, need to select a region
     *  at least this wide resp. high.
     *  Smaller regions are likely the result of an accidental
     *  click-with-jerk, which would result into a huge zoom step.
     */
    protected static final int ZOOM_PIXEL_THRESHOLD = 20;

    /** Support for un-do and re-do */
    protected final UndoableActionManager undo = new UndoableActionManager(50);

    /** Area of this canvas */
    protected volatile Rectangle area = new Rectangle(0, 0, 0, 0);

    /** Suppress updates triggered by axis changes from layout or autoscale */
    protected volatile boolean in_update = false;

    /** Does layout need to be re-computed? */
    protected final AtomicBoolean need_layout = new AtomicBoolean(true);

    /** Throttle updates, enforcing a 'dormant' period */
    private final RTPlotUpdateThrottle update_throttle;

    /** Buffer for image and color bar
     *
     *  <p>UpdateThrottle calls updateImageBuffer() to set the image
     *  in its thread, PaintListener draws it in UI thread.
     *
     *  <p>Synchronizing to access one and the same image
     *  deadlocks on Linux, so a new image is created for updates.
     *  To avoid access to disposed image, SYNC on the actual image during access.
     */
    private volatile Image plot_image = null;

    /** Listener to {@link PlotPart}s, triggering refresh of canvas */
    protected final PlotPartListener plot_part_listener = new PlotPartListener()
    {
        @Override
        public void layoutPlotPart(final PlotPart plotPart)
        {
            need_layout.set(true);
        }

        @Override
        public void refreshPlotPart(final PlotPart plotPart)
        {
            if (! in_update)
                requestUpdate();
        }
    };

    /** Hack: Access to Canvas#isRendererFallingBehind()
     *  to check for buffered updates that eventually
     *  exhaust memory.
     */
    private Method isRendererFallingBehind;

    /** Redraw the canvas on UI thread by painting the 'plot_image' */
    private final Runnable redraw_runnable = () ->
    {
        // TODO Is canvas render buffer overflowing?
        //      If yes, use ImageView instead of Canvas?
        if (isRendererFallingBehind != null)
            try
            {
                Object behind = isRendererFallingBehind.invoke(this);
                if (behind instanceof Boolean  &&   ((Boolean) behind))
                {
                    logger.log(Level.WARNING, "Plot renderer is falling behind");
                    return;
                }
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot check Canvas rendering buffer", ex);
            }

        final GraphicsContext gc = getGraphicsContext2D();
        final Image image = plot_image;
        if (image != null)
            synchronized (image)
            {
                gc.drawImage(image, 0, 0);
            }
        drawMouseModeFeedback(gc);
    };

    protected MouseMode mouse_mode = MouseMode.NONE;
    protected Optional<Point2D> mouse_start = Optional.empty();
    protected volatile Optional<Point2D> mouse_current = Optional.empty();

    /** Constructor
     *  @param active Active mode where plot reacts to mouse/keyboard?
     */
    protected PlotCanvasBase(final boolean active)
    {
        final ChangeListener<? super Number> resize_listener = (prop, old, value) ->
        {
            area = new Rectangle((int)getWidth(), (int)getHeight());
            need_layout.set(true);
            requestUpdate();
        };
        widthProperty().addListener(resize_listener);
        heightProperty().addListener(resize_listener);


        try
        {
            isRendererFallingBehind = Canvas.class.getDeclaredMethod("isRendererFallingBehind");
            isRendererFallingBehind.setAccessible(true);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot access Canvas#isRendererFallingBehind", ex);
        }

        // 50Hz default throttle
        update_throttle = new RTPlotUpdateThrottle(50, TimeUnit.MILLISECONDS, () ->
        {
            plot_image = updateImageBuffer();
            redrawSafely();
        });

        if (active)
        {
            setOnMouseEntered(this::mouseEntered);
            setOnScroll(this::wheelZoom);
        }
    }

    /** @return {@link UndoableActionManager} for this plot */
    public UndoableActionManager getUndoableActionManager()
    {
        return undo;
    }

    /** Update the dormant time between updates
     *  @param dormant_time How long throttle remains dormant after a trigger
     *  @param unit Units for the dormant period
     */
    public void setUpdateThrottle(final long dormant_time, final TimeUnit unit)
    {
        update_throttle.setDormantTime(dormant_time, unit);
    }

    /** Request a complete redraw of the plot with new layout */
    final public void requestLayout()
    {
        need_layout.set(true);
        update_throttle.trigger();
    }

    /** Request a complete redraw of the plot */
    final public void requestUpdate()
    {
        update_throttle.trigger();
    }

    /** Redraw the current image and cursors
     *  <p>May be called from any thread.
     */
    final void redrawSafely()
    {
        Platform.runLater(redraw_runnable);
    }

    /** Draw all components into image buffer
     *  @return Latest image
     */
    protected abstract Image updateImageBuffer();

    protected abstract void drawMouseModeFeedback(GraphicsContext gc);

    /** Draw the zoom indicator for a horizontal zoom, i.e. on an X axis
     *
     *  @param gc GC to use
     *  @param plot_bounds Plot area where to draw the zoom indicator
     *  @param start Initial mouse position
     *  @param current Current mouse position
     */
    protected void drawZoomXMouseFeedback(final GraphicsContext gc, final Rectangle plot_bounds, final Point2D start, final Point2D current)
    {
        final int left = (int) Math.min(start.getX(), current.getX());
        final int right = (int) Math.max(start.getX(), current.getX());
        final int width = right - left;
        final int mid_y = plot_bounds.y + plot_bounds.height / 2;

        // See stroke comments in drawZoomMouseFeedback
        final Paint orig_stroke = gc.getStroke();
        for (int i=0; i<2; ++i)
        {
            if (i==0)
            {
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(3.5);
            }
            else
            {
                gc.setStroke(orig_stroke);
                gc.setLineWidth(1.5);
            }
            // Range on axis
            gc.strokeRect(left, start.getY(), width, 1);
            // Left, right vertical bar
            gc.strokeLine(left, plot_bounds.y, left, plot_bounds.y + plot_bounds.height);
            gc.strokeLine(right, plot_bounds.y, right, plot_bounds.y + plot_bounds.height);
            if (width >= 5*ARROW_SIZE)
            {
                gc.strokeLine(left, mid_y, left + 2*ARROW_SIZE, mid_y);
                gc.strokeLine(left+ARROW_SIZE, mid_y-ARROW_SIZE, left + 2*ARROW_SIZE, mid_y);
                gc.strokeLine(left+ARROW_SIZE, mid_y+ARROW_SIZE, left + 2*ARROW_SIZE, mid_y);

                gc.strokeLine(right, mid_y, right - 2*ARROW_SIZE, mid_y);
                gc.strokeLine(right-ARROW_SIZE, mid_y-ARROW_SIZE, right - 2*ARROW_SIZE, mid_y);
                gc.strokeLine(right-ARROW_SIZE, mid_y+ARROW_SIZE, right - 2*ARROW_SIZE, mid_y);
            }
        }
        gc.setLineWidth(1.0);
    }

    /** Draw the zoom indicator for a vertical zoom, i.e. on a Y axis
     *
     *  @param gc GC to use
     *  @param plot_bounds Plot area where to draw the zoom indicator
     *  @param start Initial mouse position
     *  @param current Current mouse position
     */
    protected void drawZoomYMouseFeedback(final GraphicsContext gc, final Rectangle plot_bounds, final Point2D start, final Point2D current)
    {
        final int top = (int) Math.min(start.getY(), current.getY());
        final int bottom = (int) Math.max(start.getY(), current.getY());
        final int height = bottom - top;
        final int mid_x = plot_bounds.x + plot_bounds.width / 2;

        // See stroke comments in drawZoomMouseFeedback
        final Paint orig_stroke = gc.getStroke();
        for (int i=0; i<2; ++i)
        {
            if (i==0)
            {
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(3.5);
            }
            else
            {
                gc.setStroke(orig_stroke);
                gc.setLineWidth(1.5);
            }
            // Range on axis
            gc.strokeRect(start.getX(), top, 1, height);
            // Top, bottom horizontal bar
            gc.strokeLine(plot_bounds.x, top, plot_bounds.x + plot_bounds.width, top);
            gc.strokeLine(plot_bounds.x, bottom, plot_bounds.x + plot_bounds.width, bottom);
            if (height >= 5 * ARROW_SIZE)
            {
                gc.strokeLine(mid_x, top, mid_x, top + 2*ARROW_SIZE);
                gc.strokeLine(mid_x-ARROW_SIZE, top+ARROW_SIZE, mid_x, top + 2*ARROW_SIZE);
                gc.strokeLine(mid_x+ARROW_SIZE, top+ARROW_SIZE, mid_x, top + 2*ARROW_SIZE);

                gc.strokeLine(mid_x, bottom - 2*ARROW_SIZE, mid_x, bottom);
                gc.strokeLine(mid_x, bottom - 2*ARROW_SIZE, mid_x-ARROW_SIZE, bottom - ARROW_SIZE);
                gc.strokeLine(mid_x, bottom - 2*ARROW_SIZE, mid_x+ARROW_SIZE, bottom - ARROW_SIZE);
            }
        }
        gc.setLineWidth(1.0);
    }

    /** Draw the zoom indicator for zoom, i.e. a 'rubberband'
     *
     *  @param gc GC to use
     *  @param plot_bounds Plot area where to draw the zoom indicator
     *  @param start Initial mouse position
     *  @param current Current mouse position
     */
    protected void drawZoomMouseFeedback(final GraphicsContext gc, final Rectangle plot_bounds, final Point2D start, final Point2D current)
    {
        final int left = (int) Math.min(start.getX(), current.getX());
        final int right = (int) Math.max(start.getX(), current.getX());
        final int top = (int) Math.min(start.getY(), current.getY());
        final int bottom = (int) Math.max(start.getY(), current.getY());
        final int width = right - left;
        final int height = bottom - top;
        final int mid_x = left + width / 2;
        final int mid_y = top + height / 2;

        final Paint orig_stroke = gc.getStroke();
        for (int i=0; i<2; ++i)
        {
            if (i==0)
            {   // White 'background' to help rectangle show up on top
                // of dark images
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(3.5);
            }
            else
            {   // JFX line coordinates use the 'corner' of a pixel.
                // A 1-pixel line at 'left + 0.5, top + 0.5, ...' would be sharp,
                // but offset from the center of the cursor hot point.
                // A line at 'left, top, ..' is blurry unless its widened
                // to cover full pixels.
                // Width of 1.5 happens to result in line that nicely aligns with
                // the cursor hot spot.
                gc.setStroke(orig_stroke);
                gc.setLineWidth(1.5);
            }
            // Main 'rubberband' rect
            gc.strokeRect(left, top, width, height);
            if (width >= 5*ARROW_SIZE)
            {
                gc.strokeLine(left, mid_y, left + 2*ARROW_SIZE, mid_y);
                gc.strokeLine(left+ARROW_SIZE, mid_y-ARROW_SIZE, left + 2*ARROW_SIZE, mid_y);
                gc.strokeLine(left+ARROW_SIZE, mid_y+ARROW_SIZE, left + 2*ARROW_SIZE, mid_y);

                gc.strokeLine(right, mid_y, right - 2*ARROW_SIZE, mid_y);
                gc.strokeLine(right-ARROW_SIZE, mid_y-ARROW_SIZE, right - 2*ARROW_SIZE, mid_y);
                gc.strokeLine(right-ARROW_SIZE, mid_y+ARROW_SIZE, right - 2*ARROW_SIZE, mid_y);
            }
            if (height >= 5*ARROW_SIZE)
            {
                gc.strokeLine(mid_x, top, mid_x, top + 2*ARROW_SIZE);
                gc.strokeLine(mid_x-ARROW_SIZE, top+ARROW_SIZE, mid_x, top + 2*ARROW_SIZE);
                gc.strokeLine(mid_x+ARROW_SIZE, top+ARROW_SIZE, mid_x, top + 2*ARROW_SIZE);

                gc.strokeLine(mid_x, bottom - 2*ARROW_SIZE, mid_x, bottom);
                gc.strokeLine(mid_x, bottom - 2*ARROW_SIZE, mid_x-ARROW_SIZE, bottom - ARROW_SIZE);
                gc.strokeLine(mid_x, bottom - 2*ARROW_SIZE, mid_x+ARROW_SIZE, bottom - ARROW_SIZE);
            }
        }
        gc.setLineWidth(1.0);
    }

    /** @param mode New {@link MouseMode}
     *  @throws IllegalArgumentException if mode is internal
     */
    public void setMouseMode(final MouseMode mode)
    {
        if (mode.ordinal() >= MouseMode.INTERNAL_MODES.ordinal())
            throw new IllegalArgumentException("Not permitted to set " + mode);
        mouse_mode = mode;
        PlotCursors.setCursor(this, mouse_mode);
    }

    /** onMouseEntered */
    protected void mouseEntered(final MouseEvent e)
    {
        getScene().setCursor(getCursor());
    }

    /** Zoom in/out triggered by mouse wheel
     *  @param event Scroll event
     */
    protected void wheelZoom(final ScrollEvent event)
    {
        // Invoked by mouse scroll wheel.
        // Only allow zoom (with control), not pan.
        if (! event.isControlDown())
            return;

        if (event.getDeltaY() > 0)
            zoomInOut(event.getX(), event.getY(), 1.0/ZOOM_FACTOR);
        else if (event.getDeltaY() < 0)
            zoomInOut(event.getX(), event.getY(), ZOOM_FACTOR);
        else
            return;
        event.consume();
    }

    /** Zoom 'in' or 'out' from where the mouse was clicked
     *  @param x Mouse coordinate
     *  @param y Mouse coordinate
     *  @param factor Zoom factor, positive to zoom 'out'
     */
    protected abstract void zoomInOut(final double x, final double y, final double factor);

    /** Should be invoked when plot no longer used to release resources */
    public void dispose()
    {   // Stop updates which could otherwise still use
        // what's about to be disposed
        update_throttle.dispose();
    }
}
