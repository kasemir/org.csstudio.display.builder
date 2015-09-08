/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.widgets.ImageWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.epics.util.array.IteratorNumber;
import org.epics.util.array.ListNumber;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VType;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ImageRepresentation extends JFXBaseRepresentation<Node, ImageWidget>
{
    private final DirtyFlag dirty_position = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();

    private Canvas canvas;
    private volatile WritableImage image = null;

    // TODO Color map
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
        final int data_width = model_widget.behaviorDataWidth().getValue();
        final int data_height = model_widget.behaviorDataHeight().getValue();
        double min = model_widget.behaviorDataMinimum().getValue();
        double max = model_widget.behaviorDataMaximum().getValue();

        final VType value = model_widget.runtimeValue().getValue();

        final WritableImage image = new WritableImage(data_width, data_height);
        final PixelWriter writer = image.getPixelWriter();

        if (value instanceof VNumberArray)
        {
            final ListNumber numbers = ((VNumberArray) value).getData();
            if (numbers.size() < data_width * data_height)
            {
                logger.log(Level.SEVERE,
                           "Image {0} x {1} received only {2} data samples",
                           new Object[] { data_width, data_height, numbers.size() });
                return;
            }

            IteratorNumber iter = numbers.iterator();
            // TODO If autoscale..
            if (true)
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
            for (int y=0; y<data_height; ++y)
                for (int x=0; x<data_width; ++x)
                {
                    double sample = iter.nextDouble();
                    sample = (sample - min) / (max - min);
                    if (sample < 0.0)
                        sample = 0;
                    if (sample > 1.0)
                        sample = 1.0;
                    final Color c = Color.hsb(sample*360.0, 1.0, 1.0);
                    // final Color c = Color.gray(Math.min(1.0,  sample / 20.0));
                    writer.setColor(x, y, c);
                }
        }
        else
        {
            logger.log(Level.WARNING, "Cannot draw image from {0}", value);
        }

        this.image = image;
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
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
            final WritableImage copy = image;
            if (copy != null)
            {
                final GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.drawImage(copy, 0, 0, canvas.getWidth(), canvas.getHeight());
            }
        }
    }
}
