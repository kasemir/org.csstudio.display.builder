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
public class ImagePlot extends Canvas
{
    /** Gray scale color mapping */
    public final static DoubleFunction<Color> GRAYSCALE = value -> new Color((float)value, (float)value, (float)value);

    /** Rainbow color mapping */
    public final static DoubleFunction<Color> RAINBOW = value -> new Color(Color.HSBtoRGB((float)value, 1.0f, 1.0f));

    /** Area of this canvas */
    private volatile Rectangle area = new Rectangle(0, 0, 0, 0);

    /** Area used by the image */
    private volatile Rectangle image_area = new Rectangle(0, 0, 0, 0);

    /** Does layout need to be re-computed? */
    final private AtomicBoolean need_layout = new AtomicBoolean(true);

    /** Image data size */
    private volatile int data_width = 0, data_height = 0;

    /** Image data range */
    private volatile double min=0.0, max=0.0;

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

    private final RTPlotUpdateThrottle update_throttle;

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

    /** @param color_mapping Function that returns {@link Color} for value 0.0 .. 1.0 */
    public void setColorMapping(final DoubleFunction<Color> color_mapping)
    {
        this.color_mapping = color_mapping;
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
    private void computeLayout(final Graphics2D gc, final Rectangle bounds)
    {
        logger.log(Level.FINE, "computeLayout");
        // TODO Color bar (Y Axis)..

        image_area = bounds;
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

        final BufferedImage image = new BufferedImage(area_copy.width, area_copy.height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D gc = image.createGraphics();

        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gc.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        gc.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        gc.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // TODO If autoscale, compute min..max here, before layout of color bar

        if (need_layout.getAndSet(false))
            computeLayout(gc, area_copy);

        // Debug: Fill background and exact outer rim
//        gc.setColor(Color.WHITE);
//        gc.fillRect(0, 0, area_copy.width, area_copy.height);
//        gc.setColor(Color.RED);
//        gc.drawLine(0, 0, image_area.width-1, 0);
//        gc.drawLine(image_area.width-1, 0, image_area.width-1, image_area.height-1);
//        gc.drawLine(image_area.width-1, image_area.height-1, 0, image_area.height-1);
//        gc.drawLine(0, image_area.height-1, 0, 0);

        // TODO Paint color bar, y_axis.paint(gc, plot_bounds);

        // Paint the image
        final BufferedImage unscaled = drawData();
        if (unscaled != null)
            gc.drawImage(unscaled, 0, 0, image_area.width, image_area.height, null);

        gc.dispose();

        // Convert to JFX
        plot_image = SwingFXUtils.toFXImage(image, null);
    }

    /** @return {@link BufferedImage}, sized to match data */
    private BufferedImage drawData()
    {
        // Get safe copy of the data
        // (not synchronized, i.e. width vs. data may be inconsistent,
        //  but at least data won't change within this method)
        final int data_width = this.data_width;
        final int data_height = this.data_height;
        double min = this.min;
        double max = this.max;
        final ListNumber numbers = this.image_data;

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
        final BufferedImage image = new BufferedImage(data_width, data_height, BufferedImage.TYPE_INT_RGB);

        if (true) // TODO If autoscale..
        {
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
        final Graphics2D gc = image.createGraphics();
        if (min < max) // Implies min and max being finite, not-NaN
        {
            // Draw each pixel
            final IteratorNumber iter = numbers.iterator();
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
                    gc.setColor(color);
                    // What's faster, gc.fillRect(x, y, 1, 1) or 1-pixel line?
                    gc.drawLine(x, y, x, y);
                }
            }
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
