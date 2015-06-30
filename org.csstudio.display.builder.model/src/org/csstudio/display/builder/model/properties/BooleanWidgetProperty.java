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
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.w3c.dom.Element;

/** Widget property with Boolean value.
 *
 *  @author Kay Kasemir
 */
public class BooleanWidgetProperty extends WidgetProperty<Boolean>
{
    /** Constructor
     *  @param descriptor Property descriptor
     *  @param widget Widget that holds the property and handles listeners
     *  @param default_value Default and initial value
     */
    public BooleanWidgetProperty(
            final WidgetPropertyDescriptor<Boolean> descriptor,
            final Widget widget,
            final Boolean default_value)
    {
        super(descriptor, widget, default_value);
    }

    @Override
    public void setValueFromObject(final Object value) throws Exception
    {
        if (value instanceof Boolean)
            setValue( (Boolean) value);
        else
            setValue(Boolean.valueOf(value.toString()));
    }

    @Override
    public void writeToXML(final XMLStreamWriter writer) throws Exception
    {
        writer.writeCharacters(Boolean.toString(value));
    }

    @Override
    public void readFromXML(final Element property_xml) throws Exception
    {
        setValue(XMLUtil.parseBoolean(XMLUtil.getString(property_xml), default_value));
    }
}
