/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.csstudio.display.builder.model.macros.Macros;
import org.junit.Test;

/** Unit test for DisplayInfo
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DisplayInfoParserTest
{
    @Test
    public void testSerialization() throws Exception
    {
        final Macros macros = new Macros();

    	DisplayInfo display = new DisplayInfo("/a/path/file.opi", "Alias", macros);
    	String serialized = DisplayInfoXMLUtil.toXML(display);
    	System.out.println(serialized);
    	assertThat(serialized, not(containsString("<macros>")));

    	DisplayInfo copy = DisplayInfoXMLUtil.fromXML(serialized);
    	System.out.println(copy);
    	assertThat(copy, equalTo(display));


    	// Current implementation of DisplayInfo keeps reference to macros,
    	// so changing macros will result in updated DisplayInfo.
    	// This test does not depend on it and always creates a new DisplayInfo.
    	macros.add("S", "Test");
    	display = new DisplayInfo("/a/path/file.opi", "Alias", macros);
    	serialized = DisplayInfoXMLUtil.toXML(display);
        System.out.println(serialized);
        assertThat(serialized, containsString("<macros>"));

        copy = DisplayInfoXMLUtil.fromXML(serialized);
        System.out.println(copy);
        assertThat(copy, equalTo(display));
    }
}
