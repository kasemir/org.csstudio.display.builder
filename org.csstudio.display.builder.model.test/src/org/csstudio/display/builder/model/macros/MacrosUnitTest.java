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
    /** Test check for unresolved macros
     *  @throws Exception on error
     */
    @Test
    public void testCheck() throws Exception
    {
        assertThat(MacroHandler.containsMacros("Plain Text"), equalTo(false));
        assertThat(MacroHandler.containsMacros("${S}"), equalTo(true));
        assertThat(MacroHandler.containsMacros("This is $(S)"), equalTo(true));
        assertThat(MacroHandler.containsMacros("$(MACRO)"), equalTo(true));
        assertThat(MacroHandler.containsMacros("$(${MACRO})"), equalTo(true));
        assertThat(MacroHandler.containsMacros("Escaped \\$(S)"), equalTo(false));
        assertThat(MacroHandler.containsMacros("Escaped \\$(S) Used $(S)"), equalTo(true));
    }


    /** Test basic macro=value
     *  @throws Exception on error
     */
    @Test
    public void testParser() throws Exception
    {
        final String definition = "S = BL7, NAME=\"Flint, Eugene\", EQUAL=\"=\", TAB = \"    \", MACRO=S, QUOTED=\"Al \\\"Fred\\\" King\"";
        System.out.println(definition);
        final MacroValueProvider macros = MacroParser.parseDefinition(definition);
        System.out.println(macros);

        // Spaces around '=' and one-word value are trimmed
        assertThat(macros.getValue("S"), equalTo("BL7"));

        // Text in quotes can contain comma
        assertThat(macros.getValue("NAME"), equalTo("Flint, Eugene"));

        // Text in quotes can contain '='
        assertThat(macros.getValue("EQUAL"), equalTo("="));

        // Spaces within quotes are preserved
        assertThat(macros.getValue("TAB"), equalTo("    "));

        // Text can contain quotes as long as they are escaped.
        // Value then has the plain quotes.
        assertThat(macros.getValue("QUOTED"), equalTo("Al \"Fred\" King"));
    }

    @Test
    public void testSerialization() throws Exception
    {
        final Macros macros = new Macros();
        macros.add("S", "BL7");
        macros.add("TAB", "    ");
        macros.add("COMMA", "Flint, Fred");
        macros.add("QUOTED", "Al \"Fred\" King");
        final String text = MacroParser.serialize(macros);
        System.out.println(text);

        // Macros are serialized in alphabetical order.
        // Values are _always_ quoted, even if "BL7" could remain unquoted.
        assertThat(text, equalTo("COMMA=\"Flint, Fred\", QUOTED=\"Al \\\"Fred\\\" King\", S=\"BL7\", TAB=\"    \""));

        final Macros copy = MacroParser.parseDefinition(text);
        assertThat(copy, equalTo(macros));
    }

    /** Test basic macro=value
     *  @throws Exception on error
     */
    @Test
    public void testMacros() throws Exception
    {
        final MacroValueProvider macros = MacroParser.parseDefinition("S=BL7, NAME=\"Flint, Eugene\", TAB = \"    \", MACRO=S");

        assertThat(MacroHandler.replace(macros, "Plain Text"), equalTo("Plain Text"));
        assertThat(MacroHandler.replace(macros, "${S}"), equalTo("BL7"));
        assertThat(MacroHandler.replace(macros, "This is $(S)"), equalTo("This is BL7"));
        assertThat(MacroHandler.replace(macros, "$(MACRO)"), equalTo("S"));
        assertThat(MacroHandler.replace(macros, "$(${MACRO})"), equalTo("BL7"));
        assertThat(MacroHandler.replace(macros, "$(TAB)$(NAME)$(TAB)"), equalTo("    Flint, Eugene    "));

        assertThat(MacroHandler.replace(macros, "Escaped \\$(S)"), equalTo("Escaped \\$(S)"));
        assertThat(MacroHandler.replace(macros, "Escaped \\$(S) Used $(S)"), equalTo("Escaped \\$(S) Used BL7"));
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
        MacroValueProvider macros = MacroParser.parseDefinition("");
        System.out.println(macros);
        assertThat(macros.toString(), equalTo("[]"));

        assertThat(MacroHandler.replace(macros, "Plain Text"), equalTo("Plain Text"));
        assertThat(MacroHandler.replace(macros, "Nothing for ${S} <-- this one"), equalTo("Nothing for ${S} <-- this one"));
        assertThat(MacroHandler.replace(macros, "${NOT_CLOSED"), equalTo("${NOT_CLOSED"));
    }
}
