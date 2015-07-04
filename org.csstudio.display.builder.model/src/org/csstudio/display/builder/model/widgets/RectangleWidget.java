/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.BaseWidget;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;

/** Widget that displays a static rectangle
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RectangleWidget extends BaseWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR
        = new WidgetDescriptor("rectangle", WidgetCategory.GRAPHIC,
                Messages.RectangleWidget_Name,
                "platform:/plugin/org.csstudio.display.builder.model/icons/rectangle.png",
                Messages.RectangleWidget_Description,
                Arrays.asList("org.csstudio.opibuilder.widgets.Rectangle"))
        {
            @Override
            public Widget createWidget()
            {
                return new RectangleWidget();
            }
        };

    public RectangleWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        // TODO Angle
    }
}
