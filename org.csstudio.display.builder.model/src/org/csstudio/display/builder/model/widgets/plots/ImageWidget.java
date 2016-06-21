/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets.plots;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMaximum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMinimum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.StructuredWidgetProperty;
import org.csstudio.display.builder.model.StructuredWidgetProperty.Descriptor;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.ColorMap;
import org.csstudio.display.builder.model.properties.ColorMapWidgetProperty;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.IntegerWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.csstudio.display.builder.model.widgets.VisibleWidget;
import org.diirt.vtype.VType;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that displays an image
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ImageWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("image", WidgetCategory.MONITOR,
            "Image",
            "platform:/plugin/org.csstudio.display.builder.model/icons/image.png",
            "Display image",
            Arrays.asList("org.csstudio.opibuilder.widgets.intensityGraph"))
    {
        @Override
        public Widget createWidget()
        {
            return new ImageWidget();
        }
    };

    /** Color map: Maps values to colors in the image */
    private static final WidgetPropertyDescriptor<ColorMap> dataColormap =
        new WidgetPropertyDescriptor<ColorMap>(WidgetPropertyCategory.DISPLAY, "color_map", Messages.WidgetProperties_ColorMap)
    {
        @Override
        public WidgetProperty<ColorMap> createProperty(final Widget widget, final ColorMap map)
        {
            return new ColorMapWidgetProperty(this, widget, map);
        }
    };

    private static final WidgetPropertyDescriptor<Integer> colorbarSize =
        CommonWidgetProperties.newIntegerPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "bar_size", "Color Bar Size");

    private final static StructuredWidgetProperty.Descriptor displayColorbar =
        new Descriptor(WidgetPropertyCategory.DISPLAY, "color_bar", "Color Bar");

    /** Structure for color bar, the 'legend' that shows the color bar */
    public static class ColorBarProperty extends StructuredWidgetProperty
    {
        public ColorBarProperty(final Widget widget)
        {
            super(displayColorbar, widget,
                  Arrays.asList(CommonWidgetProperties.positionVisible.createProperty(widget, true),
                                colorbarSize.createProperty(widget, 40),
                                PlotWidgetProperties.scaleFont.createProperty(widget, NamedWidgetFonts.DEFAULT)));
        }

        public WidgetProperty<Boolean> visible()        { return getElement(0); }
        public WidgetProperty<Integer> barSize()        { return getElement(1); }
        public WidgetProperty<WidgetFont> scaleFont()   { return getElement(2); }
    };

    /** Structure for X and Y axes */
    public static class AxisWidgetProperty extends StructuredWidgetProperty
    {
        protected AxisWidgetProperty(final StructuredWidgetProperty.Descriptor axis_descriptor,
                                     final Widget widget, final String title_text)
        {
            super(axis_descriptor, widget,
                  Arrays.asList(CommonWidgetProperties.positionVisible.createProperty(widget, true),
                                PlotWidgetProperties.displayTitle.createProperty(widget, title_text),
                                CommonWidgetProperties.behaviorMinimum.createProperty(widget, 0.0),
                                CommonWidgetProperties.behaviorMaximum.createProperty(widget, 100.0),
                                PlotWidgetProperties.titleFontProperty.createProperty(widget, NamedWidgetFonts.DEFAULT_BOLD),
                                PlotWidgetProperties.scaleFont.createProperty(widget, NamedWidgetFonts.DEFAULT)));
        }

        public WidgetProperty<Boolean> visible()        { return getElement(0); }
        public WidgetProperty<String> title()           { return getElement(1); }
        public WidgetProperty<Double> minimum()         { return getElement(2); }
        public WidgetProperty<Double> maximum()         { return getElement(3); }
        public WidgetProperty<WidgetFont> titleFont()   { return getElement(4); }
        public WidgetProperty<WidgetFont> scaleFont()   { return getElement(5); }
    };

    private final static StructuredWidgetProperty.Descriptor displayXAxis =
        new Descriptor(WidgetPropertyCategory.DISPLAY, "x_axis", Messages.PlotWidget_XAxis);

    private final static StructuredWidgetProperty.Descriptor displayYAxis =
        new Descriptor(WidgetPropertyCategory.DISPLAY, "y_axis", Messages.PlotWidget_YAxis);

    /** Structure for X axis */
    private static class XAxisWidgetProperty extends AxisWidgetProperty
    {
        public XAxisWidgetProperty(final Widget widget)
        {
            super(displayXAxis, widget, "X");
        }
    };

    /** Structure for Y axis */
    private static class YAxisWidgetProperty extends AxisWidgetProperty
    {
        public YAxisWidgetProperty(final Widget widget)
        {
            super(displayYAxis, widget, "Y");
        }
    };

    /** Image data information */
    private static final WidgetPropertyDescriptor<Integer> dataWidth =
        new WidgetPropertyDescriptor<Integer>(
            WidgetPropertyCategory.BEHAVIOR, "data_width", Messages.WidgetProperties_DataWidth)
    {
        @Override
        public WidgetProperty<Integer> createProperty(final Widget widget,
                                                      final Integer width)
        {
            return new IntegerWidgetProperty(this, widget, width);
        }
    };

    private static final WidgetPropertyDescriptor<Integer> dataHeight =
        new WidgetPropertyDescriptor<Integer>(
            WidgetPropertyCategory.BEHAVIOR, "data_height", Messages.WidgetProperties_DataHeight)
    {
        @Override
        public WidgetProperty<Integer> createProperty(final Widget widget,
                                                      final Integer height)
        {
            return new IntegerWidgetProperty(this, widget, height);
        }
    };

    private static final WidgetPropertyDescriptor<Boolean> dataUnsigned =
        CommonWidgetProperties.newBooleanPropertyDescriptor(
            WidgetPropertyCategory.BEHAVIOR, "unsigned", Messages.WidgetProperties_UnsignedData);

    /** Runtime info about cursor location */
    private static final WidgetPropertyDescriptor<String> cursorInfoPV =
        CommonWidgetProperties.newStringPropertyDescriptor(WidgetPropertyCategory.MISC, "cursor_info_pv", Messages.WidgetProperties_CursorInfoPV);

    private static final WidgetPropertyDescriptor<VType> cursorInfo =
        CommonWidgetProperties.newRuntimeValue("cursor_info", Messages.WidgetProperties_CursorInfo);

    /** Structure for ROI */
    private static final WidgetPropertyDescriptor<WidgetColor> displayColor =
        CommonWidgetProperties.newColorPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "color", Messages.PlotWidget_Color);

    private static final WidgetPropertyDescriptor<String> behaviorXPVName =
        CommonWidgetProperties.newStringPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "x_pv", Messages.WidgetProperties_XPVName);

    private static final WidgetPropertyDescriptor<String> behaviorYPVName =
        CommonWidgetProperties.newStringPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "y_pv", Messages.WidgetProperties_YPVName);

    private static final WidgetPropertyDescriptor<String> behaviorWidthPVName =
        CommonWidgetProperties.newStringPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "width_pv", Messages.WidgetProperties_WidthPVName);

    private static final WidgetPropertyDescriptor<String> behaviorHeightPVName =
        CommonWidgetProperties.newStringPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "height_pv", Messages.WidgetProperties_HeightPVName);

    public static final WidgetPropertyDescriptor<Double> xValue =
        CommonWidgetProperties.newDoublePropertyDescriptor(WidgetPropertyCategory.RUNTIME, "x_value", Messages.WidgetProperties_X);

    public static final WidgetPropertyDescriptor<Double> yValue =
        CommonWidgetProperties.newDoublePropertyDescriptor(WidgetPropertyCategory.RUNTIME, "y_value", Messages.WidgetProperties_Y);

    public static final WidgetPropertyDescriptor<Double> widthValue =
        CommonWidgetProperties.newDoublePropertyDescriptor(WidgetPropertyCategory.RUNTIME, "width_value", Messages.WidgetProperties_Width);

    public static final WidgetPropertyDescriptor<Double> heightValue =
        CommonWidgetProperties.newDoublePropertyDescriptor(WidgetPropertyCategory.RUNTIME, "height_value", Messages.WidgetProperties_Height);

    private final static StructuredWidgetProperty.Descriptor behaviorROI =
            new Descriptor(WidgetPropertyCategory.DISPLAY, "roi", "Region of Interest");

    public static class ROIWidgetProperty extends StructuredWidgetProperty
    {
        protected ROIWidgetProperty(final Widget widget, final String name)
        {
            super(behaviorROI, widget,
                  Arrays.asList(CommonWidgetProperties.positionVisible.createProperty(widget, true),
                                CommonWidgetProperties.widgetName.createProperty(widget, name),
                                displayColor.createProperty(widget, new WidgetColor(255, 0, 0)),
                                behaviorXPVName.createProperty(widget, ""),
                                behaviorYPVName.createProperty(widget, ""),
                                behaviorWidthPVName.createProperty(widget, ""),
                                behaviorHeightPVName.createProperty(widget, ""),
                                xValue.createProperty(widget, Double.NaN),
                                yValue.createProperty(widget, Double.NaN),
                                widthValue.createProperty(widget, Double.NaN),
                                heightValue.createProperty(widget, Double.NaN) ));
        }

        public WidgetProperty<Boolean> visible()       { return getElement(0); }
        public WidgetProperty<String> name()           { return getElement(1); }
        public WidgetProperty<WidgetColor> color()     { return getElement(2); }
        public WidgetProperty<String> x_pv()           { return getElement(3); }
        public WidgetProperty<String> y_pv()           { return getElement(4); }
        public WidgetProperty<String> width_pv()       { return getElement(5); }
        public WidgetProperty<String> height_pv()      { return getElement(6); }
        public WidgetProperty<Double> x_value()        { return getElement(7); }
        public WidgetProperty<Double> y_value()        { return getElement(8); }
        public WidgetProperty<Double> width_value()    { return getElement(9); }
        public WidgetProperty<Double> height_value()   { return getElement(10); }
    };

    /** 'roi' array */
    public static final ArrayWidgetProperty.Descriptor<ROIWidgetProperty> miscROIs =
        new ArrayWidgetProperty.Descriptor<>(WidgetPropertyCategory.MISC, "rois", "Regions of Interest",
                                             (widget, index) -> new ROIWidgetProperty(widget, "ROI " + index),
                                             0);

    /** Configurator for legacy widgets */
    private class CustomWidgetConfigurator extends WidgetConfigurator
    {
        public CustomWidgetConfigurator(final Version xml_version)
        {
            super(xml_version);
        }

        @Override
        public boolean configureFromXML(final ModelReader model_reader, final Widget widget,
                final Element xml) throws Exception
        {
            if (! super.configureFromXML(model_reader, widget, xml))
                return false;

            if (xml_version.getMajor() < 2)
            {
                final ImageWidget image = (ImageWidget) widget;
                XMLUtil.getChildString(xml, "show_ramp")
                       .ifPresent(show -> image.color_bar.visible().setValue(Boolean.parseBoolean(show)));

                final Element el = XMLUtil.getChildElement(xml, "font");
                if (el != null)
                    image.displayColorbar().scaleFont().readFromXML(model_reader, el);

                XMLUtil.getChildString(xml, "x_axis_visible")
                       .ifPresent(show -> image.x_axis.visible().setValue(Boolean.parseBoolean(show)));
                XMLUtil.getChildDouble(xml, "x_axis_minimum")
                       .ifPresent(value -> image.x_axis.minimum().setValue(value));
                XMLUtil.getChildDouble(xml, "x_axis_maximum")
                       .ifPresent(value -> image.x_axis.maximum().setValue(value));

                XMLUtil.getChildString(xml, "y_axis_visible")
                       .ifPresent(show -> image.y_axis.visible().setValue(Boolean.parseBoolean(show)));
                XMLUtil.getChildDouble(xml, "y_axis_minimum")
                       .ifPresent(value -> image.y_axis.minimum().setValue(value));
                XMLUtil.getChildDouble(xml, "y_axis_maximum")
                       .ifPresent(value -> image.y_axis.maximum().setValue(value));
            }

            return true;
        }
    }

    private volatile WidgetProperty<ColorMap> data_colormap;
    private volatile ColorBarProperty color_bar;
    private volatile AxisWidgetProperty x_axis;
    private volatile AxisWidgetProperty y_axis;
    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<Integer> data_width;
    private volatile WidgetProperty<Integer> data_height;
    private volatile WidgetProperty<Boolean> data_autoscale;
    private volatile WidgetProperty<Double> data_minimum;
    private volatile WidgetProperty<Double> data_maximum;
    private volatile WidgetProperty<Boolean> data_unsigned;
    private volatile WidgetProperty<String> cursor_info_pv;
    private volatile WidgetProperty<VType> cursor_info;
    private volatile ArrayWidgetProperty<ROIWidgetProperty> rois;

    private WidgetProperty<VType> value;

    public ImageWidget()
    {
        super(WIDGET_DESCRIPTOR.getType(), 400, 300);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(displayBorderAlarmSensitive.createProperty(this, true));
        properties.add(data_colormap = dataColormap.createProperty(this, ColorMap.VIRIDIS));
        properties.add(color_bar = new ColorBarProperty(this));
        properties.add(x_axis = new XAxisWidgetProperty(this));
        properties.add(y_axis = new YAxisWidgetProperty(this));
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
        properties.add(data_width = dataWidth.createProperty(this, 100));
        properties.add(data_height = dataHeight.createProperty(this, 100));
        properties.add(data_unsigned = dataUnsigned.createProperty(this, false));
        properties.add(data_autoscale = PlotWidgetProperties.autoscale.createProperty(this, true));
        properties.add(data_minimum = behaviorMinimum.createProperty(this, 0.0));
        properties.add(data_maximum = behaviorMaximum.createProperty(this, 255.0));
        properties.add(value = runtimeValue.createProperty(this, null));
        properties.add(cursor_info_pv = cursorInfoPV.createProperty(this, ""));
        properties.add(cursor_info = cursorInfo.createProperty(this, null));
        properties.add(rois = miscROIs.createProperty(this, Collections.emptyList()));
    }

    @Override
    public WidgetConfigurator getConfigurator(final Version persisted_version)
            throws Exception
    {
        return new CustomWidgetConfigurator(persisted_version);
    }

    /** @return Display 'color_map' */
    public WidgetProperty<ColorMap> displayDataColormap()
    {
        return data_colormap;
    }

    /** @return Display 'color_bar' */
    public ColorBarProperty displayColorbar()
    {
        return color_bar;
    }

    /** @return Display 'x_axis' */
    public AxisWidgetProperty displayXAxis()
    {
        return x_axis;
    }

    /** @return Display 'y_axis' */
    public AxisWidgetProperty displayYAxis()
    {
        return y_axis;
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return pv_name;
    }

    /** @return Behavior 'data_width' */
    public WidgetProperty<Integer> behaviorDataWidth()
    {
        return data_width;
    }

    /** @return Behavior 'data_height' */
    public WidgetProperty<Integer> behaviorDataHeight()
    {
        return data_height;
    }

    /** @return Behavior 'unsigned' */
    public WidgetProperty<Boolean> behaviorDataUnsigned()
    {
        return data_unsigned;
    }

    /** @return Behavior 'autoscale' */
    public WidgetProperty<Boolean> behaviorDataAutoscale()
    {
        return data_autoscale;
    }

    /** @return Behavior 'minimum' */
    public WidgetProperty<Double> behaviorDataMinimum()
    {
        return data_minimum;
    }

    /** @return Behavior 'maximum' */
    public WidgetProperty<Double> behaviorDataMaximum()
    {
        return data_maximum;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
    }

    /** @return Misc. 'cursor_info_pv' */
    public WidgetProperty<String> miscCursorInfoPV()
    {
        return cursor_info_pv;
    }

    /** @return Runtime 'cursor_info' */
    public WidgetProperty<VType> runtimeCursorInfo()
    {
        return cursor_info;
    }

    /** @return Misc. 'rois' */
    public ArrayWidgetProperty<ROIWidgetProperty> miscROIs()
    {
        return rois;
    }
}
