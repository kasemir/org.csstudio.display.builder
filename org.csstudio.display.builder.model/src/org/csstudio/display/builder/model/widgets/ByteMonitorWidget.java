/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayOffColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayOnColor;
import static  org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

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

    /** Display 'start bit': Number of first (smallest) bit */
    public static final WidgetPropertyDescriptor<Integer> displayStartBit =
        new WidgetPropertyDescriptor<Integer>(WidgetPropertyCategory.DISPLAY, "startBit", Messages.ByteMonitor_StartBit)
        {
            @Override
            public WidgetProperty<Integer> createProperty(final Widget widget, final Integer value)
            {
                return new IntegerWidgetProperty(this, widget, value, 0, 31);
            }
        };

    /** Display 'num. bits': Bit number in the integer to start displaying. */
    public static final WidgetPropertyDescriptor<Integer> displayNumBits =
        new WidgetPropertyDescriptor<Integer>(WidgetPropertyCategory.DISPLAY, "numBits", Messages.ByteMonitor_NumBits)
        {
            @Override
            public WidgetProperty<Integer> createProperty(final Widget widget, final Integer value)
            {
                return new IntegerWidgetProperty(this, widget, value, 1, 32);
            }
        };

    /** Display 'bit reverse': Reverse the direction that bits are displayed; if no, the start bit (the smallest bit) is on right or bottom. */
    public static final WidgetPropertyDescriptor<Boolean> displayBitReverse =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "bitReverse", Messages.ByteMonitor_BitReverse);

    /** Display 'horizontal': Change whether bits are displayed horizontally or vertically */
    public static final WidgetPropertyDescriptor<Boolean> displayHorizontal =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "horizontal", Messages.ByteMonitor_Horizontal);

    /** Display 'square LED': Whether LEDS are square (rectangular) or round (circular) */
    public static final WidgetPropertyDescriptor<Boolean> displaySquareLED =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "square_led", Messages.ByteMonitor_SquareLED);

    private volatile WidgetProperty<String> pv_name;
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
        properties.add(behaviorPVName.createProperty(this, ""));
        properties.add(value = runtimeValue.createProperty(this, null));
        properties.add(displayBorderAlarmSensitive.createProperty(this, true));
        properties.add(off_color = displayOffColor.createProperty(this, new WidgetColor(60, 100, 60)));
        properties.add(on_color = displayOnColor.createProperty(this, new WidgetColor(60, 255, 60)));
        properties.add(startBit = displayStartBit.createProperty(this,0));
        properties.add(numBits = displayNumBits.createProperty(this,8));
        properties.add(bitReverse = displayBitReverse.createProperty(this,false));
        properties.add(horizontal = displayHorizontal.createProperty(this,true));
        properties.add(square_led = displaySquareLED.createProperty(this,false));
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return pv_name;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
    }

    /** @return 'off_color' */
    public WidgetProperty<WidgetColor> displayOffColor()
    {
        return off_color;
    }

    /** @return 'on_color' */
    public WidgetProperty<WidgetColor> displayOnColor()
    {
        return on_color;
    }

    /** @return 'startBit' */
    public WidgetProperty<Integer> displayStartBit()
    {
        return startBit;
    }

    /** @return 'numBits' */
    public WidgetProperty<Integer> displayNumBits()
    {
        return numBits;
    }

    /** @return 'bitReverse' */
    public WidgetProperty<Boolean> displayBitReverse()
    {
        return bitReverse;
    }

    /** @return 'horizontal' */
    public WidgetProperty<Boolean> displayHorizontal()
    {
        return horizontal;
    }

    /** @return 'square_led' */
    public WidgetProperty<Boolean> displaySquareLED()
    {
        return square_led;
    }

}
