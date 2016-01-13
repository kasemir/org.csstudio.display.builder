/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetType;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;
import org.junit.Test;

/** JUnit test of widget properties
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetPropertyUnitTest
{
    /** Check common widget properties */
    @Test
    public void testCommonWidgetProperty()
    {
        final Widget widget = new BaseWidget("generic");
        System.out.println(widget);
        widget.setPropertyValue(widgetName, "test1");
        assertThat(widget.getName(), equalTo("test1"));
        assertThat(widget.getProperty(widgetName).getValue(), equalTo("test1"));

        assertThat(widget.getProperty("name").getValue(), instanceOf(String.class));
        assertThat(widget.getProperty("x").getValue(), instanceOf(Integer.class));
        assertThat(widget.getProperty("visible").getValue(), instanceOf(Boolean.class));
    }

    /** Check property write access */
    @Test
    public void testPropertyWrite()
    {
        final Widget widget = new Widget("generic");
        final WidgetProperty<Integer> property = widget.getProperty(positionX);
        assertThat(property.getValue(), equalTo(0));
        assertThat(property.isDefaultValue(), equalTo(true));

        property.setValue(21);
        assertThat(property.getValue(), equalTo(21));
        assertThat(property.isDefaultValue(), equalTo(false));
    }

    class TestWidget extends Widget
    {
        TestWidget()
        {
            super("generic");
        }

        @Override
        protected void defineProperties(final List<WidgetProperty<?>> properties)
        {
            super.defineProperties(properties);
            properties.add(CustomWidget.miscZeroTen.createProperty(this, 5));
            try
            {
                properties.add(CustomWidget.miscZeroTen.createProperty(this, -5));
                throw new Error("Failed to detect default value -5 outside of range 0-10");
            }
            catch (final IllegalArgumentException ex)
            {
                // Expected...
                System.out.println("Detected: " + ex.getMessage());
            }
        }
    }

    /** Check property value range
     *  @throws Exception on error
     */
    @Test
    public void testPropertyValueRange() throws Exception
    {
        final Widget widget = new TestWidget();

        final WidgetProperty<Integer> property = widget.getProperty(CustomWidget.miscZeroTen);
        assertThat(property.getValue(), equalTo(5));
        assertThat(property.getDefaultValue(), equalTo(5));

        property.setValue(7);
        assertThat(property.getValue(), equalTo(7));

        property.setValue(12);
        assertThat(property.getValue(), equalTo(10));

        property.setValue(-12);
        assertThat(property.getValue(), equalTo(0));
    }

    /** Check read-only access */
    @Test
    public void testReadonly()
    {
        final Widget widget = new Widget("generic");
        final WidgetProperty<String> type = widget.getProperty(widgetType);
        final WidgetProperty<String> name = widget.getProperty(widgetName);
        assertThat(type.isReadonly(), equalTo(true));
        assertThat(name.isReadonly(), equalTo(false));

        assertThat(type.getValue(), equalTo("generic"));
        type.setValue("other type");
        assertThat(type.getValue(), equalTo("generic"));
    }

    /** Example enum with end-user labels */
    enum Align
    {
        LEFT("Left"), CENTER("Center"), RIGHT("Right");

        private final String label;

        private Align(final String label)
        {
            this.label = label;
        }

        public String toString()
        {
            return label;
        }
    };

    WidgetPropertyDescriptor<Align> alignHoriz =
            new WidgetPropertyDescriptor<Align>(WidgetPropertyCategory.DISPLAY, "horiz_align", "Horizontal alignment")
    {
        @Override
        public WidgetProperty<Align> createProperty(final Widget widget, final Align default_value)
        {
            return new EnumWidgetProperty<Align>(this, widget, default_value);
        }
    };

    /* Test enumerated property read/write API */
    @Test
    public void testEnum() throws Exception
    {
        final DisplayModel widget = new DisplayModel();
        final Macros macros = new Macros();
        macros.add("ALIGN", "1");
        widget.widgetMacros().setValue(macros);

        final EnumWidgetProperty<Align> prop = new EnumWidgetProperty<Align>(alignHoriz, widget, Align.LEFT);
        System.out.println(prop);
        assertThat(prop.getValue(), equalTo(Align.LEFT));

        System.out.println(Arrays.toString(prop.getLabels()));
        assertThat(prop.getLabels(), equalTo(new String[] { "Left", "Center", "Right" } ));

        // Set value as enum
        prop.setValue(Align.RIGHT);
        System.out.println(prop);
        assertThat(prop.getValue(), equalTo(Align.RIGHT));

        // Set value as object, using the enum
        prop.setValueFromObject(Align.LEFT);
        System.out.println(prop);
        assertThat(prop.getValue(), equalTo(Align.LEFT));

        // Set value from ordinal
        prop.setValueFromObject(2);
        System.out.println(prop);
        assertThat(prop.getValue(), equalTo(Align.RIGHT));

        // Set value from string with ordinal
        prop.setValueFromObject("1");
        System.out.println(prop);
        assertThat(prop.getValue(), equalTo(Align.CENTER));
        assertThat(prop.getSpecification(), equalTo("1"));

        // Capture invalid ordinal
        try
        {
            prop.setValueFromObject(20);
            fail("Allowed invalid ordinal");
        }
        catch (Exception ex)
        {
            assertThat(ex.getMessage().toLowerCase(), containsString("invalid ordinal"));
        }

        // Capture use of label or name instead of ordinal
        try
        {
            prop.setValueFromObject("CENTER");
            fail("Allowed name instead of ordinal");
        }
        catch (Exception ex)
        {
            assertThat(ex.getMessage().toLowerCase(), containsString("expects ordinal"));
        }

        // Check handling of specification and macros
        prop.setSpecification("0");
        System.out.println(prop);
        assertThat(prop.getValue(), equalTo(Align.LEFT));

        prop.setSpecification("$(ALIGN)");
        System.out.println(prop);
        assertThat(prop.getValue(), equalTo(Align.CENTER));
        System.out.println(prop);
        assertThat(prop.getSpecification(), equalTo("$(ALIGN)"));
    }
}
