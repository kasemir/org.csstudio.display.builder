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
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.w3c.dom.Element;

/** Helper for implementing Runtime properties.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
abstract public class RuntimeWidgetProperty<T> extends WidgetProperty<T>
{
    /** Constructor
     *  @param descriptor Property descriptor
     *  @param widget Widget that holds the property and handles listeners
     *  @param default_value Default and initial value
     */
    public RuntimeWidgetProperty(
            final WidgetPropertyDescriptor<T> descriptor,
            final Widget widget,
            final T default_value)
    {
        super(descriptor, widget, default_value);
        if (descriptor.getCategory() != WidgetPropertyCategory.RUNTIME)
            throw new IllegalArgumentException("Must be a runtime property");
    }

    @Override
    public void writeToXML(final XMLStreamWriter writer) throws Exception
    {
        throw new Exception("Runtime property " + getName() + " is not persisted");
    }

    @Override
    public void readFromXML(final Element property_xml) throws Exception
    {
        throw new Exception("Runtime property " + getName() + " is not persisted");
    }
}
