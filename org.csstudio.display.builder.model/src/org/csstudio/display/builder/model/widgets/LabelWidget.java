/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayText;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.BaseWidget;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;

/** Widget that displays a static text
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class LabelWidget extends BaseWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR
        = new WidgetDescriptor("label", WidgetCategory.GRAPHIC,
                Messages.LabelWidget_Name,
                "platform:/plugin/org.csstudio.display.builder.model/icons/label.png",
                Messages.LabelWidget_Description,
                Arrays.asList("org.csstudio.opibuilder.widgets.Label"))
        {
            @Override
            public Widget createWidget()
            {
                return new LabelWidget();
            }
        };

    private WidgetProperty<String> text;

    public LabelWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(text = displayText.createProperty(this, Messages.LabelWidget_Text));
    }

    /** @return Display 'text' */
    public WidgetProperty<String> displayText()
    {
        return text;
    }
}
