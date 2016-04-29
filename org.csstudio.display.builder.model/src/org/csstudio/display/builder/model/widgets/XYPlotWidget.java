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
import org.csstudio.display.builder.model.persist.ModelReader;
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
@SuppressWarnings("nls") // TODO Externalize strings
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
        /** @param axis_descriptor behaviorXAxis or behaviorYAxis
         *  @param widget
         *  @param title_text
         */
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
    private static final WidgetPropertyDescriptor<Integer> traceYAxis =
        CommonWidgetProperties.newIntegerPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "axis", "Y Axis Index");
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
                  Arrays.asList(CommonWidgetProperties.widgetName.createProperty(widget, ""),
                                traceX.createProperty(widget, ""),
                                traceY.createProperty(widget, ""),
                                traceYAxis.createProperty(widget, 0),
                                traceColor.createProperty(widget, new WidgetColor(0, 0, 255)),
                                traceXValue.createProperty(widget, null),
                                traceYValue.createProperty(widget, null)  ));
        }
        public WidgetProperty<String> traceName()       { return getElement(0); }
        public WidgetProperty<String> traceXPV()        { return getElement(1); }
        public WidgetProperty<String> traceYPV()        { return getElement(2); }
        public WidgetProperty<Integer> traceYAxis()     { return getElement(3); }
        public WidgetProperty<WidgetColor> traceColor() { return getElement(4); }
        public WidgetProperty<VType> traceXValue()      { return getElement(5); }
        public WidgetProperty<VType> traceYValue()      { return getElement(6); }
    };

    /** 'traces' array */
    private static final ArrayWidgetProperty.Descriptor<TraceWidgetProperty> behaviorTraces =
        new ArrayWidgetProperty.Descriptor<>(WidgetPropertyCategory.BEHAVIOR, "traces", "Traces",
                                             (widget, index) ->
                                             new TraceWidgetProperty(widget));

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
        public boolean configureFromXML(final ModelReader model_reader, final Widget widget, final Element xml)
                throws Exception
        {
            final XYPlotWidget plot = (XYPlotWidget) widget;
            configureAllPropertiesFromMatchingXML(model_reader, widget, xml);

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

            handleLegacyValueAxes(widget, xml, pv_macro);

            return handleLegacyTraces(model_reader, widget, xml, pv_macro);
        }

        private void handleLegacyValueAxes(final Widget widget, final Element xml, final String pv_macro)
        {
            final XYPlotWidget plot = (XYPlotWidget) widget;

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
        }

        private boolean handleLegacyTraces(final ModelReader model_reader,
                                           final Widget widget, final Element xml, final String pv_macro) throws Exception
        {
            final XYPlotWidget plot = (XYPlotWidget) widget;

            final int trace_count = XMLUtil.getChildInteger(xml, "trace_count").orElse(0);

            // "trace_0_..." held the trace info
            for (int legacy_trace=0; legacy_trace < trace_count; ++legacy_trace)
            {
                // Was legacy widget used with scalar data, concatenated into waveform?
                final Optional<String> concat = XMLUtil.getChildString(xml, "trace_" + legacy_trace + "_concatenate_data");
                if (concat.isPresent()  &&  concat.get().equals("true"))
                    return false;

                // Y PV
                final String pv_name = XMLUtil.getChildString(xml, "trace_" + legacy_trace + "_y_pv").orElse("");
                final TraceWidgetProperty trace;
                if (plot.traces.size() <= legacy_trace)
                    trace = plot.traces.addElement();
                else
                    trace = plot.traces.getElement(legacy_trace);
                ((StringWidgetProperty)trace.traceYPV()).setSpecification(pv_name.replace("$(pv_name)", pv_macro));

                // X PV
                XMLUtil.getChildString(xml, "trace_" + legacy_trace + "_x_pv").ifPresent(pv ->
                {
                    ((StringWidgetProperty)trace.traceXPV()).setSpecification(pv.replace("$(pv_name)", pv_macro));
                });

                // Color
                final Element element = XMLUtil.getChildElement(xml, "trace_" + legacy_trace + "_trace_color");
                if (element != null)
                    trace.traceColor().readFromXML(model_reader, element);

                // Name. Empty name will result in using the Y PV name
                String name = XMLUtil.getChildString(xml, "trace_" + legacy_trace + "_name").orElse("");
                name = name.replace("$(trace_" + legacy_trace + "_y_pv)", "");

                if (! name.isEmpty())
                    ((StringWidgetProperty)trace.traceName()).setSpecification(name.replace("$(pv_name)", pv_macro));

                // Legacy used index 0=X, 1=Y, 2=Y1, ..
                // except higher axis index could also stand for X1, X2, which we don't handle
                final Optional<Integer> axis_index = XMLUtil.getChildInteger(xml, "trace_" + legacy_trace + "_y_axis_index");
                if (axis_index.isPresent())
                    trace.traceYAxis().setValue(Math.max(0, axis_index.get() - 1));
            }
            return true;
        }
    };

    private volatile WidgetProperty<Boolean> show_legend;
    private volatile AxisWidgetProperty x_axis;
    private volatile ArrayWidgetProperty<AxisWidgetProperty> y_axes;
    private volatile ArrayWidgetProperty<TraceWidgetProperty> traces;

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
        properties.add(traces = behaviorTraces.createProperty(this, Arrays.asList(new TraceWidgetProperty(this))));
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

    /** @return Behavior 'traces' */
    public ArrayWidgetProperty<TraceWidgetProperty> behaviorTraces()
    {
        return traces;
    }
}
