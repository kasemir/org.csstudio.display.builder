/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.Optional;

import javax.xml.stream.XMLStreamWriter;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.persist.XMLTags;
import org.csstudio.display.builder.model.persist.XMLUtil;
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
    public boolean isDefaultValue()
    {
        return false; // TODO Remove, only to force XML
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
    public void writeToXML(final ModelWriter model_writer, final XMLStreamWriter writer) throws Exception
    {
        Optional<ColorMap.Predefined> map = value.getPredefined();
        if (map.isPresent())
        {
            writer.writeStartElement(XMLTags.NAME);
            writer.writeCharacters(map.get().name());
            writer.writeEndElement();
        }
        else
        {
            // TODO Write sections of color map
            // <map>
            //   <e red="0" green="0" blue="0">0.0</e>
            //   <e red="255" green="255" blue="255">1.0</e>
            // </map>
        }
    }

    @Override
    public void readFromXML(final ModelReader model_reader, final Element property_xml) throws Exception
    {
        // TODO Read colormap sections
        // TODO Translate legacy <map>2</map>

        XMLUtil.getChildString(property_xml, XMLTags.NAME)
               .ifPresent(name -> setValue(ColorMap.Predefined.valueOf(name).get()));
    }
}
