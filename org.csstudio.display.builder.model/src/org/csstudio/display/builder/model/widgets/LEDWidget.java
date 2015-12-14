/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionHeight;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionY;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.BaseWidget;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.ColorWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.diirt.vtype.VType;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that displays an LED which reflects the enumerated state of a PV
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class LEDWidget extends BaseWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("led", WidgetCategory.MONITOR,
            "LED",
            "platform:/plugin/org.csstudio.display.builder.model/icons/led.png",
            "LED that represents on/off or multi-state enum",
            Arrays.asList("org.csstudio.opibuilder.widgets.LED"))
    {
        @Override
        public Widget createWidget()
        {
            return new LEDWidget();
        }
    };

    /** Custom configurator to read legacy *.opi files */
    private static class LEDConfigurator extends WidgetConfigurator
    {
        public LEDConfigurator(final Version xml_version)
        {
            super(xml_version);
        }

        @Override
        public void configureFromXML(final Widget widget, final Element xml)
                throws Exception
        {
            super.configureFromXML(widget, xml);

            if (xml_version.getMajor() < 2)
            {   // Handle legacy LED sizing.
                // Border was included in the size,
                // so with the same nominal size an "alarm sensitive" LED
                // was smaller than a non-a.s. LED
                if (widget.getProperty(displayBorderAlarmSensitive).getValue())
                {
                    final int border = Integer.parseInt(XMLUtil.getChildString(xml, "border_width").orElse("1"));
                    // In principle, border goes around the widget,
                    // so X, Y get adjusted by 1*border
                    // and Width, Height by 2*border.
                    // But when comparing older files, border was 2* added to X, Y
                    // as well as size?!
                    WidgetProperty<Integer> prop = widget.getProperty(positionX);
                    prop.setValue(prop.getValue() + 2*border);
                    prop = widget.getProperty(positionY);
                    prop.setValue(prop.getValue() + 2*border);
                    prop = widget.getProperty(positionWidth);
                    prop.setValue(prop.getValue() - 2*border);
                    prop = widget.getProperty(positionHeight);
                    prop.setValue(prop.getValue() - 2*border);
                }
            }
        }
    }

    /** Property for the 'off' color */
    public static final WidgetPropertyDescriptor<WidgetColor> displayOffColor = new WidgetPropertyDescriptor<WidgetColor>(
            WidgetPropertyCategory.DISPLAY, "off_color", Messages.LEDWidget_OffColor)
    {
        @Override
        public WidgetProperty<WidgetColor> createProperty(final Widget widget,
                final WidgetColor default_color)
        {
            return new ColorWidgetProperty(this, widget, default_color);
        }
    };

    /** Property for the 'on' color */
    public static final WidgetPropertyDescriptor<WidgetColor> displayOnColor = new WidgetPropertyDescriptor<WidgetColor>(
            WidgetPropertyCategory.DISPLAY, "on_color", Messages.LEDWidget_OnColor)
    {
        @Override
        public WidgetProperty<WidgetColor> createProperty(final Widget widget,
                final WidgetColor default_color)
        {
            return new ColorWidgetProperty(this, widget, default_color);
        }
    };

    private WidgetProperty<String> pv_name;
    private WidgetProperty<WidgetColor> off_color;
    private WidgetProperty<WidgetColor> on_color;
    private WidgetProperty<VType> value;

    public LEDWidget()
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
        properties.add(off_color = displayOffColor.createProperty(this, new WidgetColor(60, 100, 60)));
        properties.add(on_color = displayOnColor.createProperty(this, new WidgetColor(60, 255, 60)));
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
    public WidgetProperty<WidgetColor> offColor()
    {
        return off_color;
    }

    /** @return 'off_color' */
    public WidgetProperty<WidgetColor> onColor()
    {
        return on_color;
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
        return new LEDConfigurator(persisted_version);
    }
}
