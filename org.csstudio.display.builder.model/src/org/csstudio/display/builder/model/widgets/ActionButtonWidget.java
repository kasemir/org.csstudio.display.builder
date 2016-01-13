/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayText;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetMacros;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.macros.Macros;

/** Widget that provides button for invoking actions
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ActionButtonWidget extends Widget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("action_button", WidgetCategory.CONTROL,
            "Action Button",
            "platform:/plugin/org.csstudio.display.builder.model/icons/action_button.png",
            "Button that can open related displays or write PVs",
            Arrays.asList("org.csstudio.opibuilder.widgets.ActionButton"))
    {
        @Override
        public Widget createWidget()
        {
            return new ActionButtonWidget();
        }
    };

    private WidgetProperty<Macros> macros;
    private WidgetProperty<String> text;

    public ActionButtonWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(behaviorPVName.createProperty(this, ""));
        properties.add(macros = widgetMacros.createProperty(this, new Macros()));
        properties.add(text = displayText.createProperty(this, "$(actions)"));
    }

    /** @return Widget 'macros' */
    public WidgetProperty<Macros> widgetMacros()
    {
        return macros;
    }

    /** @return Display 'text' */
    public WidgetProperty<String> displayText()
    {
        return text;
    }

    /** Action button widget extends parent macros
     *  @return {@link Macros}
     */
    @Override
    public Macros getEffectiveMacros()
    {
        final Macros base = super.getEffectiveMacros();
        final Macros my_macros = widgetMacros().getValue();
        return Macros.merge(base, my_macros);
    }
}
