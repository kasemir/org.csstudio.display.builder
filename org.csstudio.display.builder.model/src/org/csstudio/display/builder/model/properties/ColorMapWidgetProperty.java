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
import org.w3c.dom.Element;

/** Widget property with ColorMap as value.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ColorMapWidgetProperty extends WidgetProperty<ColorMap>
{
    /** Constructor
     *  @param descriptor Property descriptor
     *  @param widget Widget that holds the property and handles listeners
     *  @param default_value Default and initial value
     */
    public ColorMapWidgetProperty(
            final WidgetPropertyDescriptor<ColorMap> descriptor,
            final Widget widget,
            final ColorMap default_value)
    {
        super(descriptor, widget, default_value);
    }

    @Override
    public void setValueFromObject(final Object value) throws Exception
    {
        if (value instanceof ColorMap)
            setValue( (ColorMap) value);
        else
            throw new IllegalArgumentException(String.valueOf(value));
    }

    @Override
    public void writeToXML(final XMLStreamWriter writer) throws Exception
    {
        // TODO
        writer.writeStartElement("color_map");
        writer.writeEndElement();
    }

    @Override
    public void readFromXML(final Element property_xml) throws Exception
    {
        // TODO
    }
}
