/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.persist;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import org.csstudio.display.builder.model.properties.NamedWidgetColor;

/** Provider of {@link NamedWidgetColor}
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class NamedWidgetColors extends ConfigFileParser
{
    private final Map<String, NamedWidgetColor> colors = new LinkedHashMap<>();

    protected NamedWidgetColors()
    {
        defineDefaultColors();
    }

    private void defineDefaultColors()
    {
        define(new NamedWidgetColor("OK", 0, 255, 0));
        define(new NamedWidgetColor("MINOR", 255, 128, 0));
        define(new NamedWidgetColor("MAJOR", 255, 0, 0));
        define(new NamedWidgetColor("INVALID", 255, 0, 255));
        define(new NamedWidgetColor("DISCONNECTED", 255, 0, 255));
    }

    private void define(final NamedWidgetColor color)
    {
        colors.put(color.getName(), color);
    }

    /** Get named color
     *  @param name Name of the color
     *  @return Named color, if known
     */
    public Optional<NamedWidgetColor> getColor(final String name)
    {
        return Optional.ofNullable(colors.get(name));
    }

    @Override
    protected void parse(final String name, final String value) throws Exception
    {
        final StringTokenizer tokenizer = new StringTokenizer(value, ",");
        try
        {
            final int red = Integer.parseInt(tokenizer.nextToken().trim());
            final int green = Integer.parseInt(tokenizer.nextToken().trim());
            final int blue = Integer.parseInt(tokenizer.nextToken().trim());
            define(new NamedWidgetColor(name, red, green, blue));
        }
        catch (Throwable ex)
        {
            throw new Exception("Cannot parse color '" + name + "' from '" + value + "'", ex);
        }
    }
}
