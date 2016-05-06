/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.csstudio.display.builder.runtime.pv.NamePatch;
import org.junit.Test;

/** JUnit demo of the {@link NamePatch}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class NamePatchTest
{
    @Test
    public void testLongString() throws Exception
    {
        final NamePatch pvm_string = new NamePatch(" \\{\"longString\":true\\}", "");

        String name = "fred {\"longString\":true}";
        String patched = pvm_string.patch(name);
        System.out.println(name + " -> " + patched);
        assertThat(patched, equalTo("fred"));
    }

    @Test
    public void testConstantNumber() throws Exception
    {
        final NamePatch pvm_string = new NamePatch("=([0-9]+)", "loc://const$1($1)");

        String name = "=1";
        String patched = pvm_string.patch(name);
        System.out.println(name + " -> " + patched);
        assertThat(patched, equalTo("loc://const1(1)"));

        name = "=42";
        patched = pvm_string.patch(name);
        System.out.println(name + " -> " + patched);
        assertThat(patched, equalTo("loc://const42(42)"));
    }
}
