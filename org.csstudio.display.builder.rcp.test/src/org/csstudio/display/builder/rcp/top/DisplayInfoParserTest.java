/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.top;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

/** Information about a display
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DisplayInfoParserTest
{
    @Test
    public void testQuoted() throws Exception
    {
        final List<DisplayInfo> displays = DisplayInfoParser.parse("\"Instruments\" = \"https://webopi.sns.gov/webopi/opi/Instruments.opi\"");
        assertThat(displays.size(), equalTo(1));
        assertThat(displays.get(0).getName(), equalTo("Instruments"));
        assertThat(displays.get(0).getPath(), equalTo("https://webopi.sns.gov/webopi/opi/Instruments.opi"));
    }

    @Test
    public void testPlain() throws Exception
    {
        final List<DisplayInfo> displays = DisplayInfoParser.parse("Instruments = https://webopi.sns.gov/webopi/opi/Instruments.opi");
        assertThat(displays.size(), equalTo(1));
        assertThat(displays.get(0).getName(), equalTo("Instruments"));
        assertThat(displays.get(0).getPath(), equalTo("https://webopi.sns.gov/webopi/opi/Instruments.opi"));
    }

    @Test
    public void testTwo() throws Exception
    {
        final List<DisplayInfo> displays = DisplayInfoParser.parse("Main=/some/path/main.opi|https://webopi.sns.gov/webopi/opi/Instruments.opi");
        assertThat(displays.size(), equalTo(2));
        assertThat(displays.get(0).getName(), equalTo("Main"));
        assertThat(displays.get(0).getPath(), equalTo("/some/path/main.opi"));
        assertThat(displays.get(1).getName(), equalTo("Instruments"));
    }
}
