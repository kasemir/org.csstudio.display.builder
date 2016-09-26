/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newStringPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBit;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propFont;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propOffColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propOnColor;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;

/** Widget that provides button for making a binary change
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class BoolButtonWidget extends PVWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("bool_button", WidgetCategory.CONTROL,
            "Boolean Button",
            "platform:/plugin/org.csstudio.display.builder.model/icons/bool_button.gif",
            "Button that can toggle one bit of a PV value between 1 and 0",
            Arrays.asList("org.csstudio.opibuilder.widgets.BoolButton"))
    {
        @Override
        public Widget createWidget()
        {
            return new BoolButtonWidget();
        }
    };

    /** 'on label' property: Text to display when state is on */
    public static final WidgetPropertyDescriptor<String> propOnLabel =
        newStringPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "on_label", Messages.BoolWidget_OnLabel);

    /** 'off label' property: Text to display */
    public static final WidgetPropertyDescriptor<String> propOffLabel =
        newStringPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "off_label", Messages.BoolWidget_OffLabel);

    private volatile WidgetProperty<Integer> bit;
    private volatile WidgetProperty<WidgetColor> off_color;
    private volatile WidgetProperty<WidgetColor> on_color;
    private volatile WidgetProperty<String> off_label;
    private volatile WidgetProperty<String> on_label;
    private volatile WidgetProperty<WidgetFont> font;


    public BoolButtonWidget()
    {
        super(WIDGET_DESCRIPTOR.getType(), 100, 30);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(bit = propBit.createProperty(this, 0));
        properties.add(off_label = propOffLabel.createProperty(this, Messages.BoolWidget_OffLabel));
        properties.add(off_color = propOffColor.createProperty(this, new WidgetColor(60, 100, 60)));
        properties.add(on_label = propOnLabel.createProperty(this, Messages.BoolWidget_OnLabel));
        properties.add(on_color = propOnColor.createProperty(this, new WidgetColor(60, 255, 60)));
        properties.add(font = propFont.createProperty(this, NamedWidgetFonts.DEFAULT));
    }


    /** @return 'off_label' property */
    public WidgetProperty<String> propOffLabel()
    {
        return off_label;
    }

    /** @return 'on_label' property */
    public WidgetProperty<String> propOnLabel()
    {
        return on_label;
    }

    /** @return 'bit' property */
    public WidgetProperty<Integer> propBit()
    {
        return bit;
    }

    /** @return 'off_color' property*/
    public WidgetProperty<WidgetColor> propOffColor()
    {
        return off_color;
    }

    /** @return 'on_color' property */
    public WidgetProperty<WidgetColor> propOnColor()
    {
        return on_color;
    }

    /** @return 'font' property */
    public WidgetProperty<WidgetFont> propFont()
    {
        return font;
    }
}
