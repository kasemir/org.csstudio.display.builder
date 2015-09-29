/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.ArrayWidgetProperty.Descriptor;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.junit.Test;

/** JUnit test of array widget property
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ArrayWidgetPropertyUnitTest
{
    /** Element for array property */
    private static final WidgetPropertyDescriptor<String> behaviorItem =
            CommonWidgetProperties.newStringPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "item", "Item");

    /** Array property */
    private static final  ArrayWidgetProperty.Descriptor<String> behaviorItems =
            new Descriptor<>(WidgetPropertyCategory.BEHAVIOR, "items", "Items");

    /** Widget that has a array property */
    private static class DemoWidget extends Widget
    {
        public static final WidgetDescriptor WIDGET_DESCRIPTOR
            = new WidgetDescriptor("demo", WidgetCategory.GRAPHIC, "Demo", "no-icon.png", "Demo widget")
            {
                @Override
                public Widget createWidget()
                {
                    return new DemoWidget();
                }
            };

        private ArrayWidgetProperty<String> items;

        public DemoWidget()
        {
            super("demo");
        }

        @Override
        protected void defineProperties(List<WidgetProperty<?>> properties)
        {
            super.defineProperties(properties);
            properties.add( items = behaviorItems.createProperty(this, Arrays.asList(
                    behaviorItem.createProperty(this, "One"),
                    behaviorItem.createProperty(this, "Two"),
                    behaviorItem.createProperty(this, "Three"))));
        }

        public ArrayWidgetProperty<String> behaviorItems()
        {
            return items;
        }
    };

    @Test
    public void testArrayWidgetProperty() throws Exception
    {
        final DemoWidget widget = new DemoWidget();

        System.out.println(widget + " items:");
        List<WidgetProperty<String>> items = widget.behaviorItems().getValue();
        for (WidgetProperty<String> item : items)
            System.out.println(item);
        assertThat(items.size(), equalTo(3));
        assertThat(items.get(1).getValue(), equalTo("Two"));

        // Existing element can be modified
        widget.behaviorItems().getValue().get(1).setValue("Another (2)");
        System.out.println(widget + " items:");
        for (WidgetProperty<String> item : items)
            System.out.println(item);
        assertThat(items.size(), equalTo(3));
        assertThat(items.get(1).getValue(), equalTo("Another (2)"));

        // Cannot add/remove elements to 'value', need to go via ArrayProperty API
        try
        {
            widget.behaviorItems().getValue().remove(1);
            fail("Modified the array via value's List<>");
        }
        catch (UnsupportedOperationException ex)
        {
            // pass
        }

        // TODO Add/remove elements via ArrayProperty API
    }

    @Test
    public void testPersist() throws Exception
    {
        final DemoWidget widget = new DemoWidget();
        // Set value to non-default
        widget.behaviorItems().getValue().get(1).setValue("Another (2)");
        // Persist to XML
        String xml = ModelWriter.getXML(Arrays.asList(widget));
        System.out.println(xml);
        assertThat(xml, containsString("<items>"));
        assertThat(xml, containsString("<item>Another"));

        WidgetFactory.getInstance().addWidgetType(DemoWidget.WIDGET_DESCRIPTOR);
        // TODO Read from XML
    }
}
