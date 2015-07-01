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
public class Macros
{
    private final Map<String, String> macros = new HashMap<>();

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

    /** Get value for macro
     *  @param name Name of the macro
     *  @return Value of the macro or <code>null</code> if not defined
     */
    public String getValue(final String name)
    {
        return macros.get(name);
    }

    /** @return String representation for debugging */
    @Override
    public String toString()
    {
        return getNames().stream()
                         .map((macro) -> macro + " = '" + macros.get(macro) + "'")
                         .collect(Collectors.joining(", "));
    }
}
