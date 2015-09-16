/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.BaseWidget;
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
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.ColorWidgetProperty;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.StringWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.epics.vtype.VType;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that displays X/Y waveforms
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class XYPlotWidget extends BaseWidget
{
    /** Trace 'X' PV */
    private static final WidgetPropertyDescriptor<String> traceX =
        new WidgetPropertyDescriptor<String>(
            WidgetPropertyCategory.BEHAVIOR, "x_pv", "X PV")
    {
        @Override
        public WidgetProperty<String> createProperty(final Widget widget, final String pv_name)
        {
            return new StringWidgetProperty(this, widget, pv_name);
        }
    };

    /** Trace 'Y' PV */
    private static final WidgetPropertyDescriptor<String> traceY =
        new WidgetPropertyDescriptor<String>(
            WidgetPropertyCategory.BEHAVIOR, "y_pv", "Y PV")
    {
        @Override
        public WidgetProperty<String> createProperty(final Widget widget, final String pv_name)
        {
            return new StringWidgetProperty(this, widget, pv_name);
        }
    };

    /** Trace color */
    private static final WidgetPropertyDescriptor<WidgetColor> traceColor =
        new WidgetPropertyDescriptor<WidgetColor>(
            WidgetPropertyCategory.DISPLAY, "color", "Color")
    {
        @Override
        public WidgetProperty<WidgetColor> createProperty(final Widget widget, final WidgetColor color)
        {
            return new ColorWidgetProperty(this, widget, color);
        }
    };

    private static final WidgetPropertyDescriptor<VType> traceXValue = CommonWidgetProperties.newRuntimeValue("x_value", "X Value");
    private static final WidgetPropertyDescriptor<VType> traceYValue = CommonWidgetProperties.newRuntimeValue("x_value", "X Value");

    /** Trace */
    private final static StructuredWidgetProperty.Descriptor behaviorTrace =
            new Descriptor(WidgetPropertyCategory.BEHAVIOR, "trace", "Trace");

    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR
        = new WidgetDescriptor("xyplot", WidgetCategory.MONITOR,
                Messages.XYPlotWidget_Name,
                "platform:/plugin/org.csstudio.display.builder.model/icons/xyplot.png",
                Messages.XYPlotWidget_Description,
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
        public void configureFromXML(final Widget widget, final Element xml)
                throws Exception
        {
            final XYPlotWidget plot = (XYPlotWidget) widget;
            configureAllPropertiesFromMatchingXML(widget, xml);

            // Legacy widget had a "pv_name" property that was basically used as a macro
            final String pv_macro = XMLUtil.getChildString(xml, "pv_name").orElse("");

            XMLUtil.getChildString(xml, "trace_0_x_pv").ifPresent(pv ->
            {
                final WidgetProperty<String> property = plot.trace.getElement(0);
               ((StringWidgetProperty)property).setSpecification(pv.replace("$(pv_name)", pv_macro));
            });
            XMLUtil.getChildString(xml, "trace_0_y_pv").ifPresent(pv ->
            {
                final WidgetProperty<String> property = plot.trace.getElement(1);
               ((StringWidgetProperty)property).setSpecification(pv.replace("$(pv_name)", pv_macro));
            });

            Element element = XMLUtil.getChildElement(xml, "trace_0_trace_color");
            if (element != null)
                plot.trace.getElement(2).readFromXML(element);
        }
    };

    // TODO: Support minimum of legacy properties:
    // <axis_0_axis_title>
    // <trace_0_x_pv>
    // <trace_0_y_pv>
    // <trace_0_trace_color>

    // TODO: ArrayWidgetProperty of StructureWidgetProperty

    private StructuredWidgetProperty trace;

    public XYPlotWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
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
        properties.add(trace = behaviorTrace.createProperty(this,
            Arrays.asList(traceX.createProperty(this, ""),
                          traceY.createProperty(this, ""),
                          traceColor.createProperty(this, new WidgetColor(0, 0, 255)),
                          traceXValue.createProperty(this, null),
                          traceYValue.createProperty(this, null)
                          )));
    }

    /** @return Behavior 'trace' */
    public StructuredWidgetProperty behaviorTrace()
    {
        return trace;
    }
}
