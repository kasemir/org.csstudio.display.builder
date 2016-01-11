/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
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
    public void testMacros() throws Exception
    {
        final Macros macros = new Macros();
        macros.add("S", "BL7");
        macros.add("NAME", "Flint, Eugene");
        macros.add("TAB", "    ");
        macros.add("MACRO", "S");

        assertThat(MacroHandler.replace(macros, "Plain Text"), equalTo("Plain Text"));
        assertThat(MacroHandler.replace(macros, "${S}"), equalTo("BL7"));
        assertThat(MacroHandler.replace(macros, "This is $(S)"), equalTo("This is BL7"));
        assertThat(MacroHandler.replace(macros, "$(MACRO)"), equalTo("S"));
        assertThat(MacroHandler.replace(macros, "$(${MACRO})"), equalTo("BL7"));
        assertThat(MacroHandler.replace(macros, "$(TAB)$(NAME)$(TAB)"), equalTo("    Flint, Eugene    "));

        assertThat(MacroHandler.replace(macros, "Escaped \\$(S)"), equalTo("Escaped \\$(S)"));
        assertThat(MacroHandler.replace(macros, "Escaped \\$(S) Used $(S)"), equalTo("Escaped \\$(S) Used BL7"));
    }

    /** Test special cases
     *  @throws Exception on error
     */
    @Test
    public void testSpecials() throws Exception
    {
        MacroValueProvider macros = new Macros();
        System.out.println(macros);
        assertThat(macros.toString(), equalTo("[]"));

        assertThat(MacroHandler.replace(macros, "Plain Text"), equalTo("Plain Text"));
        assertThat(MacroHandler.replace(macros, "Nothing for ${S} <-- this one"), equalTo("Nothing for ${S} <-- this one"));
        assertThat(MacroHandler.replace(macros, "${NOT_CLOSED"), equalTo("${NOT_CLOSED"));
    }

    /** Test recursive macro error
     *  @throws Exception on error
     */
    @Test
    public void testReursion() throws Exception
    {
        final Macros macros = new Macros();
        macros.add("S", "$(S)");
        try
        {
            MacroHandler.replace(macros, "Never ending $(S)");
            fail("Didn't detect recursive macro");
        }
        catch (Exception ex)
        {
            assertThat(ex.getMessage(), containsString(/* [Rr] */ "ecursive"));
        }
    }
}
