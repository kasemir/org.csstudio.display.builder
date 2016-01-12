/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.Objects;

import javax.xml.stream.XMLStreamWriter;

import org.csstudio.display.builder.model.MacroizedWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.w3c.dom.Element;

/** Widget property with enumerated value.
 *
 *  <p>Requires an actual enum as a value,
 *  and code that is aware of that enum can
 *  get/set the property as such.
 *
 *  <p>Enums that are exposed to the user should
 *  have a localized string representation (Label)
 *  that is provided by the <code>toString()</code>
 *  method of the enum, while code uses the enum's
 *  <code>ordinal()</code> or <code>name()</code>.
 *
 *  <p>Property offers helpers to obtain all 'Labels'
 *  and to set an unknown enum from its name or ordinal.
 *
 *  <p>Note that there is no support to set the property
 *  from a 'Label'. User code should present the value
 *  as a label, but then set it as the ordinal or,
 *  if exact type is known, the actual enum.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EnumWidgetProperty<E extends Enum<E>> extends MacroizedWidgetProperty<E>
{
    /** Constructor
     *  @param descriptor Property descriptor
     *  @param widget Widget that holds the property and handles listeners
     *  @param default_value Default and initial value
     */
    public EnumWidgetProperty(final WidgetPropertyDescriptor<E> descriptor,
                              final Widget widget,
                              final E default_value)
    {
        // Default value must be non-null because it's used to determine
        // labels, names, ordinals
        super(descriptor, widget, Objects.requireNonNull(default_value));
    }

    /** @return Labels, i.e. String representations of all enum values */
    public String[] getLabels()
    {
        final Object[] values = default_value.getDeclaringClass().getEnumConstants();
        final String[] labels = new String[values.length];
        for (int i=0; i<labels.length; ++i)
            labels[i] = values[i].toString();
        return labels;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected E parseExpandedSpecification(final String text) throws Exception
    {
        if (text == null  ||  text.isEmpty())
            return default_value;
        // Locate matching enum name (not label!)
        for (Enum<E> value : default_value.getDeclaringClass().getEnumConstants())
            if (value.name().equals(text))
                return (E)value;

        throw new Exception("Enum property '" + getName() + "' received invalid value " + text);
    }

    @Override
    public void setValue(final E value)
    {
        if (isReadonly())
            return;
        specification = value.name();
        final E old_value = this.value;
        this.value = value;
        firePropertyChange(this, old_value, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValueFromObject(final Object value) throws Exception
    {   // Proper type?
        if (value instanceof Enum<?>  &&
            ((Enum<?>)value).getDeclaringClass() == default_value.getDeclaringClass())
            setValue((E) value);
        else if (value instanceof Number)
        {   // Use ordinal
            final int ordinal = ((Number)value).intValue();
            final E[] values = default_value.getDeclaringClass().getEnumConstants();
            if (ordinal < 0  ||  ordinal >= values.length)
                throw new Exception("Invalid ordinal " + ordinal + " for " + getName());
            setValue((E) values[ordinal]);
        }
        else // Use name
            setValue(parseExpandedSpecification(value.toString()));
    }

    @Override
    public void writeToXML(final XMLStreamWriter writer) throws Exception
    {
        writer.writeCharacters(specification);
    }

    @Override
    public void readFromXML(final Element property_xml) throws Exception
    {
        setSpecification(XMLUtil.getString(property_xml));
    }
}
