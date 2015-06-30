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

/** Widget property with Color as value.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ColorWidgetProperty extends WidgetProperty<WidgetColor>
{
    private static final String BLUE = "blue";
    private static final String GREEN = "green";
    private static final String RED = "red";

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
    public void writeToXML(final XMLStreamWriter writer) throws Exception
    {
        writer.writeStartElement(XMLTags.COLOR);
        writer.writeAttribute(RED, Integer.toString(value.getRed()));
        writer.writeAttribute(GREEN, Integer.toString(value.getGreen()));
        writer.writeAttribute(BLUE, Integer.toString(value.getBlue()));
        writer.writeEndElement();
    }

    @Override
    public void readFromXML(final Element property_xml) throws Exception
    {
        final Element col_el = XMLUtil.getChildElement(property_xml, XMLTags.COLOR);
        if (col_el == null)
            return;
        final int red = getAttrib(col_el, RED);
        final int green = getAttrib(col_el, GREEN);
        final int blue = getAttrib(col_el, BLUE);
        setValue(new WidgetColor(red, green, blue));
    }

    private int getAttrib(final Element element, final String attrib)
    {
        final String text = element.getAttribute(attrib);
        if (text.isEmpty())
           throw new IllegalStateException("<color> without " + attrib);
        return Integer.parseInt(text);
    }
}
