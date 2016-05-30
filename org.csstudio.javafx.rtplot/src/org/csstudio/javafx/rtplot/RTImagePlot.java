/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot;

import static org.csstudio.javafx.rtplot.Activator.logger;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleFunction;
import java.util.logging.Level;

import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.csstudio.javafx.PlatformInfo;
import org.csstudio.javafx.rtplot.internal.AxisPart;
import org.csstudio.javafx.rtplot.internal.HorizontalNumericAxis;
import org.csstudio.javafx.rtplot.internal.MouseMode;
import org.csstudio.javafx.rtplot.internal.PlotPart;
import org.csstudio.javafx.rtplot.internal.PlotPartListener;
import org.csstudio.javafx.rtplot.internal.YAxisImpl;
import org.csstudio.javafx.rtplot.internal.undo.ChangeImageZoom;
import org.csstudio.javafx.rtplot.internal.util.LinearScreenTransform;
import org.csstudio.javafx.rtplot.util.RTPlotUpdateThrottle;
import org.diirt.util.array.IteratorNumber;
import org.diirt.util.array.ListNumber;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Font;

/** Plot for an image
 *
 *  <p>Displays the intensity of values from a {@link ListNumber}
 *  as an image.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RTImagePlot extends Canvas
{
    final private static int ARROW_SIZE = 8;

    private static final double ZOOM_FACTOR = 1.5;

    /** When using 'rubberband' to zoom in, need to select a region
     *  at least this wide resp. high.
     *  Smaller regions are likely the result of an accidental
     *  click-with-jerk, which would result into a huge zoom step.
     */
    private static final int ZOOM_PIXEL_THRESHOLD = 20;

    /** Support for un-do and re-do */
    final private UndoableActionManager undo = new UndoableActionManager(50);

    /** Gray scale color mapping */
    public final static DoubleFunction<Color> GRAYSCALE = value -> new Color((float)value, (float)value, (float)value);

    /** Rainbow color mapping */
    public final static DoubleFunction<Color> RAINBOW = value -> new Color(Color.HSBtoRGB((float)value, 1.0f, 1.0f));

    // TODO Static cursors, init. once
    private Cursor cursor_zoom_in;

    /** Area of this canvas */
    private volatile Rectangle area = new Rectangle(0, 0, 0, 0);

    /** Does layout need to be re-computed? */
    final private AtomicBoolean need_layout = new AtomicBoolean(true);

    /** Axis range for 'full' image */
    private volatile double min_x = 0.0, max_x = 100.0, min_y = 0.0, max_y = 100.0;

    /** X Axis */
    final private AxisPart<Double> x_axis;

    /** Y Axis */
    final private YAxisImpl<Double> y_axis;

    /** Area used by the image */
    private volatile Rectangle image_area = new Rectangle(0, 0, 0, 0);

    /** Color bar Axis */
    final private YAxisImpl<Double> colorbar_axis;

    /** Area used by the color bar. <code>null</code> if not visible */
    private volatile Rectangle colorbar_area = null;

    /** Image data size */
    private volatile int data_width = 0, data_height = 0;

    /** Auto-scale the data range? */
    private volatile boolean autoscale = true;

    /** Image data range */
    private volatile double min=0.0, max=1.0;

    /** Show color bar? */
    private volatile boolean show_colormap = true;

    /** Color bar size */
    private volatile int colorbar_size = 40;

    /** Mapping of value 0..1 to color */
    private volatile DoubleFunction<Color> color_mapping = GRAYSCALE;

    /** Image data */
    private volatile ListNumber image_data = null;

    /** Is 'image_data' meant to be treated as 'unsigned'? */
    private volatile boolean unsigned_data = false;

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

    /** Throttle updates, enforcing a 'dormant' period */
    private final RTPlotUpdateThrottle update_throttle;

    /** Suppress updates triggered by axis changes from layout or autoscale */
    private volatile boolean in_update = false;

    private MouseMode mouse_mode = MouseMode.ZOOM_IN;
    private Optional<Point2D> mouse_start = Optional.empty();
    private volatile Optional<Point2D> mouse_current = Optional.empty();


    /** Listener to Axis {@link PlotPart} */
    final private PlotPartListener axis_listener = new PlotPartListener()
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

    final private Runnable redraw_runnable = () ->
    {
        final GraphicsContext gc = getGraphicsContext2D();
        final Image image = plot_image;
        if (image != null)
            synchronized (image)
            {
                gc.drawImage(image, 0, 0);
            }
        drawMouseModeFeedback(gc);
    };

    public RTImagePlot()
    {
        x_axis = new HorizontalNumericAxis("X", axis_listener);
        y_axis = new YAxisImpl<>("Y", axis_listener);
        colorbar_axis =  new YAxisImpl<>("", axis_listener);

        initializeCursors();

        // 50Hz default throttle
        update_throttle = new RTPlotUpdateThrottle(50, TimeUnit.MILLISECONDS, () ->
        {
            updateImageBuffer();
            redrawSafely();
        });
        x_axis.setValueRange(min_x, max_x);
        y_axis.setValueRange(min_y, max_y);

        final ChangeListener<? super Number> resize_listener = (prop, old, value) ->
        {
            area = new Rectangle((int)getWidth(), (int)getHeight());
            requestLayout();
        };
        widthProperty().addListener(resize_listener);
        heightProperty().addListener(resize_listener);

        // TODO Pass 'active' in as argument
        boolean active = true;
        if (active)
        {
            doSetCursor(cursor_zoom_in); // TODO Zoom out
            // TODO Keyboard commands
            // TODO Toolbar
            setOnMouseEntered(this::mouseEntered);
            setOnMousePressed(this::mouseDown);
            setOnMouseMoved(this::mouseMove);
            setOnMouseDragged(this::mouseMove);
            setOnMouseReleased(this::mouseUp);
            setOnMouseExited(this::mouseExit);
            setOnScroll(this::wheelZoom);
        }
    }

    private void initializeCursors()
    {
        try
        {
            cursor_zoom_in = new ImageCursor(Activator.getIcon("cursor_zoom_in"));
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Error loading cursors", ex);
            cursor_zoom_in = Cursor.DEFAULT;
        }
    }

    /** @param autoscale  Auto-scale the color mapping? */
    public void setAutoscale(final boolean autoscale)
    {
        this.autoscale = autoscale;
        requestUpdate();
    }

    /** Set color mapping value range
     *  @param min
     *  @param max
     */
    public void setValueRange(final double min, final double max)
    {
        this.min = min;
        this.max = max;
        requestUpdate();
    }

    /** @param color_mapping Function that returns {@link Color} for value 0.0 .. 1.0 */
    public void setColorMapping(final DoubleFunction<Color> color_mapping)
    {
        this.color_mapping = color_mapping;
        requestUpdate();
    }

    /** Set axis range for 'full' image
     *  @param min_x
     *  @param max_x
     *  @param min_y
     *  @param max_y
     */
    public void setAxisRange(final double min_x, final double max_x,
                             final double min_y, final double max_y)
    {
        this.min_x = min_x;
        this.max_x = max_x;
        this.min_y = min_y;
        this.max_y = max_y;
        x_axis.setValueRange(min_x, max_x);
        y_axis.setValueRange(min_y, max_y);
    }

    /** <b>Note: May offer too much access
     *  @return X Axis
     */
    public Axis<Double> getXAxis()
    {
        return x_axis;
    }

    /** <b>Note: May offer too much access
     *  @return Y Axis
     */
    public Axis<Double> getYAxis()
    {
        return y_axis;
    }

    /** @param show Show color map? */
    public void showColorMap(final boolean show)
    {
        show_colormap = show;
        requestLayout();
    }

    /** @param size Color bar size in pixels */
    public void setColorMapSize(final int size)
    {
        colorbar_size = size;
        requestLayout();
    }

    /** @param size Color bar size in pixels */
    public void setColorMapFont(final Font font)
    {
        colorbar_axis.setScaleFont(font);
        requestLayout();
    }

    /** Set the data to display
     *  @param width Number of elements in one 'row' of data
     *  @param height Number of data rows
     *  @param data Image elements, starting in 'top left' corner,
     *              proceeding along the row, then to next rows
     *  @param unsigned Is the data meant to be treated as 'unsigned'
     */
    public void setValue(final int width, final int height, final ListNumber data, final boolean unsigned)
    {
        data_width = width;
        data_height = height;
        image_data = data;
        unsigned_data = unsigned;
        requestUpdate();
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
     *
     *  <p>Like <code>redraw()</code>, but may be called
     *  from any thread.
     */
    final void redrawSafely()
    {
        Platform.runLater(redraw_runnable);
    }

    /** Compute layout of plot components */
    private void computeLayout(final Graphics2D gc, final Rectangle bounds,
                               final double min, final double max)
    {
        logger.log(Level.FINE, "computeLayout");

        // X Axis as high as desired. Width will depend on Y axis.
        final int x_axis_height = x_axis.getDesiredPixelSize(bounds, gc);
        final int y_axis_height = bounds.height - x_axis_height;
        final int y_axis_width  = y_axis.getDesiredPixelSize(new Rectangle(0, 0, bounds.width, y_axis_height), gc);

        image_area = new Rectangle(y_axis_width, 0, bounds.width - y_axis_width, bounds.height - x_axis_height);

        // Color bar requested and there's room?
        if (show_colormap)
        {
            colorbar_area = new Rectangle(bounds.width - colorbar_size, colorbar_size, colorbar_size, image_area.height-2*colorbar_size);

            final int cb_axis_width = colorbar_axis.getDesiredPixelSize(colorbar_area, gc);
            colorbar_axis.setBounds(colorbar_area.x, colorbar_area.y, cb_axis_width, colorbar_area.height);
            colorbar_area.x += cb_axis_width;
            colorbar_area.width -= cb_axis_width;

            if (image_area.width > cb_axis_width + colorbar_area.width)
                image_area.width -= cb_axis_width + colorbar_area.width;
            else
                colorbar_area = null;
        }
        else
            colorbar_area = null;

        y_axis.setBounds(0, 0, y_axis_width, image_area.height);
        x_axis.setBounds(image_area.x, image_area.height, image_area.width, x_axis_height);
    }

    /** Draw all components into image buffer */
    private void updateImageBuffer()
    {
        // Would like to use JFX WritableImage,
        // but rendering problem on Linux (sandbox.ImageScaling),
        // and no way to disable the color interpolation that 'smears'
        // the scaled image.
        // (http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8091877).
        // So image is prepared in AWT and then converted to JFX
        logger.log(Level.FINE, "updateImageBuffer");

        final Rectangle area_copy = area;
        if (area_copy.width <= 0  ||  area_copy.height <= 0)
            return;

        in_update = true;
        final BufferedImage image = new BufferedImage(area_copy.width, area_copy.height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D gc = image.createGraphics();

        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gc.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        gc.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        gc.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Get safe copy of the data
        // (not synchronized, i.e. width vs. data may be inconsistent,
        //  but at least data won't change within this method)
        final int data_width = this.data_width, data_height = this.data_height;
        final ListNumber numbers = this.image_data;
        final boolean unsigned = this.unsigned_data;
        double min = this.min, max = this.max;
        final DoubleFunction<Color> color_mapping = this.color_mapping;

        if (autoscale  &&  numbers != null)
        {   // Compute min..max before layout of color bar
            final IteratorNumber iter = numbers.iterator();
            min = Double.MAX_VALUE;
            max = Double.NEGATIVE_INFINITY;
            while (iter.hasNext())
            {
                final double sample = iter.nextDouble();
                if (sample > max)
                    max = sample;
                if (sample < min)
                    min = sample;
            }
            logger.log(Level.FINE, "Autoscale range {0} .. {1}", new Object[] { min, max });
        }
        colorbar_axis.setValueRange(min, max);

        if (need_layout.getAndSet(false))
            computeLayout(gc, area_copy, min, max);

        // TODO Fill with a 'background' color instead of white
        gc.setColor(Color.WHITE);
        gc.fillRect(0, 0, area_copy.width, area_copy.height);

        // Debug: Show exact outer rim
//        gc.setColor(Color.RED);
//        gc.drawLine(0, 0, image_area.width-1, 0);
//        gc.drawLine(image_area.width-1, 0, image_area.width-1, image_area.height-1);
//        gc.drawLine(image_area.width-1, image_area.height-1, 0, image_area.height-1);
//        gc.drawLine(0, image_area.height-1, 0, 0);

        if (colorbar_area != null)
        {
            final BufferedImage bar = drawColorBar(min, max, color_mapping);
            gc.drawImage(bar, colorbar_area.x, colorbar_area.y, colorbar_area.width, colorbar_area.height, null);
            colorbar_axis.paint(gc, colorbar_area);
        }

        // Paint the image
        final BufferedImage unscaled = drawData(data_width, data_height, numbers, unsigned, min, max, color_mapping);
        if (unscaled != null)
        {
            // Transform from full axis range into data range,
            // using the current 'zoom' state of each axis
            final LinearScreenTransform t = new LinearScreenTransform();
            AxisRange<Double> zoomed = x_axis.getValueRange();
            t.config(min_x, max_x, 0, data_width);
            final int sx1 = Math.max(0, (int)t.transform(zoomed.low));
            final int sx2 = Math.min(data_width, (int)t.transform(zoomed.high));

            // For Y axis, min_y == bottom == data_height
            zoomed = y_axis.getValueRange();
            t.config(min_y, max_y, data_height, 0);
            final int sy1 = Math.max(0, (int)t.transform(zoomed.high));
            final int sy2 = Math.min(data_height, (int)t.transform(zoomed.low));
            gc.drawImage(unscaled,
                         image_area.x, image_area.y, image_area.x + image_area.width, image_area.y + image_area.height,
                         sx1,  sy1,  sx2,  sy2,
                         /* ImageObserver */ null);
        }

        y_axis.paint(gc, image_area);
        x_axis.paint(gc, image_area);

        gc.dispose();

        in_update = false;

        // Convert to JFX
        plot_image = SwingFXUtils.toFXImage(image, null);
    }

    private BufferedImage drawColorBar(final double min, final double max, final DoubleFunction<Color> color_mapping)
    {
        final BufferedImage image = new BufferedImage(1, 256, BufferedImage.TYPE_INT_ARGB);
        for (int value=0; value<256; ++value)
        {
            final Color color = color_mapping.apply((255-value)/255.0);
            image.setRGB(0, value, color.getRGB());
        }
        return image;
    }

    // private static long avg_nano = 0, runs = 0;

    /** @param data_width
     *  @param data_height
     *  @param numbers
     *  @param unsigned
     *  @param min
     *  @param max
     *  @param color_mapping
     *  @return {@link BufferedImage}, sized to match data
     */
    private static BufferedImage drawData(final int data_width, final int data_height, final ListNumber numbers,
                                          final boolean unsigned,
                                          final double min, final double max, final DoubleFunction<Color> color_mapping)
    {
        // Create image that'll be written with data
        if (data_width <= 0  ||  data_height <= 0)
        {
            logger.log(Level.FINE, "Cannot draw image sized {0} x {1}", new Object[] { data_width, data_height });
            return null;
        }
        if (numbers.size() < data_width * data_height)
        {
            logger.log(Level.SEVERE, "Image sized {0} x {1} received only {2} data samples",
                                     new Object[] { data_width, data_height, numbers.size() });
            return null;
        }
        final BufferedImage image = new BufferedImage(data_width, data_height, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D gc = image.createGraphics();
        if (min < max) // Implies min and max being finite, not-NaN
        {
            // Draw each pixel
            final IteratorNumber iter = numbers.iterator();
            // final long start = System.nanoTime();
            for (int y=0; y<data_height; ++y)
            {
                for (int x=0; x<data_width; ++x)
                {
                    final double sample = unsigned ? Integer.toUnsignedLong(iter.nextInt()) : iter.nextDouble();
                    double scaled = (sample - min) / (max - min);
                    if (scaled < 0.0)
                        scaled = 0;
                    if (scaled > 1.0)
                        scaled = 1.0;
                    final Color color = color_mapping.apply(scaled);
                    // What's faster: gc.setColor(color) and gc.drawLine(x, y, x, y) or gc.fillRect(x, y, 1, 1),
                    // or direct pixel access?
                    // Test image showed ~52ms for drawLine, ~13ms for setRGB
                    // No difference for BufferedImage.TYPE_INT_ARGB vs. BufferedImage.TYPE_INT_RGB
                    image.setRGB(x, y, color.getRGB());
                }
            }
            // final long nano = System.nanoTime() - start;
            // avg_nano = (avg_nano*3 + nano)/4;
            // if (++runs > 100)
            // {
            //     runs = 0;
            //     System.out.println("ms: " + nano/1e6);
            // }
        }
        else
        {
            logger.log(Level.WARNING, "Invalid value range {0} .. {1}", new Object[] { min, max });
            final Color color = color_mapping.apply(0.0);
            gc.setColor(color);
            gc.fillRect(0, 0, data_width, data_height);
        }
        gc.dispose();

        return image;
    }

    /** Draw visual feedback (rubber band rectangle etc.)
     *  for current mouse mode
     *  @param gc GC
     */
    private void drawMouseModeFeedback(final GraphicsContext gc)
    {   // Safe copy, then check null (== isPresent())
        final Point2D current = mouse_current.orElse(null);
        if (current == null)
            return;


        final Point2D start = mouse_start.orElse(null);

        final Rectangle plot_bounds = image_area;

        if (mouse_mode == MouseMode.ZOOM_IN)
        {   // Update mouse pointer in ready-to-zoom mode
            if (plot_bounds.contains(current.getX(), current.getY()))
                doSetCursor(cursor_zoom_in);
            else if (x_axis.getBounds().contains(current.getX(), current.getY()))
                doSetCursor(Cursor.H_RESIZE);
            else if (y_axis.getBounds().contains(current.getX(), current.getY()))
                doSetCursor(Cursor.V_RESIZE);
            else
                doSetCursor(Cursor.DEFAULT);
        }
        else if (mouse_mode == MouseMode.ZOOM_IN_X  &&  start != null)
        {
            final int left = (int) Math.min(start.getX(), current.getX());
            final int right = (int) Math.max(start.getX(), current.getX());
            final int width = right - left;
            final int mid_y = plot_bounds.y + plot_bounds.height / 2;
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
        else if (mouse_mode == MouseMode.ZOOM_IN_Y  &&  start != null)
        {
            final int top = (int) Math.min(start.getY(), current.getY());
            final int bottom = (int) Math.max(start.getY(), current.getY());
            final int height = bottom - top;
            final int mid_x = plot_bounds.x + plot_bounds.width / 2;
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
        else if (mouse_mode == MouseMode.ZOOM_IN_PLOT  &&  start != null)
        {
            final int left = (int) Math.min(start.getX(), current.getX());
            final int right = (int) Math.max(start.getX(), current.getX());
            final int top = (int) Math.min(start.getY(), current.getY());
            final int bottom = (int) Math.max(start.getY(), current.getY());
            final int width = right - left;
            final int height = bottom - top;
            final int mid_x = left + width / 2;
            final int mid_y = top + height / 2;
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
    }

    /** Set cursor.
     *
     *  <p>There is already <code>Node.setCursor()</code>
     *  which sets the cursor for just this node.
     *  But that has no affect when JFX is hosted
     *  inside an SWT FXCanvas.
     *  (https://bugs.openjdk.java.net/browse/JDK-8088147)
     *
     *  <p>We set the cursor of the _scene_, and monitor
     *  the scene's cursor in the RCP code that creates the
     *  FXCanvas to then update the SWT cursor.
     *
     *  @param cursor
     */
    private void doSetCursor(final Cursor cursor)
    {
        if (cursor == getCursor())
            return;

        setCursor(cursor);

        final Scene scene = getScene();
        if (scene != null)
            scene.setCursor(cursor);
    }

    /** onMouseEntered */
    private void mouseEntered(final MouseEvent e)
    {
        getScene().setCursor(getCursor());
    }

    /** onMousePressed */
    private void mouseDown(final MouseEvent e)
    {
        // Don't start mouse actions when user invokes context menu
        if (! e.isPrimaryButtonDown()  ||  (PlatformInfo.is_mac_os_x && e.isControlDown()))
            return;
        final Point2D current = new Point2D(e.getX(), e.getY());
        mouse_start = mouse_current = Optional.of(current);

        if (mouse_mode == MouseMode.ZOOM_IN)
        {   // Determine start of 'rubberband' zoom.
            // Reset cursor from SIZE* to CROSS.
            if (y_axis.getBounds().contains(current.getX(), current.getY()))
            {
                mouse_mode = MouseMode.ZOOM_IN_Y;
                doSetCursor(Cursor.CROSSHAIR);
            }
            else if (image_area.contains(current.getX(), current.getY()))
            {
                mouse_mode = MouseMode.ZOOM_IN_PLOT;
                doSetCursor(Cursor.CROSSHAIR);
            }
            else if (x_axis.getBounds().contains(current.getX(), current.getY()))
            {
                mouse_mode = MouseMode.ZOOM_IN_X;
                doSetCursor(Cursor.CROSSHAIR);
            }
        }
    }

    /** setOnMouseMoved */
    private void mouseMove(final MouseEvent e)
    {
        final Point2D current = new Point2D(e.getX(), e.getY());
        mouse_current = Optional.of(current);
        updateLocationInfo(e.getX(), e.getY());
        redrawSafely();
    }

    /** Update information about the image location under the mouse pointer
     *  @param mouse_x
     *  @param mouse_y
     */
    private void updateLocationInfo(final double mouse_x, final double mouse_y)
    {
        if (! image_area.contains(mouse_x, mouse_y))
        {
            System.out.println("Outside of image");
            return;
        }
        final int screen_x = (int) (mouse_x + 0.5);
        final int screen_y = (int) (mouse_y + 0.5);
        // Location on axes, i.e. what user configured as horizontal and vertical values
        final double x_val = x_axis.getValue(screen_x);
        final double y_val = y_axis.getValue(screen_y);

        // Location as coordinate into image
        AxisRange<Double> range = x_axis.getValueRange();
        int image_x = (int) ((data_width-1) * (x_val - range.low) / (range.high - range.low) + 0.5);
        if (image_x >= data_width)
            image_x = data_width - 1;
        range = y_axis.getValueRange();
        int image_y = (int) ((data_height-1) * (1.0 - (y_val - range.low) / (range.high - range.low)) + 0.5);
        if (image_y >= data_height)
            image_y = data_height - 1;

        final ListNumber data = image_data;
        final double pixel = data == null ? -1 : data.getDouble(image_x + image_y * data_width);
        final String info = formatLocationInfo(x_val, y_val, pixel);

        // TODO Set 'info' text, and show that in the redraw_runnable
        System.out.println(info);
    }

    /** setOnMouseReleased */
    private void mouseUp(final MouseEvent e)
    {
        final Point2D start = mouse_start.orElse(null);
        final Point2D current = mouse_current.orElse(null);
        if (start == null  ||  current == null)
            return;

        if (mouse_mode == MouseMode.ZOOM_IN_X)
        {   // X axis increases going _right_ just like mouse 'x' coordinate
            if (Math.abs(start.getX() - current.getX()) > ZOOM_PIXEL_THRESHOLD)
            {
                final int low = (int) Math.min(start.getX(), current.getX());
                final int high = (int) Math.max(start.getX(), current.getX());
                final AxisRange<Double> original_x_range = x_axis.getValueRange();
                final AxisRange<Double> new_x_range = new AxisRange<>(Math.max(min_x, x_axis.getValue(low)),
                                                                      Math.min(max_x, x_axis.getValue(high)));
                undo.execute(new ChangeImageZoom(x_axis, original_x_range, new_x_range, null, null, null));
            }
            mouse_mode = MouseMode.ZOOM_IN;
        }
        else if (mouse_mode == MouseMode.ZOOM_IN_Y)
        {   // Mouse 'y' increases going _down_ the screen
            if (Math.abs(start.getY() - current.getY()) > ZOOM_PIXEL_THRESHOLD)
            {
                final int high = (int)Math.min(start.getY(), current.getY());
                final int low = (int) Math.max(start.getY(), current.getY());
                final AxisRange<Double> original_y_range = y_axis.getValueRange();
                final AxisRange<Double> new_y_range = new AxisRange<>(Math.max(min_y, y_axis.getValue(low)),
                                                                      Math.min(max_y, y_axis.getValue(high)));
                undo.execute(new ChangeImageZoom(null, null, null, y_axis, original_y_range, new_y_range));
            }
            mouse_mode = MouseMode.ZOOM_IN;
        }
        else if (mouse_mode == MouseMode.ZOOM_IN_PLOT)
        {
            if (Math.abs(start.getX() - current.getX()) > ZOOM_PIXEL_THRESHOLD  ||
                Math.abs(start.getY() - current.getY()) > ZOOM_PIXEL_THRESHOLD)
            {   // X axis increases going _right_ just like mouse 'x' coordinate
                int low = (int) Math.min(start.getX(), current.getX());
                int high = (int) Math.max(start.getX(), current.getX());
                final AxisRange<Double> original_x_range = x_axis.getValueRange();
                final AxisRange<Double> new_x_range = new AxisRange<>(Math.max(min_x, x_axis.getValue(low)),
                                                                      Math.min(max_x, x_axis.getValue(high)));
                // Mouse 'y' increases going _down_ the screen
                high = (int) Math.min(start.getY(), current.getY());
                low = (int) Math.max(start.getY(), current.getY());
                final AxisRange<Double> original_y_range = y_axis.getValueRange();
                final AxisRange<Double> new_y_range = new AxisRange<>(Math.max(min_y, y_axis.getValue(low)),
                                                                      Math.min(max_y, y_axis.getValue(high)));
                undo.execute(new ChangeImageZoom(x_axis, original_x_range, new_x_range,
                                                 y_axis, original_y_range, new_y_range));
            }
            mouse_mode = MouseMode.ZOOM_IN;
        }
    }

    /** setOnMouseExited */
    private void mouseExit(final MouseEvent e)
    {
        doSetCursor(null);
        updateLocationInfo(-1, -1);
    }

    /** Zoom in/out triggered by mouse wheel
     *  @param event Scroll event
     */
    private void wheelZoom(final ScrollEvent event)
    {
        // Invoked by mouse scroll wheel.
        // Only allow zoom (with control), not pan.
        if (! event.isControlDown())
            return;

        if (event.getDeltaY() > 0)
            zoomInOut(event.getX(), event.getY(), 1.0/ZOOM_FACTOR);
        else if (event.getDeltaY() < 0)
            zoomInOut(event.getX(), event.getY(), ZOOM_FACTOR);

    }

    /** Zoom 'in' or 'out' from where the mouse was clicked
     *  @param x Mouse coordinate
     *  @param y Mouse coordinate
     *  @param factor Zoom factor, positive to zoom 'out'
     */
    private void zoomInOut(final double x, final double y, final double factor)
    {
        final boolean zoom_x = x_axis.getBounds().contains(x, y);
        final boolean zoom_y = y_axis.getBounds().contains(x, y);
        final boolean zoom_both = image_area.getBounds().contains(x, y);
        AxisRange<Double> orig_x = null, orig_y = null;
        if (zoom_x || zoom_both)
            orig_x = zoomAxis(x_axis, (int)x, factor, min_x, max_x);
        if (zoom_y || zoom_both)
            orig_y = zoomAxis(y_axis, (int)y, factor, min_y, max_y);
        if (zoom_both)
            undo.add(new ChangeImageZoom(x_axis, orig_x, x_axis.getValueRange(),
                                         y_axis, orig_y, y_axis.getValueRange()));
        else if (zoom_x)
            undo.add(new ChangeImageZoom(x_axis, orig_x, x_axis.getValueRange(), null, null, null));
        else if (zoom_y)
            undo.add(new ChangeImageZoom(null, null, null, y_axis, orig_y, y_axis.getValueRange()));
    }

    /** Zoom one axis 'in' or 'out' around a position on the axis
     *  @param axis Axis to zoom
     *  @param pos Screen coordinate on the axis
     *  @param factor Zoom factor
     *  @param min Minimum and ..
     *  @param max .. maximum value for axis range
     *  @return
     */
    private AxisRange<Double> zoomAxis(final AxisPart<Double> axis, final int pos, final double factor,
                                       final double min, final double max)
    {
        final AxisRange<Double> orig = axis.getValueRange();
        final double fixed = axis.getValue(pos);
        final double new_low  = fixed - (fixed - orig.getLow()) * factor;
        final double new_high = fixed + (orig.getHigh() - fixed) * factor;
        axis.setValueRange(Math.max(min, new_low), Math.min(max, new_high));
        return orig;
    }

    private String formatLocationInfo(final double x, final double y, final double value)
    {   // TODO Allow script to install alternate format,
        // or send x, y and value to PVs
        return "(" + x + ", " + y + ") = " + value;
    }

    /** Should be invoked when plot no longer used to release resources */
    public void dispose()
    {   // Stop updates which could otherwise still use
        // what's about to be disposed
        update_throttle.dispose();
    }
}
