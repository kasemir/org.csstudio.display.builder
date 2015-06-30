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

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;

/** Widget that displays a progress bar
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ProgressBarWidget extends Widget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR
        = new WidgetDescriptor("progressbar", WidgetCategory.MONITOR,
                Messages.ProgressBar_Name, Messages.ProgressBar_Description,
                Arrays.asList("org.csstudio.opibuilder.widgets.progressbar"))
        {
            @Override
            public Widget createWidget(final String name)
            {
                return new ProgressBarWidget(name);
            }
        };

    /** @param name Widget name */
    public ProgressBarWidget(final String name)
    {
        super(WIDGET_DESCRIPTOR.getType(), name);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(behaviorPVName.createProperty(this, ""));
        properties.add(runtimeValue.createProperty(this, null));
    }
}
