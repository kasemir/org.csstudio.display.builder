/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.ConcurrentHashMap;

import org.csstudio.display.builder.model.properties.FormatOption;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.VType;

/** Utility for displaying VType data.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class FormatOptionHandler
{
    /** Idea of caching the formats similar to DIIRT NumberFormats,
     *  but thread-safe and extended beyond decimal formats
     */
    private final static ConcurrentHashMap<Integer, NumberFormat> decimal_formats = new ConcurrentHashMap<>();

    private final static ConcurrentHashMap<Integer, NumberFormat> exponential_formats = new ConcurrentHashMap<>();

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
        if (value instanceof VNumber)
        {
            final VNumber cast = (VNumber) value;
            final NumberFormat format = cast.getFormat();
            final String text;
            text = formatNumber(cast, format, option, precision);
            if (show_units  &&  !cast.getUnits().isEmpty())
                return text + " " + cast.getUnits();
            return text;
        }
        return null;
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

    private static String formatNumber(final VNumber number, final NumberFormat format,
                                       final FormatOption option, final int precision)
    {
        final Number value = number.getValue();
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
                return formatNumber(number, format, FormatOption.DECIMAL, precision);
            else
                return formatNumber(number, format, FormatOption.EXPONENTIAL, precision);
        }

        // DEFAULT
        if (format != null)
            return format.format(value);
        else
            return value.toString();
    }
}
