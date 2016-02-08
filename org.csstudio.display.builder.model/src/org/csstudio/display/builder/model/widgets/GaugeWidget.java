/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.ColorWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.diirt.vtype.VType;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that displays a Gauge which reflects the state of a PV
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class GaugeWidget extends Widget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("gauge", WidgetCategory.MONITOR,
            "GAUGE",
            "platform:/plugin/org.csstudio.display.builder.model/icons/gauge.png",
            "Gauge that displays value and alarm rnges of a PV",
            Arrays.asList("org.csstudio.opibuilder.widgets.Gauge"))
    {
        @Override
        public Widget createWidget()
        {
            return new GaugeWidget();
        }
    };

    /** Custom configurator to read legacy *.opi files */
    private static class GaugeConfigurator extends WidgetConfigurator
    {
        public GaugeConfigurator(final Version xml_version)
        {
            super(xml_version);
        }

        @Override
        public void configureFromXML(final Widget widget, final Element xml)
                throws Exception
        {
            super.configureFromXML(widget, xml);
        }
    }


    /** Property for the needle color */
    public static final WidgetPropertyDescriptor<WidgetColor> displayNeedleColor = new WidgetPropertyDescriptor<WidgetColor>(
            WidgetPropertyCategory.DISPLAY, "needle_color", Messages.GaugeWidget_NeedleColor)
    {
        @Override
        public WidgetProperty<WidgetColor> createProperty(final Widget widget,
                final WidgetColor default_color)
        {
            return new ColorWidgetProperty(this, widget, default_color);
        }
    };

    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<WidgetColor> bg_color;
    private volatile WidgetProperty<WidgetColor> needle_color;
    private volatile WidgetProperty<VType> value;

    public GaugeWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    public Version getVersion()
    {   // Legacy used 1.0.0
        return new Version(2, 0, 0);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
        properties.add(displayBorderAlarmSensitive.createProperty(this, true));
        properties.add(bg_color = displayBackgroundColor.createProperty(this, new WidgetColor(60, 100, 60)));
        properties.add(needle_color = displayNeedleColor.createProperty(this, new WidgetColor(60, 255, 60)));
        properties.add(value = runtimeValue.createProperty(this, null));

        // Initial size
        positionWidth().setValue(20);
        positionHeight().setValue(20);
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return pv_name;
    }

    /** @return 'off_color' */
    public WidgetProperty<WidgetColor> bgColor()
    {
        return bg_color;
    }

    /** @return 'off_color' */
    public WidgetProperty<WidgetColor> needleColor()
    {
        return needle_color;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
    }

    @Override
    public WidgetConfigurator getConfigurator(final Version persisted_version)
            throws Exception
    {
        return new GaugeConfigurator(persisted_version);
    }
}
