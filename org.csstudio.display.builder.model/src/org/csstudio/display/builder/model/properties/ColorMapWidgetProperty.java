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
        if (value instanceof ColorMap.Predefined)
        {
            final ColorMap.Predefined map = (ColorMap.Predefined) value;
            writer.writeStartElement(XMLTags.NAME);
            writer.writeCharacters(map.getName());
            writer.writeEndElement();
        }
        else
        {
            final int[][] sections = value.getSections();
            for (int i=0; i<sections.length; ++i)
            {
                writer.writeEmptyElement("section");
                writer.writeAttribute(XMLTags.VALUE, Integer.toString(sections[i][0]));
                writer.writeAttribute(XMLTags.RED, Integer.toString(sections[i][1]));
                writer.writeAttribute(XMLTags.GREEN, Integer.toString(sections[i][2]));
                writer.writeAttribute(XMLTags.BLUE, Integer.toString(sections[i][3]));
            }
        }
    }

    @Override
    public void readFromXML(final ModelReader model_reader, final Element property_xml) throws Exception
    {
        // Translate legacy <map>2</map>
        final Optional<Integer> legacy_map = XMLUtil.getChildInteger(property_xml, "map");
        if (legacy_map.isPresent())
        {
            switch (legacy_map.get())
            {
            case 1: // GrayScale
                setValue(ColorMap.GRAY);
                break;
            case 2: // JET
                setValue(ColorMap.JET);
                break;
            case 3: // ColorSpectrum
                setValue(ColorMap.SPECTRUM);
                break;
            case 4: // Hot
                setValue(ColorMap.HOT);
                break;
            case 5: // Cool
                setValue(ColorMap.COOL);
                break;
            case 6: // Shaded
                setValue(ColorMap.SHADED);
                break;
            }
            return;
        }

        // TODO Read legacy sections?
        // <map>
        //   <e red="0" green="0" blue="0">0.0</e>
        //   <e red="255" green="255" blue="255">1.0</e>
        // </map>

        // Named (predefined) color map?
        final Optional<String> name = XMLUtil.getChildString(property_xml, XMLTags.NAME);
        if (name.isPresent())
        {
            for (ColorMap.Predefined map : ColorMap.PREDEFINED)
                if (map.getName().equals(name.get()))
                {
                    setValue(map);
                    break;
                }
            return;
        }
        // Sectional color map?
        final int[][] sections = new int[256][4];
        int entries = 0;
        for (Element section : XMLUtil.getChildElements(property_xml, "section"))
        {
            if (entries >= sections.length)
                throw new Exception("More than " + sections.length + " color map sections");
            sections[entries][0] = Integer.parseInt(section.getAttribute(XMLTags.VALUE));
            sections[entries][1] = Integer.parseInt(section.getAttribute(XMLTags.RED));
            sections[entries][2] = Integer.parseInt(section.getAttribute(XMLTags.GREEN));
            sections[entries][3] = Integer.parseInt(section.getAttribute(XMLTags.BLUE));
            ++entries;
        }
        if (entries > 0)
        {   // Shrink array
            final int[][] used = new int[entries][4];
            for (int i=0; i<entries; ++i)
                System.arraycopy(sections[i], 0, used[i], 0, 4);
            setValue(new ColorMap(used));
        }
    }
}
