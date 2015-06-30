/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
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
        final Widget widget = new Widget("generic", "test1");
        System.out.println(widget);
        assertThat(widget.getName(), equalTo("test1"));
        assertThat(widget.getProperty(CommonWidgetProperties.widgetName).getValue(), equalTo("test1"));

        assertThat(widget.getProperty("name").getValue(), instanceOf(String.class));
        assertThat(widget.getProperty("x").getValue(), instanceOf(Integer.class));
        assertThat(widget.getProperty("visible").getValue(), instanceOf(Boolean.class));
    }

    /** Check property write access */
    @Test
    public void testPropertyWrite()
    {
        final Widget widget = new Widget("generic", "test2");
        final WidgetProperty<Integer> property = widget.getProperty(CommonWidgetProperties.positionX);
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
            super("generic", "test3");
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
        final Widget widget = new Widget("generic", "test4");
        final WidgetProperty<String> type = widget.getProperty(CommonWidgetProperties.widgetType);
        final WidgetProperty<String> name = widget.getProperty(CommonWidgetProperties.widgetName);
        assertThat(type.isReadonly(), equalTo(true));
        assertThat(name.isReadonly(), equalTo(false));

        assertThat(type.getValue(), equalTo("generic"));
        type.setValue("other type");
        assertThat(type.getValue(), equalTo("generic"));
    }
}
