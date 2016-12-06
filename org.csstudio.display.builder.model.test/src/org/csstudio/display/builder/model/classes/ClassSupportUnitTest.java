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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;
import java.io.InputStream;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.properties.NamedWidgetFont;
import org.csstudio.display.builder.model.properties.WidgetFont;
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

    @Test
    public void testPropertyUpdates() throws Exception
    {
        final InputStream stream = new FileInputStream("../org.csstudio.display.builder.model/examples/classes.btf");
        final WidgetClassSupport widget_classes = new WidgetClassSupport(stream);

        final LabelWidget widget = new LabelWidget();
        assertThat(widget.getWidgetClass(), equalTo(WidgetClassSupport.DEFAULT));
        assertThat(widget.propFont().isUsingWidgetClass(), equalTo(true));

        // Original, default font of Label
        WidgetFont value = widget.propFont().getValue();
        assertThat(value, instanceOf(NamedWidgetFont.class));
        final NamedWidgetFont orig_font = (NamedWidgetFont) value;
        System.out.println("Original font: " + orig_font);

        // TITLE class -> widget now using a different font
        widget.setPropertyValue("class", "TITLE");
        widget_classes.apply(widget);
        value = widget.propFont().getValue();
        System.out.println("TITLE class font: " + value);
        assertThat(value, instanceOf(NamedWidgetFont.class));
        final NamedWidgetFont title_font = (NamedWidgetFont) value;
        assertThat(title_font.getName(), not(equalTo(orig_font.getName())));

        // COMMENT class -> widget now using a different font
        widget.setPropertyValue("class", "COMMENT");
        widget_classes.apply(widget);
        value = widget.propFont().getValue();
        System.out.println("COMMENT class font: " + value);
        assertThat(value, instanceOf(NamedWidgetFont.class));
        final NamedWidgetFont comment_font = (NamedWidgetFont) value;
        assertThat(comment_font.getName(), not(equalTo(orig_font.getName())));
        assertThat(comment_font.getName(), not(equalTo(title_font.getName())));

        // DEFAULT class -> widget back to original
        widget.setPropertyValue("class", "DEFAULT");
        widget_classes.apply(widget);
        value = widget.propFont().getValue();
        System.out.println("DEFAULT class font: " + value);
        assertThat(value, instanceOf(NamedWidgetFont.class));
        final NamedWidgetFont default_font = (NamedWidgetFont) value;
        assertThat(default_font, equalTo(orig_font));

        // Configure font to ignore the widget class,
        // instead set some specific font
        // -> class now ignored, font stays as set
        widget.propFont().useWidgetClass(false);
        widget.propFont().setValue(NamedWidgetFonts.DEFAULT_BOLD);
        widget.setPropertyValue("class", "TITLE");
        widget_classes.apply(widget);
        value = widget.propFont().getValue();
        System.out.println("TITLE class font when not using class: " + value);
        assertThat(value, equalTo(NamedWidgetFonts.DEFAULT_BOLD));
    }

    // TODO: WidgetClassService similar to WidgetColorService that loads the class info once, in background
}
