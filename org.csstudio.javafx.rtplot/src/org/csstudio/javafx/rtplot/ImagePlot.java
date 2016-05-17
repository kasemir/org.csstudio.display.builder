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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleFunction;
import java.util.logging.Level;

import org.csstudio.javafx.rtplot.internal.AxisPart;
import org.csstudio.javafx.rtplot.internal.HorizontalNumericAxis;
import org.csstudio.javafx.rtplot.internal.PlotPart;
import org.csstudio.javafx.rtplot.internal.PlotPartListener;
import org.csstudio.javafx.rtplot.internal.YAxisImpl;
import org.csstudio.javafx.rtplot.util.RTPlotUpdateThrottle;
import org.diirt.util.array.IteratorNumber;
import org.diirt.util.array.ListNumber;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/** Plot for an image
 *
 *  <p>Displays the intensity of values from a {@link ListNumber}
 *  as an image.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ImagePlot extends Canvas
{
    /** Gray scale color mapping */
    public final static DoubleFunction<Color> GRAYSCALE = value -> new Color((float)value, (float)value, (float)value);

    /** Rainbow color mapping */
    public final static DoubleFunction<Color> RAINBOW = value -> new Color(Color.HSBtoRGB((float)value, 1.0f, 1.0f));

    /** Area of this canvas */
    private volatile Rectangle area = new Rectangle(0, 0, 0, 0);

    /** Does layout need to be re-computed? */
    final private AtomicBoolean need_layout = new AtomicBoolean(true);

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
    private volatile boolean show_colorbar = true;

    /** Color bar size */
    private volatile int colorbar_size = 40;

    /** Mapping of value 0..1 to color */
    private volatile DoubleFunction<Color> color_mapping = GRAYSCALE;

    /** Image data */
    private volatile ListNumber image_data = null;

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
        // TODO drawMouseModeFeedback(gc);
    };

    public ImagePlot()
    {
        x_axis = new HorizontalNumericAxis("X", axis_listener);
        y_axis = new YAxisImpl<>("Y", axis_listener);
        colorbar_axis =  new YAxisImpl<>("", axis_listener);

        // 50Hz default throttle
        update_throttle = new RTPlotUpdateThrottle(50, TimeUnit.MILLISECONDS, () ->
        {
            updateImageBuffer();
            redrawSafely();
        });

        final ChangeListener<? super Number> resize_listener = (prop, old, value) ->
        {
            area = new Rectangle((int)getWidth(), (int)getHeight());
            requestLayout();
        };
        widthProperty().addListener(resize_listener);
        heightProperty().addListener(resize_listener);
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

    /** @param show_colorbar Show color bar? */
    public void showColorBar(final boolean show_colorbar)
    {
        this.show_colorbar = show_colorbar;
        requestLayout();
    }

    /** @param colorbar_size Color bar size in pixels */
    public void setColorBarSize(final int colorbar_size)
    {
        this.colorbar_size = colorbar_size;
        requestLayout();
    }

    /** Set the data to display
     *  @param width Number of elements in one 'row' of data
     *  @param height Number of data rows
     *  @param data Image elements, starting in 'top left' corner,
     *              proceeding along the row, then to next rows
     */
    public void setValue(final int width, final int height, final ListNumber data)
    {
        data_width = width;
        data_height = height;
        image_data = data;
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
        if (show_colorbar)
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
        final BufferedImage unscaled = drawData(data_width, data_height, numbers, min, max, color_mapping);
        if (unscaled != null)
            gc.drawImage(unscaled, image_area.x, image_area.y, image_area.width, image_area.height, null);

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
     *  @param min
     *  @param max
     *  @param color_mapping
     *  @return {@link BufferedImage}, sized to match data
     */
    private static BufferedImage drawData(final int data_width, final int data_height, final ListNumber numbers,
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
                    final double sample = iter.nextDouble();
                    double scaled = (sample - min) / (max - min);
                    if (scaled < 0.0)
                        scaled = 0;
                    if (scaled > 1.0)
                        scaled = 1.0;
                    final Color color = color_mapping.apply(scaled);
                    // What's faster: gc.setColor(color) and gc.drawLine(x, y, x, y) or gc.fillRect(x, y, 1, 1),
                    // ordirect pixel access?
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

    /** Should be invoked when plot no longer used to release resources */
    public void dispose()
    {   // Stop updates which could otherwise still use
        // what's about to be disposed
        update_throttle.dispose();
    }
}
