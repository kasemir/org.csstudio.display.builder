/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.BaseWidget;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.ColorWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.diirt.vtype.VType;

/** Widget that displays an LED which reflects the enumerated state of a PV
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class LEDWidget extends BaseWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("led", WidgetCategory.MONITOR,
            Messages.LEDWidget_Name,
            "platform:/plugin/org.csstudio.display.builder.model/icons/led.png",
            Messages.LEDWidget_Description,
            Arrays.asList("org.csstudio.opibuilder.widgets.LED"))
    {
        @Override
        public Widget createWidget()
        {
            return new LEDWidget();
        }
    };

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

    // TODO Handle legacy LED sizing
    // Border was included in the size,
    // so with the same nominal size an "alarm sensitive" LED
    // was smaller than a non-a.s. LED

    public LEDWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
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
}
