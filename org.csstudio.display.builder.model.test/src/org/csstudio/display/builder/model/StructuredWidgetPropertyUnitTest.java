/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.csstudio.display.builder.model.StructuredWidgetProperty.Descriptor;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.junit.Test;

/** JUnit test of structured widget property
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class StructuredWidgetPropertyUnitTest
{
    /** Demo structured property */
    private final static StructuredWidgetProperty.Descriptor behaviorTrace =
            new Descriptor(WidgetPropertyCategory.BEHAVIOR, "trace", "Trace");

    /** Demo widget that has a structured property */
    private static class PlotWidget extends Widget
    {
        public static final WidgetDescriptor WIDGET_DESCRIPTOR
            = new WidgetDescriptor("plot", WidgetCategory.GRAPHIC, "Plot", "no-icon.png", "Demo widget")
            {
                @Override
                public Widget createWidget()
                {
                    return new PlotWidget();
                }
            };

        private StructuredWidgetProperty trace;

        public PlotWidget()
        {
            super("plot");
        }

        @Override
        protected void defineProperties(List<WidgetProperty<?>> properties)
        {
            super.defineProperties(properties);
            properties.add( trace = behaviorTrace.createProperty(this, Arrays.asList(
                    CommonWidgetProperties.behaviorPVName.createProperty(this, ""),
                    CommonWidgetProperties.displayForegroundColor.createProperty(this, new WidgetColor(0, 0, 255))
                    )));
        }

        public StructuredWidgetProperty behaviorTrace()
        {
            return trace;
        }
    };

    @Test
    public void testStructuredWidgetProperty() throws Exception
    {
        final PlotWidget widget = new PlotWidget();

        System.out.println(widget + " trace:");
        for (WidgetProperty<?> trace_element : widget.getPropertyValue(behaviorTrace))
            System.out.println(trace_element);

        // Structure elements are always in XML, even with default value
        widget.behaviorTrace().getValue().get(0).setValueFromObject("position");
        String xml = ModelWriter.getXML(Arrays.asList(widget));
        System.out.println(xml);
        assertThat(xml, containsString("<trace>"));
        assertThat(xml, containsString("position"));
        assertThat(xml, containsString("color"));

        final WidgetProperty<WidgetColor> color = widget.behaviorTrace().getElement(1);
        color.setValue(new WidgetColor(255, 255, 0));
        xml = ModelWriter.getXML(Arrays.asList(widget));
        System.out.println(xml);
        assertThat(xml, containsString("color"));

        // Read back from XML
        WidgetFactory.getInstance().addWidgetType(PlotWidget.WIDGET_DESCRIPTOR);
        final DisplayModel model = ModelReader.parseXML(xml);
        System.out.println(model);
        assertThat(model.getChildren().size(), equalTo(1));
        assertThat(model.getChildren().get(0).getType(), equalTo("plot"));
        final PlotWidget copy = (PlotWidget)model.getChildren().get(0);
        System.out.println(copy);
        System.out.println(copy.getProperties());
        final WidgetProperty<String> pv_name = copy.behaviorTrace().getElement(0);
        System.out.println(pv_name);
        assertThat(pv_name.getValue(), equalTo("position"));
    }

    @Test
    public void testUnmodifiable() throws Exception
    {
        final PlotWidget widget = new PlotWidget();
        try
        {
            widget.behaviorTrace().setValue(Collections.emptyList());
            fail("Structure allowed modification");
        }
        catch (IllegalAccessError ex)
        {
            assertThat(ex.getMessage(), containsString("cannot"));
        }
        try
        {
            widget.behaviorTrace().setValueFromObject(Collections.emptyList());
            fail("Structure allowed modification");
        }
        catch (Exception ex)
        {
            assertThat(ex.getMessage(), containsString("cannot"));
        }
    }

    @Test
    public void testElementAccess() throws Exception
    {
        final PlotWidget widget = new PlotWidget();

        final WidgetProperty<String> name1 = widget.behaviorTrace().getElement(0);
        final WidgetProperty<String> name2 = widget.behaviorTrace().getElement("pv_name");
        assertThat(name1, sameInstance(name2));

        WidgetProperty<WidgetColor> color_prop = widget.behaviorTrace().getElement(1);
        WidgetColor color = color_prop.getValue();
        System.out.println(color);

        color_prop = widget.behaviorTrace().getElement(0);
        try
        {
            color = color_prop.getValue();
            System.out.println(color);
            fail("Allowed access to String property as color");
        }
        catch (ClassCastException ex)
        {
            assertThat(ex.getMessage(), containsString("String cannot"));
        }
    }
}
