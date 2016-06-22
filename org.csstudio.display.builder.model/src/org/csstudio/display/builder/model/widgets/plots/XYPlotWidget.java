/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets.plots;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.StringWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.csstudio.display.builder.model.widgets.VisibleWidget;
import org.csstudio.display.builder.model.widgets.plots.PlotWidgetProperties.AxisWidgetProperty;
import org.csstudio.display.builder.model.widgets.plots.PlotWidgetProperties.TraceWidgetProperty;
import org.csstudio.display.builder.model.widgets.plots.PlotWidgetProperties.YAxisWidgetProperty;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that displays X/Y waveforms
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class XYPlotWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("xyplot", WidgetCategory.PLOT,
            Messages.XYPlot_Name,
            "platform:/plugin/org.csstudio.display.builder.model/icons/xyplot.png",
            Messages.XYPlot_Description,
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
            configureAllPropertiesFromMatchingXML(model_reader, widget, xml);

            if (xml_version.getMajor() < 2)
            {
                // Legacy widget had a "pv_name" property that was basically used as a macro within the widget
                final String pv_macro = XMLUtil.getChildString(xml, "pv_name").orElse("");
                final XYPlotWidget plot = (XYPlotWidget) widget;

                // "axis_0_*" was the X axis config
                readLegacyAxis(model_reader, 0, xml, plot.x_axis, pv_macro);

                handleLegacyYAxes(model_reader, widget, xml, pv_macro);

                return handleLegacyTraces(model_reader, widget, xml, pv_macro);
            }
            return true;
        }

        private void readLegacyAxis(final ModelReader model_reader,
                                    final int legacy_axis, final Element xml, final AxisWidgetProperty axis, final String pv_macro) throws Exception
        {
            XMLUtil.getChildString(xml, "axis_" + legacy_axis + "_axis_title").ifPresent(title ->
            {
                final WidgetProperty<String> property = axis.title();
                ((StringWidgetProperty)property).setSpecification(title.replace("$(pv_name)", pv_macro));
            });
            XMLUtil.getChildString(xml, "axis_" + legacy_axis + "_minimum").ifPresent(txt ->
                axis.minimum().setValue(Double.parseDouble(txt)) );
            XMLUtil.getChildString(xml, "axis_" + legacy_axis + "_maximum").ifPresent(txt ->
                axis.maximum().setValue(Double.parseDouble(txt)) );
            XMLUtil.getChildString(xml, "axis_" + legacy_axis + "_auto_scale").ifPresent(txt ->
                axis.autoscale().setValue(Boolean.parseBoolean(txt)) );

            Element font_el = XMLUtil.getChildElement(xml, "axis_" + legacy_axis + "_title_font");
            if (font_el != null)
                axis.titleFont().readFromXML(model_reader, font_el);

            font_el = XMLUtil.getChildElement(xml, "axis_" + legacy_axis + "_scale_font");
            if (font_el != null)
                axis.scaleFont().readFromXML(model_reader, font_el);
        }

        private void handleLegacyYAxes(final ModelReader model_reader,
                                       final Widget widget, final Element xml, final String pv_macro)  throws Exception
        {
            final XYPlotWidget plot = (XYPlotWidget) widget;

            final int axis_count = XMLUtil.getChildInteger(xml, "axis_count").orElse(0);

            // "axis_1_*" was the Y axis, and higher axes could be either X or Y
            int y_count = 0; // Number of y axes found in legacy config
            for (int legacy_axis=1; legacy_axis<axis_count; ++legacy_axis)
            {
                // Check for "axis_*_y_axis" (default: true).
                // If _not_, this is an additional X axis which we ignore
                if (! Boolean.parseBoolean(XMLUtil.getChildString(xml, "axis_" + legacy_axis + "_y_axis").orElse("true")))
                        continue;

                // Count actual Y axes, because legacy_axis includes skipped X axes
                ++y_count;

                final YAxisWidgetProperty y_axis;
                if (plot.y_axes.size() < y_count)
                {
                    y_axis = YAxisWidgetProperty.create(widget, "");
                    plot.y_axes.addElement(y_axis);
                }
                else
                    y_axis = plot.y_axes.getElement(y_count-1);

                readLegacyAxis(model_reader, legacy_axis, xml, y_axis, pv_macro);
            }
        }

        private PlotWidgetPointType mapPointType(final int legacy_style)
        {
            switch (legacy_style)
            {
            case 0: // None
                return PlotWidgetPointType.NONE;
            case 1: // POINT
            case 2: // CIRCLE
                return PlotWidgetPointType.CIRCLES;
            case 3: // TRIANGLE
            case 4: // FILLED_TRIANGLE
                return PlotWidgetPointType.TRIANGLES;
            case 5: // SQUARE
            case 6: // FILLED_SQUARE
                return PlotWidgetPointType.SQUARES;
            case 7: // DIAMOND
            case 8: // FILLED_DIAMOND
                return PlotWidgetPointType.DIAMONDS;
            case 9: // XCROSS
            case 10: // CROSS
            case 11: // BAR
            default:
                return PlotWidgetPointType.XMARKS;
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
                XMLUtil.getChildString(xml, "trace_" + legacy_trace + "_x_pv")
                       .ifPresent(pv ->
                        {
                            ((StringWidgetProperty)trace.traceXPV()).setSpecification(pv.replace("$(pv_name)", pv_macro));
                        });

                // Color
                Element element = XMLUtil.getChildElement(xml, "trace_" + legacy_trace + "_trace_color");
                if (element != null)
                    trace.traceColor().readFromXML(model_reader, element);

                XMLUtil.getChildInteger(xml, "trace_" + legacy_trace + "_point_size")
                       .ifPresent(size -> trace.tracePointSize().setValue(size));

                XMLUtil.getChildInteger(xml, "trace_" + legacy_trace + "_point_style")
                       .ifPresent(style -> trace.tracePointType().setValue(mapPointType(style)));

                // Name. Empty name will result in using the Y PV name
                String name = XMLUtil.getChildString(xml, "trace_" + legacy_trace + "_name").orElse("");
                name = name.replace("$(trace_" + legacy_trace + "_y_pv)", "");

                if (! name.isEmpty())
                    ((StringWidgetProperty)trace.traceName()).setSpecification(name.replace("$(pv_name)", pv_macro));

                // Legacy used index 0=X, 1=Y, 2=Y1, ..
                // except higher axis index could also stand for X1, X2, which we don't handle
                XMLUtil.getChildInteger(xml, "trace_" + legacy_trace + "_y_axis_index")
                       .ifPresent(index -> trace.traceYAxis().setValue(Math.max(0, index - 1)));
            }
            return true;
        }
    };

    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<String> title;
    private volatile WidgetProperty<WidgetFont> title_font;
    private volatile WidgetProperty<Boolean> show_legend;
    private volatile AxisWidgetProperty x_axis;
    private volatile ArrayWidgetProperty<YAxisWidgetProperty> y_axes;
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
        properties.add(background = displayBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.BACKGROUND)));
        properties.add(title = PlotWidgetProperties.displayTitle.createProperty(this, ""));
        properties.add(title_font = PlotWidgetProperties.titleFontProperty.createProperty(this, NamedWidgetFonts.HEADER2));
        properties.add(show_legend = PlotWidgetProperties.displayLegend.createProperty(this, true));
        properties.add(x_axis = AxisWidgetProperty.create(this, Messages.PlotWidget_X));
        properties.add(y_axes = PlotWidgetProperties.behaviorYAxes.createProperty(this, Arrays.asList(YAxisWidgetProperty.create(this, Messages.PlotWidget_Y))));
        properties.add(traces = PlotWidgetProperties.behaviorTraces.createProperty(this, Arrays.asList(new TraceWidgetProperty(this))));
    }

    /** @return Display 'background' */
    public WidgetProperty<WidgetColor> displayBackground()
    {
        return background;
    }

    /** @return Display 'title' */
    public WidgetProperty<String> displayTitle()
    {
        return title;
    }

    /** @return Display 'title_font' */
    public WidgetProperty<WidgetFont> displayTitleFont()
    {
        return title_font;
    }

    /** @return Display 'show_legend' */
    public WidgetProperty<Boolean> displayLegend()
    {
        return show_legend;
    }

    /** @return Behavior 'x_axis' */
    public AxisWidgetProperty behaviorXAxis()
    {
        return x_axis;
    }

    /** @return Behavior 'y_axes' */
    public ArrayWidgetProperty<YAxisWidgetProperty> behaviorYAxes()
    {
        return y_axes;
    }

    /** @return Behavior 'traces' */
    public ArrayWidgetProperty<TraceWidgetProperty> behaviorTraces()
    {
        return traces;
    }
}
