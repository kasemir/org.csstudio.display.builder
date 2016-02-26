/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.StructuredWidgetProperty;
import org.csstudio.display.builder.model.StructuredWidgetProperty.Descriptor;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.StringWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.diirt.vtype.VType;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that displays X/Y waveforms
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class XYPlotWidget extends VisibleWidget
{
    private static final WidgetPropertyDescriptor<Boolean> behaviorLegend =
        CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "show_legend", "Show Legend");

    // Elements of the 'axis' structure
    private static final WidgetPropertyDescriptor<String> title =
        CommonWidgetProperties.newStringPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "title", "Title");

    private static final WidgetPropertyDescriptor<Boolean> autoscale =
        CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "autoscale", "Auto-scale");

    private final static StructuredWidgetProperty.Descriptor behaviorXAxis =
            new Descriptor(WidgetPropertyCategory.BEHAVIOR, "x_axis", "X Axis");

    private final static StructuredWidgetProperty.Descriptor behaviorYAxis =
            new Descriptor(WidgetPropertyCategory.BEHAVIOR, "y_axis", "Y Axis");

    /** Structure for 'axis' element */
    public static class AxisWidgetProperty extends StructuredWidgetProperty
    {
        public AxisWidgetProperty(final StructuredWidgetProperty.Descriptor axis_descriptor,
                                  final Widget widget, final String title_text)
        {
            super(axis_descriptor, widget,
                  Arrays.asList(title.createProperty(widget, title_text),
                                autoscale.createProperty(widget, false),
                                CommonWidgetProperties.behaviorMinimum.createProperty(widget, 0.0),
                                CommonWidgetProperties.behaviorMaximum.createProperty(widget, 100.0)));
        }
        public WidgetProperty<String> title()        { return getElement(0); }
        public WidgetProperty<Boolean> autoscale()   { return getElement(1); }
        public WidgetProperty<Double> minimum()      { return getElement(2); }
        public WidgetProperty<Double> maximum()      { return getElement(3); }
    };

    /** 'axes' array */
    private static final ArrayWidgetProperty.Descriptor<AxisWidgetProperty> behaviorYAxes =
        new ArrayWidgetProperty.Descriptor<>(WidgetPropertyCategory.BEHAVIOR, "y_axes", "Y Axes",
                                             (widget, index) ->
                                             new AxisWidgetProperty(behaviorYAxis, widget, index > 0 ? "Y " + index : "Y"));

    // Elements of the 'trace' structure
    private static final WidgetPropertyDescriptor<String> traceX =
        CommonWidgetProperties.newStringPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "x_pv", "X PV");
    private static final WidgetPropertyDescriptor<String> traceY =
        CommonWidgetProperties.newStringPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "y_pv", "Y PV");
    private static final WidgetPropertyDescriptor<WidgetColor> traceColor =
        CommonWidgetProperties.newColorPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "color", "Color");
    private static final WidgetPropertyDescriptor<VType> traceXValue = CommonWidgetProperties.newRuntimeValue("x_value", "X Value");
    private static final WidgetPropertyDescriptor<VType> traceYValue = CommonWidgetProperties.newRuntimeValue("y_value", "Y Value");

    private final static StructuredWidgetProperty.Descriptor behaviorTrace =
            new Descriptor(WidgetPropertyCategory.BEHAVIOR, "trace", "Trace");

    /** 'trace' structure */
    public static class TraceWidgetProperty extends StructuredWidgetProperty
    {
        public TraceWidgetProperty(final Widget widget)
        {
            super(behaviorTrace, widget,
                  Arrays.asList(traceX.createProperty(widget, ""),
                                traceY.createProperty(widget, ""),
                                traceColor.createProperty(widget, new WidgetColor(0, 0, 255)),
                                traceXValue.createProperty(widget, null),
                                traceYValue.createProperty(widget, null)  ));
        }
        public WidgetProperty<String> traceX()          { return getElement(0); }
        public WidgetProperty<String> traceY()          { return getElement(1); }
        public WidgetProperty<WidgetColor> traceColor() { return getElement(2); }
        public WidgetProperty<VType> xValue()           { return getElement(3); }
        public WidgetProperty<VType> yValue()           { return getElement(4); }
    };



    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("xyplot", WidgetCategory.PLOT,
            "X/Y Plot",
            "platform:/plugin/org.csstudio.display.builder.model/icons/xyplot.png",
            "Displays waveform PVs",
            Arrays.asList("org.csstudio.opibuilder.widgets.xyGraph"))
    {
        @Override
        public Widget createWidget()
        {
            return new XYPlotWidget();
        }
    };

    /** Configurator that handles legacy properties */
    private static class Configurator extends WidgetConfigurator
    {
        public Configurator(final Version xml_version)
        {
            super(xml_version);
        }

        @Override
        public boolean configureFromXML(final Widget widget, final Element xml)
                throws Exception
        {
            final XYPlotWidget plot = (XYPlotWidget) widget;
            configureAllPropertiesFromMatchingXML(widget, xml);

            // Legacy widget had a "pv_name" property that was basically used as a macro within the widget
            final String pv_macro = XMLUtil.getChildString(xml, "pv_name").orElse("");

            // "axis_0_*" was the X axis config
            XMLUtil.getChildString(xml, "axis_0_axis_title").ifPresent(title ->
            {
                final WidgetProperty<String> property = plot.x_axis.title();
                ((StringWidgetProperty)property).setSpecification(title.replace("$(pv_name)", pv_macro));
            });
            XMLUtil.getChildString(xml, "axis_0_minimum").ifPresent(txt ->
                plot.x_axis.minimum().setValue(Double.parseDouble(txt)) );
            XMLUtil.getChildString(xml, "axis_0_maximum").ifPresent(txt ->
                plot.x_axis.maximum().setValue(Double.parseDouble(txt)) );
            XMLUtil.getChildString(xml, "axis_0_auto_scale").ifPresent(txt ->
                plot.x_axis.autoscale().setValue(Boolean.parseBoolean(txt)) );

            XMLUtil.getChildString(xml, "trace_0_x_pv").ifPresent(pv ->
            {
                final WidgetProperty<String> property = plot.trace.traceX();
                ((StringWidgetProperty)property).setSpecification(pv.replace("$(pv_name)", pv_macro));
            });
            XMLUtil.getChildString(xml, "trace_0_y_pv").ifPresent(pv ->
            {
                final WidgetProperty<String> property = plot.trace.traceY();
                ((StringWidgetProperty)property).setSpecification(pv.replace("$(pv_name)", pv_macro));
            });

            final Element element = XMLUtil.getChildElement(xml, "trace_0_trace_color");
            if (element != null)
                plot.trace.traceColor().readFromXML(element);

            // "axis_1_*" was the Y axis, and higher axes could be either X or Y
            int y_count = 0; // Number of y axes found in legacy config
            for (int legacy_axis=1; /**/; ++legacy_axis)
            {
                // Check for "axis_*_y_axis" (default: true).
                // If _not_, this is an additional X axis which we ignore
                if (! Boolean.parseBoolean(XMLUtil.getChildString(xml, "axis_" + legacy_axis + "_y_axis").orElse("true")))
                        continue;

                // Done reading legacy Y axes?
                final Optional<String> title = XMLUtil.getChildString(xml, "axis_" + legacy_axis + "_axis_title");
                if (! title.isPresent())
                    break;
                // Count actual Y axes, because legacy_axis includes skipped X axes
                ++y_count;

                final AxisWidgetProperty y_axis;
                if (plot.y_axes.size() < y_count)
                {
                    y_axis = new AxisWidgetProperty(behaviorYAxis, widget, title.get());
                    plot.y_axes.addElement(y_axis);
                }
                else
                {
                    y_axis = plot.y_axes.getElement(y_count-1);
                    final WidgetProperty<String> property = y_axis.title();
                    ((StringWidgetProperty)property).setSpecification(title.get().replace("$(pv_name)", pv_macro));
                }

                XMLUtil.getChildString(xml, "axis_" + legacy_axis + "_minimum").ifPresent(txt ->
                    y_axis.minimum().setValue(Double.parseDouble(txt)) );
                XMLUtil.getChildString(xml, "axis_" + legacy_axis + "_maximum").ifPresent(txt ->
                    y_axis.maximum().setValue(Double.parseDouble(txt)) );
                XMLUtil.getChildString(xml, "axis_" + legacy_axis + "_auto_scale").ifPresent(txt ->
                    y_axis.autoscale().setValue(Boolean.parseBoolean(txt)) );
            }
            return true;
        }
    };

    private volatile WidgetProperty<Boolean> show_legend;
    private volatile AxisWidgetProperty x_axis;
    private volatile ArrayWidgetProperty<AxisWidgetProperty> y_axes;
    private volatile TraceWidgetProperty trace;

    public XYPlotWidget()
    {
        super(WIDGET_DESCRIPTOR.getType(), 400, 300);
    }

    @Override
    public WidgetConfigurator getConfigurator(final Version persisted_version) throws Exception
    {
        return new Configurator(persisted_version);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(show_legend = behaviorLegend.createProperty(this, true));
        properties.add(x_axis = new AxisWidgetProperty(behaviorXAxis, this, "X"));
        properties.add(y_axes = behaviorYAxes.createProperty(this, Arrays.asList(new AxisWidgetProperty(behaviorYAxis, this, "Y"))));
        properties.add(trace = new TraceWidgetProperty(this));
    }

    /** @return Behavior 'show_legend' */
    public WidgetProperty<Boolean> behaviorLegend()
    {
        return show_legend;
    }

    /** @return Behavior 'x_axis' */
    public AxisWidgetProperty behaviorXAxis()
    {
        return x_axis;
    }

    /** @return Behavior 'y_axes' */
    public ArrayWidgetProperty<AxisWidgetProperty> behaviorYAxes()
    {
        return y_axes;
    }

    /** @return Behavior 'trace' */
    public TraceWidgetProperty behaviorTrace()
    {
        return trace;
    }
}
