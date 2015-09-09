/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.properties.ColorMap;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.widgets.ImageWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.epics.util.array.IteratorNumber;
import org.epics.util.array.ListNumber;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VType;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ImageRepresentation extends JFXBaseRepresentation<Node, ImageWidget>
{
    private final DirtyFlag dirty_position = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();

    /** Most recent image, prepared in background */
    // Would like to use JFX WritableImage,
    // but rendering problem on Linux (sandbox.ImageScaling),
    // and no way to disable the color interpolation that 'smears'
    // the scaled image.
    // (http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8091877).
    // So image is prepared in AWT and then converted to JFX
    private volatile Image image;

    /** Canvas that displays the image. */
    // Use ImageView + setFitWidth(width), setFitHeight(height)?
    private Canvas canvas;

    // TODO Axes, axis info for cursor
    // TODO Zoom in and back out

    public ImageRepresentation(final ToolkitRepresentation<Group, Node> toolkit,
                               final ImageWidget model_widget)
    {
        super(toolkit, model_widget);
    }

    @Override
    public Canvas createJFXNode() throws Exception
    {
        canvas = new Canvas();
        // For now only one 'canvas' Node.
        // This will change when axes and color bar are added.
        return canvas;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addPropertyListener(this::positionChanged);
        model_widget.positionHeight().addPropertyListener(this::positionChanged);
        model_widget.behaviorDataWidth().addPropertyListener(this::contentChanged);
        model_widget.behaviorDataHeight().addPropertyListener(this::contentChanged);
        model_widget.runtimeValue().addPropertyListener(this::contentChanged);
    }

    private void positionChanged(final PropertyChangeEvent event)
    {
        dirty_position.mark();
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    private void contentChanged(final PropertyChangeEvent event)
    {
        image = getImage();
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    /** @return JFX Image, scaled to match canvas */
    private Image getImage()
    {
        final BufferedImage unscaled = drawData();

        final int w = model_widget.positionWidth().getValue();
        final int h = model_widget.positionHeight().getValue();
        final BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        final Graphics2D gc = scaled.createGraphics();
        // SWT and JFX image scaling will by default interpolate.
        // For detector displays, it's best to NOT interpolate and instead show the plain pixels.
        // gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gc.drawImage(unscaled, 0, 0, w, h, null);
        gc.dispose();

        // Convert to JFX
        return SwingFXUtils.toFXImage(scaled, null);
    }

    /** @return {@link BufferedImage}, sized to match data */
    private BufferedImage drawData()
    {
        // Determine sizes
        final int data_width = model_widget.behaviorDataWidth().getValue();
        final int data_height = model_widget.behaviorDataHeight().getValue();
        double min = model_widget.behaviorDataMinimum().getValue();
        double max = model_widget.behaviorDataMaximum().getValue();
        final ColorMap color_map = model_widget.behaviorDataColormap().getValue();

        // Create image that'll be written with data
        final BufferedImage image = new BufferedImage(data_width, data_height, BufferedImage.TYPE_INT_RGB);

        // Check data
        final VType value = model_widget.runtimeValue().getValue();
        if (! (value instanceof VNumberArray))
        {
            logger.log(Level.WARNING, "Cannot draw image from {0}", value);
        }
        else
        {
            final ListNumber numbers = ((VNumberArray) value).getData();
            if (numbers.size() < data_width * data_height)
            {
                logger.log(Level.SEVERE,
                           "Image sized {0} x {1} received only {2} data samples",
                           new Object[] { data_width, data_height, numbers.size() });
                return image;
            }

            IteratorNumber iter = numbers.iterator();
            if (true) // TODO If autoscale..
            {
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
                iter = numbers.iterator();
            }
            // Draw each pixel
            Graphics2D gc = image.createGraphics();
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
                    final WidgetColor color = color_map.getColor(scaled);
                    gc.setColor(new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue()));
                    gc.fillRect(x, y, 1, 1);
                }
            }
            gc.dispose();
        }
        return image;
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_position.checkAndClear())
        {
            final int w = model_widget.positionWidth().getValue();
            final int h = model_widget.positionHeight().getValue();
            canvas.setWidth(w);
            canvas.setHeight(h);
        }
        if (dirty_content.checkAndClear())
        {
            final Image copy = image;
            if (copy != null)
            {
                final GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.drawImage(copy, 0, 0, canvas.getWidth(), canvas.getHeight());
            }
        }
    }
}
