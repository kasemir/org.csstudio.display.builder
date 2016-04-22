/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.util;

import static org.csstudio.display.builder.model.ModelPlugin.logger;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.csstudio.display.builder.model.properties.FormatOption;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.Display;
import org.diirt.vtype.VDouble;
import org.diirt.vtype.VEnum;
import org.diirt.vtype.VEnumArray;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VString;
import org.diirt.vtype.VStringArray;
import org.diirt.vtype.VType;

/** Utility for formatting data as string.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class FormatOptionHandler
{
    /** Cached formats for DECIMAL by precision */
    private final static ConcurrentHashMap<Integer, NumberFormat> decimal_formats = new ConcurrentHashMap<>();

    /** Cached formats for EXPONENTIAL by precision */
    private final static ConcurrentHashMap<Integer, NumberFormat> exponential_formats = new ConcurrentHashMap<>();

    /** Cached formats for ENGINEERING by precision */
    private final static ConcurrentHashMap<Integer, NumberFormat> engineering_formats = new ConcurrentHashMap<>();

    /** Format value as string
     *
     *  @param value Value to format
     *  @param option How to format the value
     *  @param precision Precision to use. Ignored for DEFAULT, otherwise details depend on option.
     *  @param show_units Include units?
     *  @return Formatted value
     */
    public static String format(final VType value, final FormatOption option,
                                final int precision, final boolean show_units)
    {
        if (value == null)
            return "<null>";
        if (value instanceof VNumber)
        {
            final VNumber number = (VNumber) value;
            final String text = formatNumber(number.getValue(), number, option, precision);
            if (show_units  &&  !number.getUnits().isEmpty())
                return text + " " + number.getUnits();
            return text;
        }
        else if (value instanceof VString)
            return ((VString)value).getValue();
        else if (value instanceof VEnum)
            return formatEnum((VEnum) value, option);
        else if (value instanceof VNumberArray)
        {
            final VNumberArray array = (VNumberArray) value;
            if (option == FormatOption.STRING)
                return getLongString(array);
            final ListNumber data = array.getData();
            if (data.size() <= 0)
                return "[]";
            final StringBuilder buf = new StringBuilder("[");
            buf.append(formatNumber(data.getDouble(0), array, option, precision));
            for (int i=1; i<data.size(); ++i)
            {
                buf.append(", ");
                buf.append(formatNumber(data.getDouble(i), array, option, precision));
            }
            buf.append("]");
            if (show_units  &&  !array.getUnits().isEmpty())
                buf.append(" ").append(array.getUnits());
            return buf.toString();
        }
        else if (value instanceof VEnumArray)
        {
            final List<String> labels = ((VEnumArray)value).getLabels();
            final StringBuilder buf = new StringBuilder("[");
            for (int i=0; i<labels.size(); ++i)
            {
                if (i > 0)
                    buf.append(", ");
                buf.append(labels.get(i));
            }
            buf.append("]");
            return buf.toString();
        }
        else if (value instanceof VStringArray)
            return StringList.join(((VStringArray)value).getData());

        return "<" + value.getClass().getName() + ">";
    }

    private static NumberFormat getDecimalFormat(final int precision)
    {
        return decimal_formats.computeIfAbsent(precision, FormatOptionHandler::createDecimalFormat);
    }

    private static NumberFormat createDecimalFormat(int precision)
    {
        if (precision <= 0)
            precision = 0;
        final NumberFormat fmt = NumberFormat.getNumberInstance();
        fmt.setGroupingUsed(false);
        fmt.setMinimumFractionDigits(precision);
        fmt.setMaximumFractionDigits(precision);
        return fmt;
    }

    private static NumberFormat getExponentialFormat(final int precision)
    {
        return exponential_formats.computeIfAbsent(precision, FormatOptionHandler::createExponentialFormat);
    }

    private static NumberFormat createExponentialFormat(final int precision)
    {
        // DecimalFormat needs pattern for exponential notation,
        // there are no factory or configuration methods
        final StringBuilder pattern = new StringBuilder("0");
        if (precision > 0)
            pattern.append('.');
        for (int i=0; i<precision; ++i)
            pattern.append('0');
        pattern.append("E0");
        return new DecimalFormat(pattern.toString());
    }

    private static NumberFormat getEngineeringFormat(final int precision)
    {
        return engineering_formats.computeIfAbsent(precision, FormatOptionHandler::createEngineeringFormat);
    }

    private static NumberFormat createEngineeringFormat(final int precision)
    {
        // Special case of DecimalFormat to get 'engineering' notation
        // where exponent it multiple of 3
        final StringBuilder pattern = new StringBuilder("##0.");
        // No way to control the number of fractional digits.
        // Total number of 'significant' digits will be 1 from "##0."
        // plus the trailing '0' count
        // --> precision determines total number of significant digits,
        //     combined from pre-and post fractional digits.
        for (int i=1; i<precision; ++i)
            pattern.append('0');
        pattern.append("E0");
        return new DecimalFormat(pattern.toString());
    }

    private static String formatNumber(final Number value, final Display display,
                                       final FormatOption option, final int precision)
    {
        // Handle invalid numbers
        if (Double.isNaN(value.doubleValue()))
            return "NaN";
        if (Double.isInfinite(value.doubleValue()))
            return Double.toString(value.doubleValue());

        if (option == FormatOption.DECIMAL)
            return getDecimalFormat(precision).format(value);
        if (option == FormatOption.EXPONENTIAL)
            return getExponentialFormat(precision).format(value);
        if (option == FormatOption.ENGINEERING)
            return getEngineeringFormat(precision).format(value);
        if (option == FormatOption.HEX)
        {
            final StringBuilder buf = new StringBuilder();
            if (precision <= 8)
                buf.append(Integer.toHexString(value.intValue()).toUpperCase());
            else
                buf.append(Long.toHexString(value.longValue()).toUpperCase());
            for (int i=buf.length(); i<precision; ++i)
                buf.insert(0, '0');
            buf.insert(0, "0x");
            return buf.toString();
        }
        if (option == FormatOption.STRING)
            return new String(new byte[] { value.byteValue() });
        if (option == FormatOption.COMPACT)
        {
            final double criteria = Math.abs(value.doubleValue());
            if (criteria > 0.0001  &&  criteria < 10000)
                return formatNumber(value, display, FormatOption.DECIMAL, precision);
            else
                return formatNumber(value, display, FormatOption.EXPONENTIAL, precision);
        }

        // DEFAULT
        final NumberFormat format = display.getFormat();
        if (format != null)
            return format.format(value);
        else
            return value.toString();
    }

    private static String formatEnum(final VEnum value, final FormatOption option)
    {
        if (option == FormatOption.DEFAULT  ||  option == FormatOption.STRING)
            return value.getValue();
        return Integer.toString(value.getIndex());
    }

    /** @param value Array of numbers
     *  @return String based on character for each array element
     */
    private static String getLongString(final VNumberArray value)
    {
        final ListNumber data = value.getData();
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

    /** Parse a string, presumably as formatted by this class,
     *  into a value suitable for writing back to the PV
     *
     *  @param value Last known value of the PV
     *  @param text Formatted text
     *  @return Object to write to PV for the 'text'
     */
    public static Object parse(final VType value, String text)
    {
        try
        {
            if (value instanceof VNumber)
            {   // Remove trailing text (units or part of units)
                text = text.trim();
                final int sep = text.lastIndexOf(' ');
                if (sep > 0)
                    text = text.substring(0, sep).trim();
                if (value instanceof VDouble)
                    return Double.parseDouble(text);
                return Long.parseLong(text);
            }
            if (value instanceof VEnum)
            {   // Send index for valid enumeration string
                final List<String> labels = ((VEnum)value).getLabels();
                text = text.trim();
                for (int i=0; i<labels.size(); ++i)
                    if (labels.get(i).equals(text))
                        return i;
                // Otherwise write the string
                return text;
            }
            if (value instanceof VNumberArray)
            {
                text = text.trim();
                if (text.startsWith("["))
                    text = text.substring(1);
                if (text.endsWith("]"))
                    text = text.substring(0, text.length()-1);
                final String[] items = text.split(" *, *");
                final double[] array = new double[items.length];
                for (int i=0; i<array.length; ++i)
                    array[i] = Double.parseDouble(items[i].trim());
                return array;
            }
            if (value instanceof VStringArray)
            {
                final List<String> items = StringList.split(text);
                return items.toArray(new String[items.size()]);
            }
        }
        catch (Throwable ex)
        {
            logger.log(Level.WARNING, "Error parsing value from '" +  text + "', will use as is", ex);
        }
        return text;
    }
}
