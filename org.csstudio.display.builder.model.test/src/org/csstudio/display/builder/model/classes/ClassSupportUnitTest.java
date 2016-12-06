/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.classes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;
import java.io.InputStream;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.junit.Test;

/** JUnit test of {@link WidgetClassSupport}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ClassSupportUnitTest
{
    @Test
    public void testUsingWidgetClass()
    {
        // Default behavior
        final Widget widget = new LabelWidget();
        assertThat(widget.getProperty("text").isUsingWidgetClass(), equalTo(false));
        assertThat(widget.getProperty("font").isUsingWidgetClass(), equalTo(true));

        // Can be changed
        widget.getProperty("text").useWidgetClass(true);
        assertThat(widget.getProperty("text").isUsingWidgetClass(), equalTo(true));
    }

    @Test
    public void testClassfile() throws Exception
    {
        final InputStream stream = new FileInputStream("../org.csstudio.display.builder.model/examples/classes.btf");
        final WidgetClassSupport widget_classes = new WidgetClassSupport(stream);
        System.out.println(widget_classes);

        // Every widget has a DEFAULT class
        assertThat(widget_classes.getWidgetClasses("textupdate"), hasItem("DEFAULT"));

        // Label has more classes defined in classes.btf
        assertThat(widget_classes.getWidgetClasses("label"), hasItem("DEFAULT"));
        assertThat(widget_classes.getWidgetClasses("label"), hasItem("TITLE"));
        assertThat(widget_classes.getWidgetClasses("label"), hasItem("COMMENT"));
    }

    // TODO:
    // final Widget widget = new LabelWidget();
    // widget.setPropertyValue("class", "TITLE")
    // widget_classes.update(widget);
    // -> widget now using a different font
    //
    // widget.setPropertyValue("class", "COMMENT")
    // widget_classes.update(widget);
    // -> widget now using a different font
    //
    // widget.setPropertyValue("class", "DEFAULT")
    // widget_classes.update(widget);
    // -> widget now using a different font
    //
    // widget.getProperty("font").useWidgetClass(false);
    // widget.setPropertyValue("class", "TITLE")
    // widget_classes.update(widget);
    // -> widget NOT using a different font
}
