/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.csstudio.display.builder.runtime.script.Script;
import org.csstudio.display.builder.runtime.script.ScriptSupport;
import org.junit.Test;

/** JUnit test of script support
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class JythonScriptTest
{
    @Test
    public void testJythonScript() throws Exception
    {
        final DisplayModel display = RuntimeUtil.loadModel("../org.csstudio.display.builder.runtime.test/examples/dummy.opi", "script_test.opi");

        final Widget widget = display.getChildren().parallelStream().filter(w -> w.getName().equals("Label 100")).findFirst().get();

        System.out.println(widget);

        // Set widget variable in script
        final ScriptSupport scripting = new ScriptSupport();
        final Script script = scripting.compile("../org.csstudio.display.builder.runtime.test/examples/updateText.py");

        for (int run=0; run<10; ++run)
        {
            widget.setPropertyValue("text", "Initial");
            String text = widget.getPropertyValue("text");
            assertThat(text, equalTo("Initial"));
            script.submit(widget).get();
            assertThat(widget.getPropertyValue("text"), equalTo("Hello"));

            widget.setPropertyValue("text", "Initial");
            assertThat(widget.getPropertyValue("text"), equalTo("Initial"));
            script.submit(widget).get();
            assertThat(widget.getPropertyValue("text"), equalTo("Hello"));
        }

        scripting.close();
    }
}
