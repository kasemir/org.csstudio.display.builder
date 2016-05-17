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
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayOffColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayOnColor;
//**TODO: create Integer properties numBits, startBit
//import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorBit;
//import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
//import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFont;
//??import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newStringPropertyDescriptor;

import java.util.Arrays;
import java.util.List;

//import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
//import org.csstudio.display.builder.model.WidgetPropertyCategory;
//import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
//import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.properties.WidgetColor;
//import org.csstudio.display.builder.model.properties.WidgetFont;
import org.diirt.vtype.VType;
//import org.csstudio.display.builder.model.persist.ModelReader;
//import org.csstudio.display.builder.model.persist.XMLUtil;
//import org.osgi.framework.Version;
//import org.w3c.dom.Element;

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
    
    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<VType> value;
    private volatile WidgetProperty<WidgetColor> off_color;
    private volatile WidgetProperty<WidgetColor> on_color;
    //**private volatile WidgetProperty<Integer> num_bits;
    /*private volatile WidgetProperty<WidgetFont> font;
    //improbable: private volatile WidgetProperty<Integer> bit;
    private volatile WidgetProperty<String> off_label;
    private volatile WidgetProperty<String> on_label;
    //??private volatile WidgetProperty<???> displayBorderAlarmSensitive;*/
    
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
        properties.add(off_color = displayOffColor.createProperty(this, new WidgetColor(60, 100, 60)));
        properties.add(on_color = displayOnColor.createProperty(this, new WidgetColor(60, 255, 60)));
        //**properties.add(num_bits = numBits.createProperty(this,8));
        //properties.add(bit = behaviorBit.createProperty(this, 0));
        //properties.add(font = displayFont.createProperty(this, NamedWidgetFonts.DEFAULT));
        //properties.add(off_label = displayOffLabel.createProperty(this, Messages.BoolWidget_OffLabel));
        //properties.add(on_label = displayOnLabel.createProperty(this, Messages.BoolWidget_OnLabel));
        //properties.add(displayBorderAlarmSensitive.createProperty(this, true));

        // Initial size
        //positionWidth().setValue(20);
        //positionHeight().setValue(20);
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
}
