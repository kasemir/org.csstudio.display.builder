/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayOffColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayOnColor;
//import static  org.csstudio.display.builder.model.properties.CommonWidgetProperties.newIntegerPropertyDescriptor;
import static  org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static  org.csstudio.display.builder.model.properties.CommonWidgetProperties.newStringPropertyDescriptor;
//import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFont;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.IntegerWidgetProperty;
import org.csstudio.display.builder.model.properties.StringWidgetProperty;
//import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.properties.WidgetColor;
//import org.csstudio.display.builder.model.properties.WidgetFont;
import org.diirt.vtype.VType;

/** Widget that displays the bits in an Integer or Long Integer value as a set of LEDs
 *  @author Amanda Carpenter
 */
//@SuppressWarnings("nls")
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
        //newIntegerPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "start_bit", Messages.ByteMonitor_StartBit);
        new WidgetPropertyDescriptor<Integer>(WidgetPropertyCategory.DISPLAY, "start_bit", Messages.ByteMonitor_StartBit)
        {
            @Override
            public WidgetProperty<Integer> createProperty(final Widget widget, final Integer value)
            {
                return new IntegerWidgetProperty(this, widget, value, 0, 15);
            }
        };

    /** Display 'num. bits': Bit number in the integer to start displaying. */
    public static final WidgetPropertyDescriptor<Integer> displayNumBits =
        //newIntegerPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "num_bits", Messages.ByteMonitor_NumBits);
        new WidgetPropertyDescriptor<Integer>(WidgetPropertyCategory.DISPLAY, "num_bits", Messages.ByteMonitor_NumBits)
        {
            @Override
            public WidgetProperty<Integer> createProperty(final Widget widget, final Integer value)
            {
                return new IntegerWidgetProperty(this, widget, value, 1, 16);
            }
        };
    
    /** Display 'bit reverse': Reverse the direction that bits are displayed; if no, the start bit (the smallest bit) is on right or bottom. */
    public static final WidgetPropertyDescriptor<Boolean> displayBitReverse =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "bit_rev", Messages.ByteMonitor_BitReverse);

    /** Display 'horizontal': Change whether bits are displayed horizontally or vertically */
    public static final WidgetPropertyDescriptor<Boolean> displayHorizontal =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "horiz", Messages.ByteMonitor_Horizontal);
    
    /** Display 'square LED': Whether LEDS are square (rectangular) or round (circular) */
    public static final WidgetPropertyDescriptor<Boolean> displaySquareLED =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "square_led", Messages.ByteMonitor_SquareLED);
    
    //Labels property, model MultiStateLED
    //Is registered somewhere?
    //TODO: this is really tangled; double-check functionality, efficiency
    private static final ArrayWidgetProperty.Descriptor<StringWidgetProperty> displayLabels =
            new ArrayWidgetProperty.Descriptor<StringWidgetProperty>(
                    WidgetPropertyCategory.DISPLAY,
                    "labels", "Labels", (widget, index) -> 
                    new StringWidgetProperty(
                            newStringPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "labels", "Labels"),
                            widget, "") );
    
    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<VType> value;
    private volatile WidgetProperty<WidgetColor> off_color;
    private volatile WidgetProperty<WidgetColor> on_color;
    private volatile WidgetProperty<Integer> start_bit;
    private volatile WidgetProperty<Integer> num_bits;
    private volatile WidgetProperty<Boolean> bit_rev;
    private volatile WidgetProperty<Boolean> horiz;
    private volatile WidgetProperty<Boolean> square_led;
    private volatile ArrayWidgetProperty<StringWidgetProperty> labels;
    //private volatile WidgetProperty<WidgetFont> font;
    
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
        properties.add(start_bit = displayStartBit.createProperty(this,0));
        properties.add(num_bits = displayNumBits.createProperty(this,8));
        properties.add(bit_rev = displayBitReverse.createProperty(this,false));
        properties.add(horiz = displayHorizontal.createProperty(this,true));
        properties.add(square_led = displaySquareLED.createProperty(this,false));
        properties.add(labels = displayLabels.createProperty(this, new ArrayList<StringWidgetProperty>(8)));
            //TODO: how best to get List<StringWidgetProperty> elements?
        //properties.add(font = displayFont.createProperty(this, NamedWidgetFonts.DEFAULT));
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
    
    /** @return 'start_bit' */
    public WidgetProperty<Integer> displayStartBit()
    {
        return start_bit;
    }

    /** @return 'num_bits' */
    public WidgetProperty<Integer> displayNumBits()
    {
        return num_bits;
    }
    
    /** @return 'bit_rev' */
    public WidgetProperty<Boolean> displayBitReverse()
    {
        return bit_rev;
    }
    
    /** @return 'horiz' */
    public WidgetProperty<Boolean> displayHorizontal()
    {
        return horiz;
    }

    /** @return 'square_led' */
    public WidgetProperty<Boolean> displaySquareLED()
    {
        return square_led;
    }
    
    /** @return 'labels' */
    public ArrayWidgetProperty<StringWidgetProperty> displayLabels()
    {
        return labels;
    }

}
