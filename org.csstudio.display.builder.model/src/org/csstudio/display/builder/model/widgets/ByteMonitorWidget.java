/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static  org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propFlat;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propHorizontal;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propOffColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propOnColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propSquare;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import org.csstudio.display.builder.model.properties.IntegerWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that displays the bits in an Integer or Long Integer value as a set of LEDs
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class ByteMonitorWidget extends PVWidget
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

    private static class CustomConfigurator extends WidgetConfigurator
    {
        public CustomConfigurator(final Version xml_version)
        {
            super(xml_version);
        }

        @Override
        public boolean configureFromXML(final ModelReader model_reader, final Widget widget,
                                        final Element xml) throws Exception
        {
            if (! super.configureFromXML(model_reader, widget, xml))
                return false;

            final Optional<Boolean> square = XMLUtil.getChildBoolean(xml, "square_led");
            if (square.isPresent())
                ((ByteMonitorWidget) widget).propSquare().setValue(square.get());
            return true;
        }
    };


    private volatile WidgetProperty<WidgetColor> off_color;
    private volatile WidgetProperty<WidgetColor> on_color;
    private volatile WidgetProperty<Integer> startBit;
    private volatile WidgetProperty<Integer> numBits;
    private volatile WidgetProperty<Boolean> bitReverse;
    private volatile WidgetProperty<Boolean> horizontal;
    private volatile WidgetProperty<Boolean> square;
    private volatile WidgetProperty<Boolean> flat;

    public ByteMonitorWidget()
    {
        super(WIDGET_DESCRIPTOR.getType(), 160, 20);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(startBit = propStartBit.createProperty(this,0));
        properties.add(numBits = propNumBits.createProperty(this,8));
        properties.add(bitReverse = propBitReverse.createProperty(this,false));
        properties.add(horizontal = propHorizontal.createProperty(this,true));
        properties.add(square = propSquare.createProperty(this,false));
        properties.add(flat = propFlat.createProperty(this, false));
        properties.add(off_color = propOffColor.createProperty(this, new WidgetColor(60, 100, 60)));
        properties.add(on_color = propOnColor.createProperty(this, new WidgetColor(60, 255, 60)));
    }

    @Override
    public WidgetConfigurator getConfigurator(Version persisted_version)
            throws Exception
    {
        return new CustomConfigurator(persisted_version);
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

    /** @return 'square' property */
    public WidgetProperty<Boolean> propSquare()
    {
        return square;
    }

    /** @return 'flat' property*/
    public WidgetProperty<Boolean> propFlat()
    {
        return flat;
    }
}
