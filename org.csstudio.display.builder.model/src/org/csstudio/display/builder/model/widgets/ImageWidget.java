/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.BaseWidget;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.DoubleWidgetProperty;
import org.csstudio.display.builder.model.properties.IntegerWidgetProperty;
import org.epics.vtype.VType;

/** Widget that displays an image
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ImageWidget extends BaseWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR
        = new WidgetDescriptor("image", WidgetCategory.MONITOR,
                Messages.ImageWidget_Name,
                "platform:/plugin/org.csstudio.display.builder.model/icons/image.png",
                Messages.ImageWidget_Description,
                Arrays.asList("org.csstudio.opibuilder.widgets.intensityGraph"))
        {
            @Override
            public Widget createWidget()
            {
                return new ImageWidget();
            }
        };


    private static final WidgetPropertyDescriptor<Integer> dataWidth =
        new WidgetPropertyDescriptor<Integer>(
            WidgetPropertyCategory.BEHAVIOR, "data_width", Messages.WidgetProperties_DataWidth)
    {
        @Override
        public WidgetProperty<Integer> createProperty(final Widget widget,
                                                      final Integer width)
        {
            return new IntegerWidgetProperty(this, widget, width);
        }
    };

    private static final WidgetPropertyDescriptor<Integer> dataHeight =
        new WidgetPropertyDescriptor<Integer>(
            WidgetPropertyCategory.BEHAVIOR, "data_height", Messages.WidgetProperties_DataHeight)
    {
        @Override
        public WidgetProperty<Integer> createProperty(final Widget widget,
                                                      final Integer height)
        {
            return new IntegerWidgetProperty(this, widget, height);
        }
    };

    private static final WidgetPropertyDescriptor<Double> dataMinimum =
        new WidgetPropertyDescriptor<Double>(
            WidgetPropertyCategory.BEHAVIOR, "minimum", Messages.WidgetProperties_DataMinimum)
    {
        @Override
        public WidgetProperty<Double> createProperty(final Widget widget,
                                                     final Double min)
        {
            return new DoubleWidgetProperty(this, widget, min);
        }
    };

    private static final WidgetPropertyDescriptor<Double> dataMaximum =
        new WidgetPropertyDescriptor<Double>(
            WidgetPropertyCategory.BEHAVIOR, "maximum", Messages.WidgetProperties_DataMaximum)
    {
        @Override
        public WidgetProperty<Double> createProperty(final Widget widget,
                                                      final Double max)
        {
            return new DoubleWidgetProperty(this, widget, max);
        }
    };


    private WidgetProperty<String> pv_name;
    private WidgetProperty<Integer> data_width;
    private WidgetProperty<Integer> data_height;
    private WidgetProperty<Double> data_minimum;
    private WidgetProperty<Double> data_maximum;

    private WidgetProperty<VType> value;

    public ImageWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
        properties.add(data_width = dataWidth.createProperty(this, 100));
        properties.add(data_height = dataHeight.createProperty(this, 100));
        properties.add(data_minimum = dataMinimum.createProperty(this, 0.0));
        properties.add(data_maximum = dataMaximum.createProperty(this, 255.0));
        properties.add(value = runtimeValue.createProperty(this, null));
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return pv_name;
    }

    /** @return Behavior 'data_width' */
    public WidgetProperty<Integer> behaviorDataWidth()
    {
        return data_width;
    }

    /** @return Behavior 'data_height' */
    public WidgetProperty<Integer> behaviorDataHeight()
    {
        return data_height;
    }

    /** @return Behavior 'minimum' */
    public WidgetProperty<Double> behaviorDataMinimum()
    {
        return data_minimum;
    }

    /** @return Behavior 'maximum' */
    public WidgetProperty<Double> behaviorDataMaximum()
    {
        return data_maximum;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
    }
}
