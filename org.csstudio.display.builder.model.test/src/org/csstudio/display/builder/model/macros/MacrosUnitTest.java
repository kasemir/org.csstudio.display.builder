/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.macros;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

/** JUnit test of macro handling
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class MacrosUnitTest
{
    /** Test basic macro=value
     *  @throws Exception on error
     */
    @Test
    public void testMacros() throws Exception
    {
        final Macros macros = MacroParser.parseDefinition("S=BL7, NAME=\"Flint, Eugene\", TAB = \"    \", MACRO=S");
        System.out.println(macros);
        assertThat(macros.getValue("S"),    equalTo("BL7"));
        assertThat(macros.getValue("NAME"), equalTo("Flint, Eugene"));
        assertThat(macros.getValue("TAB"),  equalTo("    "));

        assertThat(MacroHandler.replace(macros, "Plain Text"), equalTo("Plain Text"));
        assertThat(MacroHandler.replace(macros, "${S}"), equalTo("BL7"));
        assertThat(MacroHandler.replace(macros, "This is $(S)"), equalTo("This is BL7"));
        assertThat(MacroHandler.replace(macros, "$(MACRO)"), equalTo("S"));
        assertThat(MacroHandler.replace(macros, "$(${MACRO})"), equalTo("BL7"));
        assertThat(MacroHandler.replace(macros, "$(TAB)$(NAME)$(TAB)"), equalTo("    Flint, Eugene    "));
    }

    /** Test errors*/
    @Test
    public void testErrors()
    {
        try
        {
            MacroParser.parseDefinition("S  value");
            fail("Missing '='");
        }
        catch (Exception ex)
        {
            assertThat(ex.getMessage(), containsString("="));
        }

        try
        {
            MacroParser.parseDefinition("S=\"");
            fail("Open quotes");
        }
        catch (Exception ex)
        {
            assertThat(ex.getMessage(), containsString("quote"));
        }
    }

    /** Test special cases
     *  @throws Exception on error
     */
    @Test
    public void testSpecials() throws Exception
    {
        Macros macros = MacroParser.parseDefinition("");
        System.out.println(macros);
        assertThat(macros.toString(), equalTo("[]"));

        assertThat(MacroHandler.replace(macros, "Plain Text"), equalTo("Plain Text"));
        assertThat(MacroHandler.replace(macros, "Nothing for ${S} <-- this one"), equalTo("Nothing for ${S} <-- this one"));
        assertThat(MacroHandler.replace(macros, "${NOT_CLOSED"), equalTo("${NOT_CLOSED"));
    }
}
