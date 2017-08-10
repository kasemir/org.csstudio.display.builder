/*******************************************************************************
 * Copyright (c) 2014-2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot.internal;

import static org.csstudio.javafx.rtplot.Activator.logger;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.csstudio.javafx.rtplot.internal.util.Log10;

/** Helper for creating tick marks.
 *  <p>
 *  Computes tick positions, formats tick labels.
 *  Doesn't perform the actual drawing.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class LogTicks extends LinearTicks
{
    // 'start' is the first tick mark as in LinearTicks
    // 'distance' is used as in LinearTicks when positive.
    // A negative 'distance' is used when it refers to the exponent

    public LogTicks()
    {
        num_fmt = createExponentialFormat(2);
        detailed_num_fmt = createExponentialFormat(3);
    }

    /** {@inheritDoc} */
    @Override
    public void compute(final Double low, final Double high, final Graphics2D gc, final int screen_width)
    {
        logger.log(Level.FINE, "Compute log ticks, width {0}, for {1} - {2}",
                               new Object[] { screen_width, low, high });


        final List<MajorTick<Double>> major_ticks = new ArrayList<>();
        final List<MinorTick<Double>> minor_ticks = new ArrayList<>();

        // Only support low < high
        if (! isSupportedRange(low, high)  ||   high <= low)
            throw new Error("Unsupported range " + low + " .. " + high);

        // Determine range of values on axis
        final double low_exp = (int) Math.floor(Log10.log10(low));
        final double high_exp = (int) Math.floor(Log10.log10(high));
        final double low_power = Log10.pow10(low_exp);
        final double low_mantissa = low / low_power;

        // Test format
        num_fmt = createExponentialFormat(2);

        // Determine minimum label distance on the screen, using some
        // percentage of the available screen space.
        // Guess the label width, using the two extremes.
        final String low_label = format(low);
        final String high_label = format(high);
        final FontMetrics metrics = gc.getFontMetrics();
        final int label_width = Math.max(metrics.stringWidth(low_label), metrics.stringWidth(high_label));
        final int num_that_fits = Math.max(1,  screen_width/label_width*FILL_PERCENTAGE/100);

        int minor = 2;
        int precision;
        if (high_exp <= low_exp)
        {   // Are numbers are within the same order of magnitude,
            // i.e. same exponent xeN = 2e4, 4e4, 6e4

            System.out.println("Same order of exp: " + low_exp + " .. " + high_exp);

            // Determine distance in terms of mantissa, relative to lower end of range
            final double high_mantissa = high / low_power;
            double dist = (high_mantissa - low_mantissa) / num_that_fits;
            dist = selectNiceStep(dist);
            if (dist <= 0.0)
                throw new Error("Broken tickmark computation");

            precision = determinePrecision(low_mantissa) + 1;
            num_fmt = createExponentialFormat(precision);
            detailed_num_fmt = createExponentialFormat(precision+1);

            // Start at 'low' adjusted to a multiple of the tick distance
//            start = Math.ceil(low_mantissa / dist) * dist;
//            start = start * low_power;
//
//            distance = dist * low_power;
        }
        else
        {   // Range covers different orders of magnitude,
            // example 1eN = 1e-5, 1e0, 1e10, ..
            // Distance refers to exponent of value.
            double dist = (high_exp - low_exp) / num_that_fits;

            System.out.println("Different order of exp: " + low_exp + " .. " + high_exp);


            // Round up to the precision used to display values
            dist = selectNiceStep(dist);
            if (dist <= 0.0)
                throw new Error("Broken tickmark computation, range " + low + " .. " + high +
                                ", distance between exponents " + low_exp + " .. " + high_exp +
                                " is " + dist);

            if (dist == 1)
            {
                // Example: 1e2, 1e3, 1e4 with dist==1 between exponents
                precision = 0;
                minor = 10;
            }
            else
            {
                precision = determinePrecision(low_mantissa);
                minor = 10;
            }
            num_fmt = createExponentialFormat(precision);
            detailed_num_fmt = createExponentialFormat(precision+1);

            final double start = Log10.pow10(Math.ceil(Log10.log10(low) / dist) * dist);

            // Compute major tick marks
            final double step_factor = Log10.pow10(dist);
            double value = start;
            double prev = start / step_factor;
            while (value <= high*step_factor)
            {
                if (value >= low  &&  value <= high)
                    major_ticks.add(new MajorTick<Double>(value, num_fmt.format(value)));

                // Fill major tick marks with minor ticks
                for (int i=1; i<(minor-1); ++i)
                {
                    final double min_val = prev + ((value - prev)*i)/(minor-1);
                    if (min_val <= low || min_val >= high)
                        continue;
                    minor_ticks.add(new MinorTick<Double>(min_val));
                }
                prev = value;

                value *= step_factor;
                // Rounding errors can result in a situation where
                // we don't make any progress...
                if (value <= prev)
                    break;
            }
        }

        this.major_ticks = major_ticks;
        this.minor_ticks = minor_ticks;
    }
}
