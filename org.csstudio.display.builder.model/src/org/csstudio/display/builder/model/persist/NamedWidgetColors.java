/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.persist;

import java.util.Collection;
import java.util.Collections;
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
    public static final NamedWidgetColor ALARM_OK = new NamedWidgetColor("OK", 0, 255, 0);
    public static final NamedWidgetColor ALARM_MINOR = new NamedWidgetColor("MINOR", 255, 128, 0);
    public static final NamedWidgetColor ALARM_MAJOR = new NamedWidgetColor("MAJOR", 255, 0, 0);
    public static final NamedWidgetColor ALARM_INVALID = new NamedWidgetColor("INVALID", 255, 0, 255);
    public static final NamedWidgetColor ALARM_DISCONNECTED = new NamedWidgetColor("DISCONNECTED", 255, 0, 255);

    public static final NamedWidgetColor TEXT = new NamedWidgetColor("Text", 0, 0, 0);
    public static final NamedWidgetColor BACKGROUND = new NamedWidgetColor("Background", 255, 255, 255);
    public static final NamedWidgetColor READ_BACKGROUND = new NamedWidgetColor("Read_Background", 240, 240, 240);
    public static final NamedWidgetColor WRITE_BACKGROUND = new NamedWidgetColor("Write_Background", 128, 255, 255);


    private final Map<String, NamedWidgetColor> colors = new LinkedHashMap<>();

    protected NamedWidgetColors()
    {
        defineDefaultColors();
    }

    private void defineDefaultColors()
    {
        define(ALARM_OK);
        define(ALARM_MINOR);
        define(ALARM_MAJOR);
        define(ALARM_INVALID);
        define(ALARM_DISCONNECTED);
        define(TEXT);
        define(BACKGROUND);
        define(READ_BACKGROUND);
        define(WRITE_BACKGROUND);
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

    /** Get all named colors
     *  @return Collection of all named colors
     */
    public Collection<NamedWidgetColor> getColors()
    {
        return Collections.unmodifiableCollection(colors.values());
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
