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
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayText;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.properties.WidgetFont;

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

    private volatile WidgetProperty<String> text;
    private volatile WidgetProperty<WidgetFont> font;
    private volatile WidgetProperty<Integer> bit;

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
        properties.add(text = displayText.createProperty(this, "toggle"));
        properties.add(font = displayFont.createProperty(this, NamedWidgetFonts.DEFAULT));
    }


    /** @return Display 'text' */
    public WidgetProperty<String> displayText()
    {
        return text;
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

}
