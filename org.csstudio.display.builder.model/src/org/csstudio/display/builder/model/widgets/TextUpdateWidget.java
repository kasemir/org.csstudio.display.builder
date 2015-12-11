/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFont;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayForegroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.BaseWidget;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.diirt.vtype.VType;

/** Widget that displays a changing text
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TextUpdateWidget extends BaseWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("textupdate", WidgetCategory.MONITOR,
            "TextUpdate",
            "platform:/plugin/org.csstudio.display.builder.model/icons/textupdate.png",
            "Displays current value of PV as text",
            Arrays.asList("org.csstudio.opibuilder.widgets.TextUpdate"))
    {
        @Override
        public Widget createWidget()
        {
            return new TextUpdateWidget();
        }
    };

    private WidgetProperty<String> pv_name;
    private WidgetProperty<WidgetColor> foreground;
    private WidgetProperty<WidgetColor> background;
    private WidgetProperty<WidgetFont> font;
    private WidgetProperty<VType> value;

    public TextUpdateWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
        properties.add(foreground = displayForegroundColor.createProperty(this, NamedWidgetColors.TEXT));
        properties.add(background = displayBackgroundColor.createProperty(this, NamedWidgetColors.READ_BACKGROUND));
        properties.add(font = displayFont.createProperty(this, NamedWidgetFonts.DEFAULT));
        properties.add(value = runtimeValue.createProperty(this, null));
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return pv_name;
    }

    /** @return Display 'foreground_color' */
    public WidgetProperty<WidgetColor> displayForegroundColor()
    {
        return foreground;
    }

    /** @return Display 'background_color' */
    public WidgetProperty<WidgetColor> displayBackgroundColor()
    {
        return background;
    }

    /** @return Display 'font' */
    public WidgetProperty<WidgetFont> displayFont()
    {
        return font;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
    }
}
