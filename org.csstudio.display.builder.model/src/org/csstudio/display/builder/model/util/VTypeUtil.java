/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.util;

import java.text.NumberFormat;
import java.util.List;

import org.diirt.util.array.ListByte;
import org.diirt.util.array.ListInt;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VByteArray;
import org.diirt.vtype.VEnum;
import org.diirt.vtype.VEnumArray;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VString;
import org.diirt.vtype.VType;

/** Utility for displaying VType data.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class VTypeUtil
{
    /** Convert byte array into String
     *  @param barray {@link VByteArray}
     *  @return {@link String}
     */
    final public static String toString(final VByteArray barray)
    {
        final ListByte data = barray.getData();
        final byte[] bytes = new byte[data.size()];
        // Copy bytes until end or '\0'
        int len = 0;
        while (len<bytes.length)
        {
            final byte b = data.getByte(len);
            if (b == 0)
                break;
            else
                bytes[len++] = b;
        }
        return new String(bytes, 0, len);
    }

    /** Format a value as text.
     *
     *  <p>Byte arrays are treated as long strings.
     *
     *  TODO Add equivalent of org.csstudio.simplepv.FormatEnum
     *
     *  @param value VType
     *  @param with_units Add units?
     *  @return Text for value (without timestamp, alarm, ..)
     */
    public static String getValueString(final VType value, final boolean with_units)
    {
        if (value instanceof VNumber)
        {
            final VNumber cast = (VNumber) value;
            final NumberFormat format = cast.getFormat();
            final String text;
            if (format != null)
                text = format.format(cast.getValue());
            else
                text = cast.getValue().toString();
            if (with_units  &&  !cast.getUnits().isEmpty())
                return text + " " + cast.getUnits();
            return text;
        }
        if (value instanceof VString)
            return ((VString)value).getValue();
        if (value instanceof VEnum)
            return ((VEnum)value).getValue();
        if (value instanceof VByteArray)
            return toString((VByteArray) value);
        if (value instanceof VNumberArray)
        {
            final VNumberArray cast = (VNumberArray)value;
            final ListNumber numbers = cast.getData();
            final NumberFormat format = cast.getFormat();
            final StringBuilder text = new StringBuilder("[");
            for (int i=0; i<numbers.size(); ++i)
            {
                if (i > 0)
                    text.append(", ");
                if (format != null)
                    text.append(format.format(numbers.getDouble(i)));
                else
                    text.append(numbers.getDouble(i));
            }
            if (with_units  &&  !cast.getUnits().isEmpty())
                text.append(" ").append(cast.getUnits());
            text.append("]");
            return text.toString();
        }
        if (value instanceof VEnumArray)
        {
            final List<String> labels = ((VEnumArray)value).getLabels();
            final StringBuilder text = new StringBuilder("[");
            for (int i=0; i<labels.size(); ++i)
            {
                if (i > 0)
                    text.append(", ");
                text.append(labels.get(i));
            }
            text.append("]");
            return text.toString();
        }
        if (value == null)
            return "<null>";
        return "<" + value.getClass().getName() + ">";
    }

    /** Obtain numeric value
     *  @param value VType
     *  @return Number for value.
     *          <code>Double.NaN</code> in case the value type
     *          does not decode into a number.
     */
    public static Number getValueNumber(final VType value)
    {
        if (value instanceof VNumber)
        {
            final VNumber cast = (VNumber) value;
            return cast.getValue();
        }
        if (value instanceof VEnum)
            return ((VEnum)value).getIndex();
        // For arrays, return first element
        if (value instanceof VNumberArray)
        {
            final ListNumber array = ((VNumberArray)value).getData();
            if (array.size() > 0)
                return array.getDouble(0);
        }
        if (value instanceof VEnumArray)
        {
            final ListInt array = ((VEnumArray)value).getIndexes();
            if (array.size() > 0)
                return array.getInt(0);
        }
        return Double.valueOf(Double.NaN);
    }
}
