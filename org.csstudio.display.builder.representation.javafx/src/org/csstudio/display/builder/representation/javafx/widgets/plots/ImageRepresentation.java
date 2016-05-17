/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets.plots;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.ColorMap;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.widgets.plots.ImageWidget;
import org.csstudio.display.builder.representation.javafx.widgets.RegionBaseRepresentation;
import org.csstudio.javafx.rtplot.ImagePlot;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VType;

import javafx.scene.layout.Pane;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ImageRepresentation extends RegionBaseRepresentation<Pane, ImageWidget>
{
    private final DirtyFlag dirty_position = new DirtyFlag();

    /** Actual plotting delegated to {@link ImagePlot} */
    private final ImagePlot image_plot = new ImagePlot();

    @Override
    public Pane createJFXNode() throws Exception
    {
        image_plot.setAutoscale(false);
        return new Pane(image_plot);
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::positionChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::positionChanged);

        model_widget.behaviorDataColormap().addPropertyListener(this::colormapChanged);

        model_widget.displayShowColormap().addUntypedPropertyListener(this::configChanged);
        model_widget.displayColormapSize().addUntypedPropertyListener(this::configChanged);
        model_widget.behaviorDataAutoscale().addUntypedPropertyListener(this::configChanged);
        model_widget.behaviorDataMinimum().addUntypedPropertyListener(this::configChanged);
        model_widget.behaviorDataMaximum().addUntypedPropertyListener(this::configChanged);

        model_widget.behaviorDataWidth().addUntypedPropertyListener(this::contentChanged);
        model_widget.behaviorDataHeight().addUntypedPropertyListener(this::contentChanged);
        model_widget.runtimeValue().addUntypedPropertyListener(this::contentChanged);

        // Initial update
        colormapChanged(null, null, model_widget.behaviorDataColormap().getValue());
        configChanged(null, null, null);
    }

    private void positionChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_position.mark();
        toolkit.scheduleUpdate(this);
    }

    private void colormapChanged(final WidgetProperty<ColorMap> property, final ColorMap old_value, final ColorMap colormap)
    {
        image_plot.setColorMapping(value ->
        {
            final WidgetColor color = colormap.getColor(value);
            return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
        });
    }

    private void configChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        image_plot.showColorMap(model_widget.displayShowColormap().getValue());
        image_plot.setColorMapSize(model_widget.displayColormapSize().getValue());
        image_plot.setAutoscale(model_widget.behaviorDataAutoscale().getValue());
        image_plot.setValueRange(model_widget.behaviorDataMinimum().getValue(),
                            model_widget.behaviorDataMaximum().getValue());
    }

    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        final VType value = model_widget.runtimeValue().getValue();
        if (value instanceof VNumberArray)
        {
            final ListNumber numbers = ((VNumberArray) value).getData();
            image_plot.setValue(model_widget.behaviorDataWidth().getValue(),
                                model_widget.behaviorDataHeight().getValue(), numbers);
        }
        else if (value != null)
            logger.log(Level.WARNING, "Cannot draw image from {0}", value);
        // else: Ignore null values
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_position.checkAndClear())
        {
            final int w = model_widget.positionWidth().getValue();
            final int h = model_widget.positionHeight().getValue();
            image_plot.setWidth(w);
            image_plot.setHeight(h);
        }
        image_plot.requestUpdate();
    }
}
