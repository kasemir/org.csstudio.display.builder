/*******************************************************************************
 * Copyright (c) 2014-2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.csstudio.javafx.rtplot.internal.LinearTicks;
import org.junit.Test;

/** JUnit test
 *  @author Kay Kasemir
 */
public class LinearTicksTest extends TicksTestBase
{
    @Override
    @Test
    public void testPrecision()
    {
        assertThat(LinearTicks.determinePrecision(100.0), equalTo(0));
        assertThat(LinearTicks.determinePrecision(5.0), equalTo(1));
        assertThat(LinearTicks.determinePrecision(0.5), equalTo(2));
        assertThat(LinearTicks.determinePrecision(2e-6), equalTo(7));
    }

    @Override
    @Test
    public void testNiceDistance()
    {
        for (double order_of_magnitude : new double[] { 1.0, 0.0001, 1000.0, 1e12, 1e-7 })
        {
            assertThat(LinearTicks.selectNiceStep(10.0*order_of_magnitude), equalTo(10.0*order_of_magnitude));
            assertThat(LinearTicks.selectNiceStep(9.0*order_of_magnitude), equalTo(10.0*order_of_magnitude));
            assertThat(LinearTicks.selectNiceStep(7.0*order_of_magnitude), equalTo(10.0*order_of_magnitude));
            assertThat(LinearTicks.selectNiceStep(5.0*order_of_magnitude), equalTo(5.0*order_of_magnitude));
            assertThat(LinearTicks.selectNiceStep(4.0*order_of_magnitude), equalTo(5.0*order_of_magnitude));
            assertThat(LinearTicks.selectNiceStep(3.0*order_of_magnitude), equalTo(5.0*order_of_magnitude));
            assertThat(LinearTicks.selectNiceStep(2.01*order_of_magnitude), equalTo(2.0*order_of_magnitude));
            assertThat(LinearTicks.selectNiceStep(1.5*order_of_magnitude), equalTo(2.0*order_of_magnitude));
        }
    }

    @Test
    public void testNormalTicks()
    {
        final LinearTicks ticks = new LinearTicks();
        double start = 1.0,  end = 100.0;
        ticks.compute(start, end, gc, buf.getWidth());

        System.out.println("Ticks for " + start + " .. " + end + ":");
        final String text = ticks2text(ticks.getMajorTicks(), ticks.getMinorTicks());
        System.out.println(text);

        assertThat(text, equalTo("2.0 4.0 6.0 8.0 '10' 12.0 14.0 16.0 18.0 '20' 22.0 24.0 26.0 28.0 '30' 32.0 34.0 36.0 38.0 '40' 42.0 44.0 46.0 48.0 '50' 52.0 54.0 56.0 58.0 '60' 62.0 64.0 66.0 68.0 '70' 72.0 74.0 76.0 78.0 '80' 82.0 84.0 86.0 88.0 '90' 92.0 94.0 96.0 98.0 "));
    }

    @Test
    public void testReverseTicks()
    {
        final LinearTicks ticks = new LinearTicks();
        double start = 10000.0,  end = 1.0;
        ticks.compute(start, end, gc, buf.getWidth());

        System.out.println("Ticks for " + start + " .. " + end + ":");
        final String text = ticks2text(ticks.getMajorTicks(), ticks.getMinorTicks());
        System.out.println(text);

        assertThat(text, equalTo("'10000' 9600.0 9200.0 8800.0 8400.0 '8000' 7600.0 7200.0 6800.0 6400.0 '6000' 5600.0 5200.0 4800.0 4400.0 '4000' 3600.0 3200.0 2800.0 2400.0 '2000' 1600.0 1200.0 800.0 400.0 "));
    }
}
