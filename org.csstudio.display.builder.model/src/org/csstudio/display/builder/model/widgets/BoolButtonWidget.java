/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorBit;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFont;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayOffColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayOnColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newStringPropertyDescriptor;
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
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.diirt.vtype.VType;

/** Widget that provides button for making a binary change
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class BoolButtonWidget extends Widget
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

    /** Display 'on label': Text to display when state is on */
    public static final WidgetPropertyDescriptor<String> displayOnLabel =
        newStringPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "on_label", Messages.BoolWidget_OnLabel);

    /** Display 'off label': Text to display */
    public static final WidgetPropertyDescriptor<String> displayOffLabel =
        newStringPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "off_label", Messages.BoolWidget_OffLabel);

    private volatile WidgetProperty<WidgetFont> font;
    private volatile WidgetProperty<Integer> bit;
    private volatile WidgetProperty<VType> value;
    private volatile WidgetProperty<WidgetColor> off_color;
    private volatile WidgetProperty<WidgetColor> on_color;
    private volatile WidgetProperty<String> off_label;
    private volatile WidgetProperty<String> on_label;


    public BoolButtonWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(behaviorPVName.createProperty(this, ""));
        properties.add(bit = behaviorBit.createProperty(this, 0));
        properties.add(font = displayFont.createProperty(this, NamedWidgetFonts.DEFAULT));
        properties.add(value = runtimeValue.createProperty(this, null));
        properties.add(off_color = displayOffColor.createProperty(this, new WidgetColor(60, 100, 60)));
        properties.add(on_color = displayOnColor.createProperty(this, new WidgetColor(60, 255, 60)));
        properties.add(off_label = displayOffLabel.createProperty(this, Messages.BoolWidget_OffLabel));
        properties.add(on_label = displayOnLabel.createProperty(this, Messages.BoolWidget_OnLabel));
    }


    /** @return Display 'off_label' */
    public WidgetProperty<String> displayOffLabel()
    {
        return off_label;
    }

    /** @return Display 'on_label' */
    public WidgetProperty<String> displayOnLabel()
    {
        return on_label;
    }

    /** @return Display 'font' */
    public WidgetProperty<WidgetFont> displayFont()
    {
        return font;
    }

    /** @return Behavior 'bit' */
    public WidgetProperty<Integer> behaviorBit()
    {
        return bit;
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
