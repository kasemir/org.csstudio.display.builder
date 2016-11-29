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
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.properties.ColorMap;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.widgets.plots.ImageWidget;
import org.csstudio.display.builder.model.widgets.plots.ImageWidget.AxisWidgetProperty;
import org.csstudio.display.builder.model.widgets.plots.ImageWidget.ROIWidgetProperty;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.display.builder.representation.javafx.widgets.RegionBaseRepresentation;
import org.csstudio.javafx.rtplot.Axis;
import org.csstudio.javafx.rtplot.Interpolation;
import org.csstudio.javafx.rtplot.RTImagePlot;
import org.csstudio.javafx.rtplot.RTImagePlotListener;
import org.csstudio.javafx.rtplot.RegionOfInterest;
import org.diirt.util.array.ArrayByte;
import org.diirt.util.array.ArrayDouble;
import org.diirt.util.array.ArrayInt;
import org.diirt.vtype.VImage;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueFactory;

import javafx.application.Platform;
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

    private volatile boolean changing_roi = false;

    private final static List<String> cursor_info_names = Arrays.asList("X", "Y", "Value", "xi", "yi");
    private final static List<Class<?>> cursor_info_types = Arrays.asList(Double.TYPE, Double.TYPE, Double.TYPE, Integer.TYPE, Integer.TYPE);

    private final RTImagePlotListener plot_listener = new RTImagePlotListener()
    {
        @Override
        public void changedCursorLocation(final double x, final double y, final int xi, final int yi, final double value)
        {
            model_widget.runtimePropCursorInfo().setValue(
                ValueFactory.newVTable(cursor_info_types,
                                       cursor_info_names,
                                       Arrays.asList(new ArrayDouble(x), new ArrayDouble(y),
                                                     new ArrayDouble(value),
                                                     new ArrayInt(xi), new ArrayInt(yi))));
        }

        @Override
        public void changedROI(final int index, final String name, final Rectangle2D region)
        {
            if (changing_roi)
                return;
            final ROIWidgetProperty widget_roi = model_widget.propROIs().getValue().get(index);
            changing_roi =  true;
            widget_roi.x_value().setValue(region.getMinX());
            widget_roi.y_value().setValue(region.getMinY());
            widget_roi.width_value().setValue(region.getWidth());
            widget_roi.height_value().setValue(region.getHeight());
            changing_roi =  false;
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
            for (ROIWidgetProperty roi : model_widget.propROIs().getValue())
                createROI(roi);
        }

        return new Pane(image_plot);
    }

    private void createROI(final ROIWidgetProperty model_roi)
    {
        final RegionOfInterest plot_roi = image_plot.addROI(model_roi.name().getValue(),
                                                            JFXUtil.convert(model_roi.color().getValue()),
                                                            model_roi.visible().getValue());
        // Show/hide ROI as roi.visible() changes
        model_roi.visible().addPropertyListener((prop, old, visible) ->
        {
            plot_roi.setVisible(visible);
            Platform.runLater(() -> image_plot.removeROITracker());
            image_plot.requestUpdate();
        });

        // Listen to roi.x_value(), .. and update plot_roi
        final WidgetPropertyListener<Double> model_roi_listener = (o, old, value) ->
        {
            if (changing_roi)
                return;
            Rectangle2D region = plot_roi.getRegion();
            region = new Rectangle2D(existingOrProperty(region.getMinX(), model_roi.x_value()),
                                     existingOrProperty(region.getMinY(), model_roi.y_value()),
                                     existingOrProperty(region.getWidth(), model_roi.width_value()),
                                     existingOrProperty(region.getHeight(), model_roi.height_value()));
            changing_roi = true;
            plot_roi.setRegion(region);
            changing_roi = false;
            image_plot.requestUpdate();
        };
        model_roi.x_value().addPropertyListener(model_roi_listener);
        model_roi.y_value().addPropertyListener(model_roi_listener);
        model_roi.width_value().addPropertyListener(model_roi_listener);
        model_roi.height_value().addPropertyListener(model_roi_listener);
    }

    /** @param old Existing value
     *  @param prop Property that may have new value, or <code>null</code>
     *  @return Property's value, falling back to old value
     */
    private double existingOrProperty(final double old, final WidgetProperty<Double> prop)
    {
        final Double value = prop.getValue();
        if (value == null)
            return old;
        return value;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.propWidth().addUntypedPropertyListener(this::positionChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::positionChanged);

        model_widget.propBackground().addUntypedPropertyListener(this::configChanged);
        model_widget.propToolbar().addUntypedPropertyListener(this::configChanged);
        model_widget.propDataColormap().addPropertyListener(this::colormapChanged);
        model_widget.propColorbar().visible().addUntypedPropertyListener(this::configChanged);
        model_widget.propColorbar().barSize().addUntypedPropertyListener(this::configChanged);
        model_widget.propColorbar().scaleFont().addUntypedPropertyListener(this::configChanged);
        addAxisListener(model_widget.propXAxis());
        addAxisListener(model_widget.propYAxis());

        model_widget.propDataAutoscale().addUntypedPropertyListener(this::configChanged);
        model_widget.propDataMinimum().addUntypedPropertyListener(this::configChanged);
        model_widget.propDataMaximum().addUntypedPropertyListener(this::configChanged);
        model_widget.propDataInterpolation().addUntypedPropertyListener(this::configChanged);

        model_widget.propDataWidth().addUntypedPropertyListener(this::contentChanged);
        model_widget.propDataHeight().addUntypedPropertyListener(this::contentChanged);
        model_widget.runtimePropValue().addUntypedPropertyListener(this::contentChanged);

        image_plot.setListener(plot_listener);

        // Initial update
        colormapChanged(null, null, model_widget.propDataColormap().getValue());
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
        image_plot.setBackground(JFXUtil.convert(model_widget.propBackground().getValue()));
        image_plot.showToolbar(model_widget.propToolbar().getValue());
        image_plot.showColorMap(model_widget.propColorbar().visible().getValue());
        image_plot.setColorMapSize(model_widget.propColorbar().barSize().getValue());
        image_plot.setColorMapFont(JFXUtil.convert(model_widget.propColorbar().scaleFont().getValue()));
        image_plot.setAxisRange(model_widget.propXAxis().minimum().getValue(),
                                model_widget.propXAxis().maximum().getValue(),
                                model_widget.propYAxis().minimum().getValue(),
                                model_widget.propYAxis().maximum().getValue());
        axisChanged(model_widget.propXAxis(), image_plot.getXAxis());
        axisChanged(model_widget.propYAxis(), image_plot.getYAxis());
        image_plot.setAutoscale(model_widget.propDataAutoscale().getValue());
        image_plot.setInterpolation(Interpolation.values()[model_widget.propDataInterpolation().getValue().ordinal()]);
        image_plot.setValueRange(model_widget.propDataMinimum().getValue(),
                                 model_widget.propDataMaximum().getValue());
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
        final VType value = model_widget.runtimePropValue().getValue();
        if (value instanceof VNumberArray)
            image_plot.setValue(model_widget.propDataWidth().getValue(),
                                model_widget.propDataHeight().getValue(),
                                ((VNumberArray) value).getData(),
                                model_widget.propDataUnsigned().getValue());
        else if (value instanceof VImage)
        {
            final VImage image = (VImage) value;
            image_plot.setValue(image.getWidth(), image.getHeight(), new ArrayByte(image.getData(), true),
                                model_widget.propDataUnsigned().getValue());
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
            final int w = model_widget.propWidth().getValue();
            final int h = model_widget.propHeight().getValue();
            image_plot.setPrefWidth(w);
            image_plot.setPrefHeight(h);
        }
        image_plot.requestUpdate();
    }
}
