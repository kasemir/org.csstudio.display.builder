/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.util;

import java.text.NumberFormat;

import org.epics.vtype.VEnum;
import org.epics.vtype.VNumber;
import org.epics.vtype.VString;
import org.epics.vtype.VType;

/** Utility for displaying VType data.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class VTypeUtil
{
    /** Format a value as text.
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
        // TODO Handle VNumberArray, VStringArray, VEnumArray
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
        // TODO Handle VNumberArray, VEnumArray?
        return Double.valueOf(Double.NaN);
    }
}
