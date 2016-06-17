/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets.plots;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.ColorMap;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.widgets.plots.ImageWidget;
import org.csstudio.display.builder.model.widgets.plots.ImageWidget.AxisWidgetProperty;
import org.csstudio.display.builder.model.widgets.plots.ImageWidget.ROIWidgetProperty;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.display.builder.representation.javafx.widgets.RegionBaseRepresentation;
import org.csstudio.javafx.rtplot.Axis;
import org.csstudio.javafx.rtplot.RTImagePlot;
import org.csstudio.javafx.rtplot.RTImagePlotListener;
import org.diirt.util.array.ArrayByte;
import org.diirt.util.array.ArrayDouble;
import org.diirt.vtype.VImage;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueFactory;

import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Pane;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ImageRepresentation extends RegionBaseRepresentation<Pane, ImageWidget>
{
    private final DirtyFlag dirty_position = new DirtyFlag();

    /** Actual plotting delegated to {@link RTImagePlot} */
    private RTImagePlot image_plot;

    private final static List<String> cursor_info_names = Arrays.asList("X", "Y", "Value");
    private final static List<Class<?>> cursor_info_types = Arrays.asList(Double.TYPE, Double.TYPE, Double.TYPE);

    private final RTImagePlotListener plot_listener = new RTImagePlotListener()
    {
        @Override
        public void changedCursorLocation(final double x, final double y, final double value)
        {
            model_widget.runtimeCursorInfo().setValue(
                ValueFactory.newVTable(cursor_info_types,
                                       cursor_info_names,
                                       Arrays.asList(new ArrayDouble(x), new ArrayDouble(y), new ArrayDouble(value))));
        }

        @Override
        public void changedROI(final String name, final Rectangle2D region)
        {
            // TODO Auto-generated method stub
            System.out.println(name + " moved to " + region);
        }
    };

    @Override
    public Pane createJFXNode() throws Exception
    {
        // Plot is only active in runtime mode, not edit mode
        image_plot = new RTImagePlot(! toolkit.isEditMode());
        image_plot.setAutoscale(false);

        if (! toolkit.isEditMode())
        {
            // Create ROIs once. Not allowing adding/removing ROIs in runtime.
            for (ROIWidgetProperty roi : model_widget.miscROIs().getValue())
            {
                image_plot.addROI(roi.name().getValue(),
                                  JFXUtil.convert(roi.color().getValue()),
                                  roi.visible().getValue());
                // TODO Show/hide ROI as roi.visible() changes
                // TODO Bind, bidirectionally, ROI position to roi.x_value(), y_value(), wid, hei
            }
        }

        return new Pane(image_plot);
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::positionChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::positionChanged);

        model_widget.displayDataColormap().addPropertyListener(this::colormapChanged);
        model_widget.displayColorbar().visible().addUntypedPropertyListener(this::configChanged);
        model_widget.displayColorbar().barSize().addUntypedPropertyListener(this::configChanged);
        model_widget.displayColorbar().scaleFont().addUntypedPropertyListener(this::configChanged);
        addAxisListener(model_widget.displayXAxis());
        addAxisListener(model_widget.displayYAxis());


        model_widget.behaviorDataAutoscale().addUntypedPropertyListener(this::configChanged);
        model_widget.behaviorDataMinimum().addUntypedPropertyListener(this::configChanged);
        model_widget.behaviorDataMaximum().addUntypedPropertyListener(this::configChanged);

        model_widget.behaviorDataWidth().addUntypedPropertyListener(this::contentChanged);
        model_widget.behaviorDataHeight().addUntypedPropertyListener(this::contentChanged);
        model_widget.runtimeValue().addUntypedPropertyListener(this::contentChanged);

        image_plot.setListener(plot_listener);

        // Initial update
        colormapChanged(null, null, model_widget.displayDataColormap().getValue());
        configChanged(null, null, null);
    }

    private void addAxisListener(final AxisWidgetProperty axis)
    {
        axis.visible().addUntypedPropertyListener(this::configChanged);
        axis.title().addUntypedPropertyListener(this::configChanged);
        axis.minimum().addUntypedPropertyListener(this::configChanged);
        axis.maximum().addUntypedPropertyListener(this::configChanged);
        axis.titleFont().addUntypedPropertyListener(this::configChanged);
        axis.scaleFont().addUntypedPropertyListener(this::configChanged);
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
        image_plot.showColorMap(model_widget.displayColorbar().visible().getValue());
        image_plot.setColorMapSize(model_widget.displayColorbar().barSize().getValue());
        image_plot.setColorMapFont(JFXUtil.convert(model_widget.displayColorbar().scaleFont().getValue()));
        image_plot.setAxisRange(model_widget.displayXAxis().minimum().getValue(),
                                model_widget.displayXAxis().maximum().getValue(),
                                model_widget.displayYAxis().minimum().getValue(),
                                model_widget.displayYAxis().maximum().getValue());
        axisChanged(model_widget.displayXAxis(), image_plot.getXAxis());
        axisChanged(model_widget.displayYAxis(), image_plot.getYAxis());
        image_plot.setAutoscale(model_widget.behaviorDataAutoscale().getValue());
        image_plot.setValueRange(model_widget.behaviorDataMinimum().getValue(),
                                 model_widget.behaviorDataMaximum().getValue());
    }

    private void axisChanged(final AxisWidgetProperty property, final Axis<Double> axis)
    {
        axis.setVisible(property.visible().getValue());
        axis.setName(property.title().getValue());
        axis.setLabelFont(JFXUtil.convert(property.titleFont().getValue()));
        axis.setScaleFont(JFXUtil.convert(property.scaleFont().getValue()));
    }

    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        final VType value = model_widget.runtimeValue().getValue();
        if (value instanceof VNumberArray)
            image_plot.setValue(model_widget.behaviorDataWidth().getValue(),
                                model_widget.behaviorDataHeight().getValue(),
                                ((VNumberArray) value).getData(),
                                model_widget.behaviorDataUnsigned().getValue());
        else if (value instanceof VImage)
        {
            final VImage image = (VImage) value;
            image_plot.setValue(image.getWidth(), image.getHeight(), new ArrayByte(image.getData(), true),
                                model_widget.behaviorDataUnsigned().getValue());
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
            image_plot.setPrefWidth(w);
            image_plot.setPrefHeight(h);
        }
        image_plot.requestUpdate();
    }
}
