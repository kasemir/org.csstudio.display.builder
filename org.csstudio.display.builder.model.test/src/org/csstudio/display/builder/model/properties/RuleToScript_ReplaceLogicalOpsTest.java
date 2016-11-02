/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Test replacement of logical operators in RuleToScript.
 *
 * @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class RuleToScript_ReplaceLogicalOpsTest
{
    public void testAnd()
    {
        String replacement = RuleToScript.replaceLogicalOperators("x && y");
        assertThat(replacement, containsString("and"));

        replacement = RuleToScript.replaceLogicalOperators("a < 1 && y != \"a && b\"");
        assertThat(replacement, containsString("and"));
        assertThat(replacement, containsString("\"a && b\""));

        replacement = RuleToScript.replaceLogicalOperators("y != \"a && b\" && a < 1");
        assertThat(replacement, containsString("and"));
        assertThat(replacement, containsString("\"a && b\""));
    }

    public void testOr()
    {
        String replacement = RuleToScript.replaceLogicalOperators("x || y");
        assertThat(replacement, containsString("or"));

        replacement = RuleToScript.replaceLogicalOperators("a < 1 || y != \"a || b\"");
        assertThat(replacement, containsString("or"));
        assertThat(replacement, containsString("\"a || b\""));

        replacement = RuleToScript.replaceLogicalOperators("y != \"a || b\" || a < 1");
        assertThat(replacement, containsString("or"));
        assertThat(replacement, containsString("\"a || b\""));
    }

    public void testNot()
    {
        String replacement = RuleToScript.replaceLogicalOperators("x != y");
        assertThat(replacement, not(containsString("not")));

        replacement = RuleToScript.replaceLogicalOperators("!(x+y > 1)");
        assertThat(replacement, containsString("not (x+y"));

        replacement = RuleToScript.replaceLogicalOperators("!(a==b) && x != \"!\"");
        assertThat(replacement, containsString("not (a==b)"));
        assertThat(replacement, containsString("\"!\""));
        assertThat(replacement, containsString("!="));
    }

    @Test
    public void testAll()
    {
        String expression = "a == \"a\" || x != \"\\\"\" && y == \"b!\"";
        //a == "a" && a == "b!" || a != "\""
        String replacement = RuleToScript.replaceLogicalOperators(expression);
        System.out.println("Repl: " + expression + "\nWith: " + replacement);
        assertThat(replacement, containsString("and"));
        assertThat(replacement, containsString("or"));
        assertThat(replacement, not(containsString("not")));

        assertThat(replacement, containsString("a =="));
        assertThat(replacement, containsString("x !="));
        assertThat(replacement, containsString("y =="));

        assertThat(replacement, containsString("\"\\\"\""));
        assertThat(replacement, containsString("\"b!\""));
    }
}
