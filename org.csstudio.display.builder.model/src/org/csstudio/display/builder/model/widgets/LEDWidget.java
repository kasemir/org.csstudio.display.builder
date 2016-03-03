/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorBit;

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
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.ColorWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that displays an LED which reflects the on/off state of a PV
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class LEDWidget extends BaseLEDWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("led", WidgetCategory.MONITOR,
            "LED",
            "platform:/plugin/org.csstudio.display.builder.model/icons/led.png",
            "LED that represents on/off",
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
        public boolean configureFromXML(final ModelReader model_reader, final Widget widget, final Element xml)
                throws Exception
        {
            // Legacy XML with <state_count> identifies MultiStateLEDWidget
            final Element element = XMLUtil.getChildElement(xml, "state_count");
            if (element != null)
                return false;

            super.configureFromXML(model_reader, widget, xml);

            BaseLEDWidget.handle_legacy_position(widget, xml_version, xml);
            return true;
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

    private volatile WidgetProperty<Integer> bit;
    private volatile WidgetProperty<WidgetColor> off_color;
    private volatile WidgetProperty<WidgetColor> on_color;

    public LEDWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(bit = behaviorBit.createProperty(this, -1));
        properties.add(off_color = displayOffColor.createProperty(this, new WidgetColor(60, 100, 60)));
        properties.add(on_color = displayOnColor.createProperty(this, new WidgetColor(60, 255, 60)));
    }

    /** @return 'bit' */
    public WidgetProperty<Integer> bit()
    {
        return bit;
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

    @Override
    public WidgetConfigurator getConfigurator(final Version persisted_version)
            throws Exception
    {
        return new LEDConfigurator(persisted_version);
    }
}
