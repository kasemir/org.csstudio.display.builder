/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFont;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayForegroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayText;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayTransparent;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.BaseWidget;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;

/** Widget that displays a static text
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class LabelWidget extends BaseWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("label", WidgetCategory.GRAPHIC,
            "Label",
            "platform:/plugin/org.csstudio.display.builder.model/icons/label.png",
            "Label displays one or more lines of text",
            Arrays.asList("org.csstudio.opibuilder.widgets.Label"))
    {
        @Override
        public Widget createWidget()
        {
            return new LabelWidget();
        }
    };

    private WidgetProperty<String> text;
    private WidgetProperty<WidgetColor> foreground;
    private WidgetProperty<WidgetColor> background;
    private WidgetProperty<Boolean> transparent;
    private WidgetProperty<WidgetFont> font;

    public LabelWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(text = displayText.createProperty(this, Messages.LabelWidget_Text));
        properties.add(foreground = displayForegroundColor.createProperty(this, NamedWidgetColors.TEXT));
        properties.add(background = displayBackgroundColor.createProperty(this, NamedWidgetColors.BACKGROUND));
        properties.add(transparent = displayTransparent.createProperty(this, true));
        properties.add(font = displayFont.createProperty(this, NamedWidgetFonts.DEFAULT));
    }

    /** @return Display 'text' */
    public WidgetProperty<String> displayText()
    {
        return text;
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

    /** @return Display 'transparent' */
    public WidgetProperty<Boolean> displayTransparent()
    {
        return transparent;
    }

    /** @return Display 'font' */
    public WidgetProperty<WidgetFont> displayFont()
    {
        return font;
    }
}
