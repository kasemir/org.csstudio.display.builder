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
}
