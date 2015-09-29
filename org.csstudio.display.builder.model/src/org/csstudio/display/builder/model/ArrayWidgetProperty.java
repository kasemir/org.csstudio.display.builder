/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamWriter;

import org.csstudio.display.builder.model.persist.XMLUtil;
import org.w3c.dom.Element;

/** Widget property that contains array of widget properties.
 *
 *  <p>Individual elements need to be modified via this class.
 *  getValue() returns a read-only list.
 *
 *  @author Kay Kasemir
 *  @param <E> Type of each item's property
 */
@SuppressWarnings("nls")
public class ArrayWidgetProperty<E> extends WidgetProperty<List<WidgetProperty<E>>>
{
    /** Descriptor of an array property */
    public static class Descriptor<E> extends WidgetPropertyDescriptor<List<WidgetProperty<E>>>
    {
        public Descriptor(final WidgetPropertyCategory category,
                          final String name, final String description)
        {
            super(category, name, description);
        }

        @Override
        public ArrayWidgetProperty<E> createProperty(
                final Widget widget, final List<WidgetProperty<E>> elements)
        {
            return new ArrayWidgetProperty<E>(this, widget, elements);
        }
    };

    protected ArrayWidgetProperty(final Descriptor<E> descriptor,
            final Widget widget, final List<WidgetProperty<E>> elements)
    {
        // Default's elements can be changed, they each track their own 'default',
        // but overall default_value List<> is not modifiable
        super(descriptor, widget, Collections.unmodifiableList(elements));
    }



// TODO Define what overall 'isReadonly()' means
//    /** @return <code>true</code> if all elements are read-only */
//    @Override
//    public boolean isReadonly()
//    {
//        for (WidgetProperty<E> element : value)
//            if (! element.isReadonly())
//                return false;
//        return true;
//    }

    @Override
    public boolean isDefaultValue()
    {
        for (WidgetProperty<E> element : value)
            if (! element.isDefaultValue())
                return false;
        return true;
    }

    /** Access element
     *  @param index Element index, 0 .. (<code>getValue().size()</code>-1)
     *  @return Element of array
     */
    public  WidgetProperty<E> getElement(final int index)
    {
        return value.get(index);
    }

    @Override
    public void setValueFromObject(final Object value) throws Exception
    {
        // TODO setValueFromObject(List)
        throw new Exception("Array elements for " + getName() + " cannot be set from Object, yet");
    }

    @Override
    public void writeToXML(final XMLStreamWriter writer) throws Exception
    {
        for (WidgetProperty<E> element : value)
        {
            writer.writeStartElement(element.getName());
            element.writeToXML(writer);
            writer.writeEndElement();
        }
    }

    @Override
    public void readFromXML(final Element property_xml) throws Exception
    {
        for (WidgetProperty<E> element : value)
        {
            final Element xml = XMLUtil.getChildElement(property_xml, element.getName());
            if (xml == null)
                continue;
            try
            {
                element.readFromXML(xml);
            }
            catch (Exception ex)
            {
                Logger.getLogger(getClass().getName())
                      .log(Level.WARNING, "Error reading " + getName() + " element " + element.getName(), ex);
            }
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder("'" + getName() + "' = [ ");
        boolean first = true;
        for (WidgetProperty<E> element : value)
        {
            if (first)
                first = false;
            else
                buf.append(", ");
            buf.append(element);
        }
        buf.append(" ]");
        return buf.toString();
    }
}
