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
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.w3c.dom.Element;

/** Widget property that describes macros.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class MacrosWidgetProperty extends WidgetProperty<Macros>
{
    /** Constructor
     *  @param descriptor Property descriptor
     *  @param widget Widget that holds the property and handles listeners
     *  @param default_value Default and initial value
     */
    public MacrosWidgetProperty(
            final WidgetPropertyDescriptor<Macros> descriptor,
            final Widget widget,
            final Macros default_value)
    {
        super(descriptor, widget, default_value);
    }

    /** @param value Must be ActionInfo array(!), not List */
    @Override
    public void setValueFromObject(final Object value) throws Exception
    {
        if (value instanceof Macros)
            setValue((Macros) value);
        else
            throw new Exception("Need Macros, got " + value);
    }

    @Override
    public void writeToXML(final XMLStreamWriter writer) throws Exception
    {
        writeMacros(writer, value);
    }

    @Override
    public void readFromXML(final Element property_xml) throws Exception
    {
        setValue(readMacros(property_xml));
    }

    /** Write content of <macros>
     *  @param writer
     *  @param value
     *  @throws Exception
     */
    static void writeMacros(final XMLStreamWriter writer, final Macros value) throws Exception
    {
        // TODO Write if parent macros are inherited
        for (String name : value.getNames())
        {
            writer.writeStartElement(name);
            writer.writeCharacters(value.getValue(name));
            writer.writeEndElement();
        }
    }

    /** Read content of <macros>
     *  @param property_xml
     */
    static Macros readMacros(final Element property_xml)
    {
        final Macros macros = new Macros();
        for (Element element = XMLUtil.findElement(property_xml.getFirstChild());
             element != null;
             element = XMLUtil.findElement(element.getNextSibling()))
        {
            final String name = element.getTagName();
            final String value = XMLUtil.getString(element);
            if (name.equals("include_parent_macros"))
            {
                // TODO Handle it
                // Legacy used 'include_parent_macros'
                // in a way that actually conflicts with a macro of that name.
                // Too late now..
            }
            else
                macros.add(name, value);
        }
        return macros;
    }
}
