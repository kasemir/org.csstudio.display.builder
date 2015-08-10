/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetFactory;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;

/** Helper for naming widgets
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetNaming
{
    private static final Pattern pattern = Pattern.compile("(.*)_(\\d+)");

    // TODO: Keep WidgetNaming instance in editor.
    // Reset on each new model
    // Keep map of index for each widget name

    public static void setDefaultName(final DisplayModel model, final Widget widget)
    {
        String name = widget.getName();

        // Default to human-readable widget type
        if (name.isEmpty())
            name = WidgetFactory.getInstance().getWidgetDescriptor(widget.getType()).get().getName();

        // Does the name match "SomeName_14"?
        final Matcher matcher = pattern.matcher(name);
        final String base;
        int number;
        if (matcher.matches())
        {
            base = matcher.group(1);
            number = Integer.parseInt(matcher.group(2));
        }
        else
        {
            base = name;
            number = 0;
        }

        // Locate next available "SomeName_14"
        while (model.getChildByName(name) != null)
        {
            ++number;
            name = base + "_" + number;
            System.out.println("Checking " + name);
        }
        widget.setPropertyValue(CommonWidgetProperties.widgetName, name);
    }
}
