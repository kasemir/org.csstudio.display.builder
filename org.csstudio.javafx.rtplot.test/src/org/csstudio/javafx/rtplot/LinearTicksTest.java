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
        double start = 1.0,  end = 10000.0;
        ticks.compute(start, end, gc, buf.getWidth());

        System.out.println("Ticks for " + start + " .. " + end + ":");
        final String text = ticks2text(ticks.getMajorTicks(), ticks.getMinorTicks());
        System.out.println(text);

        assertThat(text, equalTo("500.0 1000.0 1500.0 '2000' 2500.0 3000.0 3500.0 '4000' 4500.0 5000.0 5500.0 '6000' 6500.0 7000.0 7500.0 '8000' "));
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

        assertThat(text, equalTo("'10000' 9500.0 9000.0 8500.0 '8000' 7500.0 7000.0 6500.0 '6000' 5500.0 5000.0 4500.0 '4000' 3500.0 3000.0 2500.0 '2000' "));
    }

}
