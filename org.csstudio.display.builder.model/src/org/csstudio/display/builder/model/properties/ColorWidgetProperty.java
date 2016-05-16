/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
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
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.persist.XMLTags;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.w3c.dom.Element;

/** Widget property with Color as value.
 *
 *  <p>Named colors are written with their name and the current RGB data.
 *
 *  <p>When loading a named color, an attempt is made to obtain the
 *  current definition of that color from the {@link WidgetColorService}.
 *  If the color is not known by name, the RGB data from the saved config
 *  is used, but the color still keeps its name so that it can be saved
 *  with that name and later loaded as a known color.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ColorWidgetProperty extends WidgetProperty<WidgetColor>
{
    /** Constructor
     *  @param descriptor Property descriptor
     *  @param widget Widget that holds the property and handles listeners
     *  @param default_value Default and initial value
     */
    public ColorWidgetProperty(
            final WidgetPropertyDescriptor<WidgetColor> descriptor,
            final Widget widget,
            final WidgetColor default_value)
    {
        super(descriptor, widget, default_value);
    }

    @Override
    public void setValueFromObject(final Object value) throws Exception
    {
        if (value instanceof WidgetColor)
            setValue( (WidgetColor) value);
        else
            throw new IllegalArgumentException(String.valueOf(value));
    }

    @Override
    public void writeToXML(final ModelWriter model_writer, final XMLStreamWriter writer) throws Exception
    {
        writer.writeStartElement(XMLTags.COLOR);
        if (value instanceof NamedWidgetColor)
            writer.writeAttribute(XMLTags.NAME, ((NamedWidgetColor) value).getName());
        writer.writeAttribute(XMLTags.RED, Integer.toString(value.getRed()));
        writer.writeAttribute(XMLTags.GREEN, Integer.toString(value.getGreen()));
        writer.writeAttribute(XMLTags.BLUE, Integer.toString(value.getBlue()));
        writer.writeEndElement();
    }

    @Override
    public void readFromXML(final ModelReader model_reader, final Element property_xml) throws Exception
    {
        final Element col_el = XMLUtil.getChildElement(property_xml, XMLTags.COLOR);
        if (col_el == null)
            return;

        final String name = col_el.getAttribute(XMLTags.NAME);
        final int red = getAttrib(col_el, XMLTags.RED);
        final int green = getAttrib(col_el, XMLTags.GREEN);
        final int blue = getAttrib(col_el, XMLTags.BLUE);

        final WidgetColor color;
        if (name.isEmpty())
            // Plain color
            color = new WidgetColor(red, green, blue);
        else
            color = WidgetColorService.getColors().resolve(new NamedWidgetColor(name, red, green, blue));
        setValue(color);
    }

    private int getAttrib(final Element element, final String attrib)
    {
        final String text = element.getAttribute(attrib);
        if (text.isEmpty())
           throw new IllegalStateException("<color> without " + attrib);
        return Integer.parseInt(text);
    }
}
