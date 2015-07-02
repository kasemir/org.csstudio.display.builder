/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.BaseWidget;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.epics.vtype.VType;

/** Widget that displays a changing text
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TextUpdateWidget extends BaseWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR
        = new WidgetDescriptor("textupdate", WidgetCategory.MONITOR,
                Messages.TextUpdateWidget_Name, Messages.TextUpdateWidget_Description,
                Arrays.asList("org.csstudio.opibuilder.widgets.TextUpdate"))
        {
            @Override
            public Widget createWidget()
            {
                return new TextUpdateWidget();
            }
        };

    public TextUpdateWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(behaviorPVName.createProperty(this, ""));
        properties.add(runtimeValue.createProperty(this, null));
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return getProperty(behaviorPVName);
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return getProperty(runtimeValue);
    }
}
