/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.macros;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Macro information
 *
 *  <p>Holds macros and their value
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Macros implements MacroValueProvider
{
    private final Map<String, String> macros = new HashMap<>();

    /** Create empty macro map */
    public Macros()
    {
    }

    /** Merge two macro maps
     *
     *  @param base Base macros
     *  @param addition Additional macros that may override 'base'
     *  @return Merged macros
     */
    public static Macros merge(final Macros base, final Macros addition)
    {
        // Optimize if one is empty
        if (addition == null  ||  addition.macros.isEmpty())
            return base;
        if (base == null  ||  base.macros.isEmpty())
            return addition;
        // Construct new macros
        final Macros merged = new Macros();
        merged.macros.putAll(base.macros);
        merged.macros.putAll(addition.macros);
        return merged;
    }

    /** Add a macro
     *  @param name Name of the macro
     *  @param value Value of the macro
     */
    public void add(final String name, final String value)
    {
        macros.put(name, value);
    }

    /** @return Macro names, sorted alphabetically */
    public Collection<String> getNames()
    {
        final List<String> names = new ArrayList<>(macros.keySet());
        Collections.sort(names);
        return names;
    }

    /** {@inheritDoc} */
    @Override
    public String getValue(final String name)
    {
        return macros.get(name);
    }

    // Hash based on content
    @Override
    public int hashCode()
    {
        return macros.hashCode();
    }

    // Compare based on content
    @Override
    public boolean equals(final Object obj)
    {
        if (! (obj instanceof Macros))
            return false;
        final Macros other = (Macros) obj;
        return other.macros.equals(macros);
    }

    /** @return String representation for debugging */
    @Override
    public String toString()
    {
        return "[" + getNames().stream()
                               .map((macro) -> macro + " = '" + macros.get(macro) + "'")
                               .collect(Collectors.joining(", ")) +
               "]";
    }
}
