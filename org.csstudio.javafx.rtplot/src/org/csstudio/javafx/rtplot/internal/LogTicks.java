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

        // Only support 'normal' order, low < high
        if (! isSupportedRange(low, high)  ||   high <= low)
            throw new Error("Unsupported range " + low + " .. " + high);

        // Determine range of values on axis
        final double low_exp = (int) Math.floor(Log10.log10(low));
        final double high_exp = (int) Math.floor(Log10.log10(high));
        final double low_power = Log10.pow10(low_exp);
        final double low_mantissa = low / low_power;

        // Test format
        int precision = 2;
        num_fmt = createExponentialFormat(precision);

        // Determine minimum label distance on the screen, using some
        // percentage of the available screen space.
        // Guess the label width, using the two extremes.
        final String low_label = format(low);
        final String high_label = format(high);
        final FontMetrics metrics = gc.getFontMetrics();
        final int label_width = Math.max(metrics.stringWidth(low_label), metrics.stringWidth(high_label));
        final int num_that_fits = Math.max(1, screen_width*FILL_PERCENTAGE/label_width/100);
        final List<MajorTick<Double>> major_ticks = new ArrayList<>();
        final List<MinorTick<Double>> minor_ticks = new ArrayList<>();

        // Cases:
        // "Normal" log axis with 10 minor ticks
        // 1e0   .  ..1e1  .   ..1e2  .  ..1e3

        // Log axis with some same exponent, no minor ticks
        // 1.0E0     3.2E0    1.0E1    3.2E1

        // Log axis where complete range is within same exponent
        // 1.0E6  .  1.2E6  .   1.4E6  .  1.8E6

        if (low_exp >= high_exp)
        {
            // Complete range is within same exponent
            System.out.println("\nOne range");

            // Determine distance in terms of mantissa
            double dist = (high - low) / num_that_fits;
            dist = selectNiceStep(dist);
            if (dist <= 0.0)
                throw new Error("Broken tickmark computation");

            precision = determinePrecision(low_mantissa) + 1;
            num_fmt = createExponentialFormat(precision);
            detailed_num_fmt = createExponentialFormat(precision+1);

            // Start at 'low' adjusted to a multiple of the tick distance
            double start = Math.ceil(low_mantissa / dist) * dist;
            start = start * low_power;

            double prev = start - dist;
            for (double value = start;  value <= high+dist;  value += dist)
            {
                if (value >= low  &&  value <= high)
                    major_ticks.add(new MajorTick<Double>(value, num_fmt.format(value)));
                final double min_val = (prev + value) / 2.0;
                if (min_val >= low  &&  min_val <= high)
                    minor_ticks.add(new MinorTick<Double>(min_val));
                prev = value;
            }
        }
        else
        {   // Range covers different orders of magnitude,
            // example 1eN = 1e-5, 1e0, 1e10, ..
            System.out.println("Different order of exp: " + low_exp + " .. " + high_exp);

            // Try major tick distance between __exponents__
            double exp_dist = (high_exp - low_exp) / num_that_fits;
            System.out.println("Exp dist: " + exp_dist + " for range "+ num_fmt.format(low) + " .. " + num_fmt.format(high));

            // Round up to a 'nice' step size
            exp_dist = selectNiceStep(exp_dist);
            System.out.println("Nice: " + exp_dist);



            if (exp_dist <= 0.0)
                throw new Error("Broken tickmark computation, range " + low + " .. " + high +
                                ", distance between exponents " + low_exp + " .. " + high_exp +
                                " is " + exp_dist);
            int minor_count = 10;
            if (exp_dist < 1.0)
                minor_count = 0;
            if (exp_dist == 1)
                // Example: 1e2, 1e3, 1e4 with dist==1 between exponents
                precision = 0;
            else
                precision = determinePrecision(low_mantissa);
            num_fmt = createExponentialFormat(precision);
            detailed_num_fmt = createExponentialFormat(precision+1);

            final double start = Log10.pow10(Math.ceil(Log10.log10(low) / exp_dist) * exp_dist);

            // Compute major tick marks
            final double major_factor = Log10.pow10(exp_dist);
            double value = start;
            double prev = start / major_factor;
            while (value <= high*major_factor)
            {
                if (value >= low  &&  value <= high)
                    major_ticks.add(new MajorTick<Double>(value, num_fmt.format(value)));

                // Fill major tick marks with minor ticks
                // Minor ticks use 1/N of the _linear range.
                // Example:
                // Major ticks 0,   10: Minors at  1,  2,  3,  4, ..,  9
                // Major ticks 0,  100: Minors at 10, 20, 30, 40, .., 90
                final double minor_step = value  / minor_count;
                for (int i=1; i<minor_count; ++i)
                {
                    final double min_val = prev + i * minor_step;
                    if (min_val <= low || min_val >= high)
                        continue;
                    minor_ticks.add(new MinorTick<Double>(min_val));
                }
                prev = value;

                value *= major_factor;
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
