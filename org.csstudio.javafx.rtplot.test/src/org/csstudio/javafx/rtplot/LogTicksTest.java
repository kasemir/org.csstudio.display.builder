/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

//
//import org.csstudio.javafx.rtplot.internal.LinearTicks;
import org.csstudio.javafx.rtplot.internal.LogTicks;
import org.junit.Test;

/** JUnit test
 *  @author Kay Kasemir
 */
public class LogTicksTest extends TicksTestBase
{
    @Test
    public void testLogTicks()
    {
        final LogTicks ticks = new LogTicks();
        double start = 1.0,  end = 10000.0;
        ticks.compute(start, end, gc, buf.getWidth());

        System.out.println("Ticks for " + start + " .. " + end + ":");
        final String text = ticks2text(ticks.getMajorTicks(), ticks.getMinorTicks());
        System.out.println(text);

        assertThat(text, equalTo("'1E0' 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 '1E1' 20.0 30.0 40.0 50.0 60.0 70.0 80.0 90.0 '1E2' 200.0 300.0 400.0 500.0 600.0 700.0 800.0 900.0 '1E3' 2000.0 3000.0 4000.0 5000.0 6000.0 7000.0 8000.0 9000.0 '1E4' "));
    }
}
