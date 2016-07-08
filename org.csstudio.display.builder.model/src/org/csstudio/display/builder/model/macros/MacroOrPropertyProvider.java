/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.macros;

import static org.csstudio.display.builder.model.ModelPlugin.logger;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetType;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import org.csstudio.display.builder.model.WidgetProperty;

/** Provides values from macros, falling back to widget properties
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class MacroOrPropertyProvider implements MacroValueProvider
{
    private final MacroValueProvider macros;
    private final Map<String, WidgetProperty<?>> properties;

    /** Initialize
     *  @param macros      Macros to use
     *  @param properties  Properties on which to fall back
     */
    public MacroOrPropertyProvider(final MacroValueProvider macros,
                                   final Map<String, WidgetProperty<?>> properties)
    {
        this.macros = macros;
        this.properties = properties;
    }

    @Override
    public String getValue(final String name)
    {
        // Automatic macro for Display ID,
        // uniquely identifies the display
        if ("DID".equals(name))
        {
            // Every widget must have a 'type' property,
            // so fetch that to get the widget and then the display
            int id;
            try
            {
                id = System.identityHashCode(properties.get(widgetType.getName()).getWidget().getDisplayModel());
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot obtain display ID for $(DID)", ex);
                return "DP00";
            }
            return "DP" + Integer.toHexString(id);
        }

        // Automatic macro for Display NAME
        if ("DNAME".equals(name))
        {
            try
            {
                return properties.get(widgetType.getName()).getWidget().getDisplayModel().widgetName().getValue();
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot obtain display name", ex);
                return "Unknown";
            }
        }

        String value = macros.getValue(name);
        if (value != null)
            return value;

        final WidgetProperty<?> property = properties.get(name);
        if (property != null)
        {   // If value is a single-element collection, get string for that one element.
            // This is primarily for buttons that use $(actions) as their text,
            // and there's a single action which should show as "That Action"
            // and not "[That Action]".
            final Object prop_val = property.getValue();
            if (prop_val instanceof Collection<?>)
            {
                final Collection<?> coll = (Collection<?>) prop_val;
                if (coll.size() == 1)
                    return coll.iterator().next().toString();
            }
            return prop_val.toString();
        }
        return null;
    }
}
