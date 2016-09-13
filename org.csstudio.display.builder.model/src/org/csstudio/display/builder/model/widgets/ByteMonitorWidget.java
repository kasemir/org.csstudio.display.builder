/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static  org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propOffColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propOnColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimePropValue;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.IntegerWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.diirt.vtype.VType;

/** Widget that displays the bits in an Integer or Long Integer value as a set of LEDs
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class ByteMonitorWidget extends VisibleWidget
{
    /** Widget descriptor */
	public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("byte_monitor", WidgetCategory.MONITOR,
            "Byte Monitor",
            "platform:/plugin/org.csstudio.display.builder.model/icons/byte_monitor.png",
            "Displays the bits in an Integer or Long Integer value as a set of LEDs",
            Arrays.asList("org.csstudio.opibuilder.widgets.bytemonitor"))
    {
        @Override
        public Widget createWidget()
        {
            return new ByteMonitorWidget();
        }
    };

    /** 'start bit' property: Number of first (smallest) bit */
    public static final WidgetPropertyDescriptor<Integer> propStartBit =
        new WidgetPropertyDescriptor<Integer>(WidgetPropertyCategory.DISPLAY, "startBit", Messages.ByteMonitor_StartBit)
        {
            @Override
            public WidgetProperty<Integer> createProperty(final Widget widget, final Integer value)
            {
                return new IntegerWidgetProperty(this, widget, value, 0, 31);
            }
        };

    /** 'num. bits' property: Bit number in the integer to start displaying. */
    public static final WidgetPropertyDescriptor<Integer> propNumBits =
        new WidgetPropertyDescriptor<Integer>(WidgetPropertyCategory.DISPLAY, "numBits", Messages.ByteMonitor_NumBits)
        {
            @Override
            public WidgetProperty<Integer> createProperty(final Widget widget, final Integer value)
            {
                return new IntegerWidgetProperty(this, widget, value, 1, 32);
            }
        };

    /** 'bit reverse' property: Reverse the direction that bits are displayed; if no, the start bit (the smallest bit) is on right or bottom. */
    public static final WidgetPropertyDescriptor<Boolean> propBitReverse =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "bitReverse", Messages.ByteMonitor_BitReverse);

    /** 'horizontal' property: Change whether bits are displayed horizontally or vertically */
    public static final WidgetPropertyDescriptor<Boolean> propHorizontal =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "horizontal", Messages.ByteMonitor_Horizontal);

    /** 'square LED' property: Whether LEDS are square (rectangular) or round (circular) */
    public static final WidgetPropertyDescriptor<Boolean> propSquareLED =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "square_led", Messages.ByteMonitor_SquareLED);

    private volatile WidgetProperty<VType> value;
    private volatile WidgetProperty<WidgetColor> off_color;
    private volatile WidgetProperty<WidgetColor> on_color;
    private volatile WidgetProperty<Integer> startBit;
    private volatile WidgetProperty<Integer> numBits;
    private volatile WidgetProperty<Boolean> bitReverse;
    private volatile WidgetProperty<Boolean> horizontal;
    private volatile WidgetProperty<Boolean> square_led;

    public ByteMonitorWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(propPVName.createProperty(this, ""));
        properties.add(value = runtimePropValue.createProperty(this, null));
        properties.add(propBorderAlarmSensitive.createProperty(this, true));
        properties.add(startBit = propStartBit.createProperty(this,0));
        properties.add(numBits = propNumBits.createProperty(this,8));
        properties.add(bitReverse = propBitReverse.createProperty(this,false));
        properties.add(horizontal = propHorizontal.createProperty(this,true));
        properties.add(square_led = propSquareLED.createProperty(this,false));
        properties.add(off_color = propOffColor.createProperty(this, new WidgetColor(60, 100, 60)));
        properties.add(on_color = propOnColor.createProperty(this, new WidgetColor(60, 255, 60)));
    }

    /** @return Runtime 'value' property */
    public WidgetProperty<VType> runtimePropValue()
    {
        return value;
    }

    /** @return 'off_color' property */
    public WidgetProperty<WidgetColor> propOffColor()
    {
        return off_color;
    }

    /** @return 'on_color' property */
    public WidgetProperty<WidgetColor> propOnColor()
    {
        return on_color;
    }

    /** @return 'startBit' property */
    public WidgetProperty<Integer> propStartBit()
    {
        return startBit;
    }

    /** @return 'numBits' property */
    public WidgetProperty<Integer> propNumBits()
    {
        return numBits;
    }

    /** @return 'bitReverse' property */
    public WidgetProperty<Boolean> propBitReverse()
    {
        return bitReverse;
    }

    /** @return 'horizontal' property */
    public WidgetProperty<Boolean> propHorizontal()
    {
        return horizontal;
    }

    /** @return 'square_led' property */
    public WidgetProperty<Boolean> propSquareLED()
    {
        return square_led;
    }

}
