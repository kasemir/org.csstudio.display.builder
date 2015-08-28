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

import org.csstudio.display.builder.model.properties.NamedWidgetFont;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.csstudio.display.builder.model.properties.WidgetFontStyle;

/** Provider of {@link NamedWidgetFont}
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class NamedWidgetFonts extends ConfigFileParser
{
    private final Map<String, NamedWidgetFont> fonts = new LinkedHashMap<>();

    protected NamedWidgetFonts()
    {
        defineDefaultFonts();
    }

    private void defineDefaultFonts()
    {
        final WidgetFont base = WidgetFont.getDefault();
        define(new NamedWidgetFont("Default", base.getFamily(), WidgetFontStyle.REGULAR, base.getSize()));
        define(new NamedWidgetFont("Default Bold", base.getFamily(), WidgetFontStyle.BOLD, base.getSize()));
        define(new NamedWidgetFont("Header 1", base.getFamily(), WidgetFontStyle.BOLD, base.getSize() + 8));
        define(new NamedWidgetFont("Header 2", base.getFamily(), WidgetFontStyle.BOLD, base.getSize() + 4));
        define(new NamedWidgetFont("Header 3", base.getFamily(), WidgetFontStyle.BOLD, base.getSize() + 2));
        define(new NamedWidgetFont("Comment", base.getFamily(), WidgetFontStyle.ITALIC, base.getSize()));
        define(new NamedWidgetFont("Fine Print", base.getFamily(), WidgetFontStyle.REGULAR, base.getSize() - 2));
    }

    private void define(final NamedWidgetFont font)
    {
        fonts.put(font.getName(), font);
    }

    /** Get named font
     *  @param name Name of the font
     *  @return Named font, if known
     */
    public Optional<NamedWidgetFont> getFont(final String name)
    {
        return Optional.ofNullable(fonts.get(name));
    }

    /** Get all named fonts
     *  @return Collection of all named fonts
     */
    public Collection<NamedWidgetFont> getFonts()
    {
        return Collections.unmodifiableCollection(fonts.values());
    }

    @Override
    protected void parse(final String name, final String value) throws Exception
    {
        // TODO Check if name is 'qualified' by OS

        final StringTokenizer tokenizer = new StringTokenizer(value, "-");
        try
        {
            String family = tokenizer.nextToken().trim();
            final WidgetFontStyle style = parseStyle(tokenizer.nextToken().trim());
            final double size = Double.parseDouble(tokenizer.nextToken().trim());

            if (family.equalsIgnoreCase("SystemDefault"))
                family = WidgetFont.getDefault().getFamily();

            final NamedWidgetFont font = new NamedWidgetFont(name, family, style, size);
            define(font);
        }
        catch (Throwable ex)
        {
            throw new Exception("Cannot parse font '" + name + "' from '" + value + "'", ex);
        }
    }

    private WidgetFontStyle parseStyle(final String style_text)
    {
        switch (style_text.toLowerCase())
        {
        case "bold":
            return WidgetFontStyle.BOLD;
        case "italic":
            return WidgetFontStyle.ITALIC;
        case "bold italic":
            return WidgetFontStyle.BOLD_ITALIC;
        default:
            return WidgetFontStyle.REGULAR;
        }
    }
}
