/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import javax.xml.stream.XMLStreamWriter;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.XMLTags;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.w3c.dom.Element;

/** Widget property with font as value.
 *
 *  <p>Named fonts are written with their name and the current font info.
 *
 *  <p>When loading a named font, an attempt is made to obtain the
 *  current definition of that font from the {@link WidgetFontService}.
 *  If the font is not known by name, the info from the saved config
 *  is used, but the font still keeps its name so that it can be saved
 *  with that name and later loaded as a known font.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class FontWidgetProperty extends WidgetProperty<WidgetFont>
{
    private static double legacy_size_calibration = 0.75;

    private static final String FAMILY = "family";
    private static final String STYLE = "style";
    private static final String SIZE = "size";

    /** Set calibration factor for fonts loaded from legacy files
     *  @param factor Factor by which legacy displays would need to be scaled to match current font sizes
     */
    public static void setLegacyFontSizeCalibration(final double factor)
    {
        legacy_size_calibration = factor;
    }


    /** Constructor
     *  @param descriptor Property descriptor
     *  @param widget Widget that holds the property and handles listeners
     *  @param default_value Default and initial value
     */
    public FontWidgetProperty(
            final WidgetPropertyDescriptor<WidgetFont> descriptor,
            final Widget widget,
            final WidgetFont default_value)
    {
        super(descriptor, widget, default_value);
    }

    @Override
    public void setValueFromObject(final Object value) throws Exception
    {
        if (value instanceof WidgetFont)
            setValue( (WidgetFont) value);
        else
            throw new IllegalArgumentException(String.valueOf(value));
    }

    @Override
    public void writeToXML(final XMLStreamWriter writer) throws Exception
    {
        writer.writeStartElement(XMLTags.FONT);
        if (value instanceof NamedWidgetFont)
            writer.writeAttribute(XMLTags.NAME, ((NamedWidgetFont) value).getName());
        writer.writeAttribute(FAMILY, value.getFamily());
        writer.writeAttribute(STYLE, value.getStyle().name());
        writer.writeAttribute(SIZE, Double.toString(value.getSize()));
        writer.writeEndElement();
    }

    @Override
    public void readFromXML(final Element property_xml) throws Exception
    {
        final String name, family;
        final WidgetFontStyle style;
        final double size;

        Element font_el = XMLUtil.getChildElement(property_xml, XMLTags.FONT);
        if (font_el != null)
        {   // Current format:  <font name="Name" family="Liberation Sans" style="BOLD" size="18" />
            name = font_el.getAttribute(XMLTags.NAME);
            family = font_el.getAttribute(FAMILY);
            style = WidgetFontStyle.valueOf(font_el.getAttribute(STYLE));
            size = Double.parseDouble(font_el.getAttribute(SIZE));
        }
        else
        {   // Legacy *.opi used either
            // <opifont.name fontName="Sans" height="18" style="1">Header 1</opifont.name>
            font_el = XMLUtil.getChildElement(property_xml, "opifont.name");
            if (font_el != null)
            {
                name = XMLUtil.getString(font_el);
                family = font_el.getAttribute("fontName");
                style = WidgetFontStyle.values()[Integer.parseInt(font_el.getAttribute(STYLE))];
                size = Double.parseDouble(font_el.getAttribute("height")) / legacy_size_calibration;
            }
            else
            {   // or   <fontdata fontName="Sans" height="20" style="3" />
                font_el = XMLUtil.getChildElement(property_xml, "fontdata");
                if (font_el != null)
                {
                    name = "";
                    family = font_el.getAttribute("fontName");
                    style = WidgetFontStyle.values()[Integer.parseInt(font_el.getAttribute(STYLE))];
                    size = Double.parseDouble(font_el.getAttribute("height")) / legacy_size_calibration;
                }
                else
                    throw new Exception("Cannot parse font");
            }
        }

        final WidgetFont font;
        if (name.isEmpty())
            font = new WidgetFont(family, style, size);
        else
        {
// TODO       final Optional<NamedWidgetFont> known_font = WidgetFontService.getFonts().getFont(name);
//            if (known_font.isPresent())
//                // Known named font
//                font = known_font.get();
//            else
//                // Unknown named font: Use name with info values from file, lacking own definition of that font
//                font = new NamedWidgetFont(name, red, green, blue);
            font = new NamedWidgetFont(name, family, style, size);
        }
        setValue(font);
    }
}
