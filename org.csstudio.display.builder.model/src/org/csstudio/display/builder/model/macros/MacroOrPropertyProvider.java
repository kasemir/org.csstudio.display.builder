/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.macros;

import java.util.Map;

import org.csstudio.display.builder.model.WidgetProperty;

/** Provides values from macros, falling back to widget properties
 *  @author Kay Kasemir
 */
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
        String value = macros.getValue(name);
        if (value != null)
            return value;

        final WidgetProperty<?> property = properties.get(name);
        if (property != null)
            return property.getValue().toString();
        return null;
    }
}
