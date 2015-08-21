/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.persist;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.properties.NamedWidgetColor;
import org.junit.Test;

/** JUnit test of color handling
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ColorUnitTest
{
    /** Test fetching named colors
     *  @throws Exception on error
     */
    @Test
    public void testNamedColors() throws Exception
    {
        final NamedWidgetColors colors = new NamedWidgetColors();

        NamedWidgetColor color = colors.getColor("MAJOR").orElse(null);
        System.out.println(color);
        assertThat(color, not(nullValue()));
        assertThat(color.getRed(), equalTo(255));

        color = colors.getColor("Attention").orElse(null);
        assertThat(color, nullValue());

        colors.read(new FileInputStream("../org.csstudio.display.builder.runtime.test/examples/color.def"));

        color = colors.getColor("Attention").orElse(null);
        System.out.println(color);
        assertThat(color, not(nullValue()));
   }

    /** Test fetching named colors from service
     *  @throws Exception on error
     */
    @Test
    public void testColorService() throws Exception
    {
        System.out.println("On " + Thread.currentThread().getName());
        final Logger logger = Logger.getLogger(getClass().getName());

        // Default colors do now include 'Attention'
        NamedWidgetColors colors = WidgetColorService.getColors();
        NamedWidgetColor color = colors.getColor("Attention").orElse(null);
        assertThat(color, nullValue());

        // Load colors, using a source with artificial delay
        final Callable<InputStream> slow_color_source = () ->
        {
            logger.warning("Delaying file access.. on " + Thread.currentThread().getName());
            TimeUnit.SECONDS.sleep(2 * WidgetColorService.LOAD_DELAY);
            logger.warning("Finally opening the file");
            return new FileInputStream("../org.csstudio.display.builder.runtime.test/examples/color.def");
        };
        WidgetColorService.loadColors("Slow file", slow_color_source);

        // Getting the colors is now delayed by the WidgetColorService.LOAD_DELAY
        long start = System.currentTimeMillis();
        colors = WidgetColorService.getColors();
        double seconds = (System.currentTimeMillis() - start) / 1000.0;
        System.out.println("Loading the slow color file took " + seconds + " seconds");

        // Should get default names, because slow_color_source is not ready, yet
        color = colors.getColor("Attention").orElse(null);
        System.out.println("'Attention' should be null: " + color);
        assertThat(color, nullValue());

        // The file is still loading, and eventually we should get it
        TimeUnit.SECONDS.sleep(2 * WidgetColorService.LOAD_DELAY);
        start = System.currentTimeMillis();
        colors = WidgetColorService.getColors();
        seconds = (System.currentTimeMillis() - start) / 1000.0;
        System.out.println("Fetching colors after the file got loaded took " + seconds + " seconds");

        color = colors.getColor("Attention").orElse(null);
        System.out.println(color);
        assertThat(color, not(nullValue()));
    }
}
