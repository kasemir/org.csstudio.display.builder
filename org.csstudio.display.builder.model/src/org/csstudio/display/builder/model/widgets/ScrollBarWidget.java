/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newDoublePropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propEnabled;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propHorizontal;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propLimitsFromPV;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propMaximum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propMinimum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propPageIncrement;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propStepIncrement;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;

/** Widget that can read/write numeric PV via scrollbar
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class ScrollBarWidget extends PVWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("scrollbar", WidgetCategory.CONTROL,
            "Scrollbar",
            "platform:/plugin/org.csstudio.display.builder.model/icons/scrollbar.png",
            "A scrollbar that can read/write a numeric PV",
            Arrays.asList("org.csstudio.opibuilder.widgets.scrollbar"))
    {
        @Override
        public Widget createWidget()
        {
            return new ScrollBarWidget();
        }
    };

    /** 'show_value_tip' property: Show value tip */
    public static final WidgetPropertyDescriptor<Boolean> propShowValueTip =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_value_tip", Messages.ScrollBar_ShowValueTip);

    /** 'bar_length' property: Bar length: length visible */
    public static final WidgetPropertyDescriptor<Double> propBarLength =
        newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "bar_length", Messages.ScrollBar_BarLength);

    private volatile WidgetProperty<Double> minimum;
    private volatile WidgetProperty<Double> maximum;
    private volatile WidgetProperty<Boolean> limits_from_pv;
    private volatile WidgetProperty<Boolean> horizontal;
    private volatile WidgetProperty<Boolean> show_value_tip;
    private volatile WidgetProperty<Double> bar_length;
    private volatile WidgetProperty<Double> step_increment;
    private volatile WidgetProperty<Double> page_increment;
    private volatile WidgetProperty<Boolean> enabled;

    public ScrollBarWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }



    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(minimum = propMinimum.createProperty(this, 0.0));
        properties.add(maximum = propMaximum.createProperty(this, 100.0));
        properties.add(limits_from_pv = propLimitsFromPV.createProperty(this, false));
        properties.add(horizontal = propHorizontal.createProperty(this, true));
        properties.add(show_value_tip = propShowValueTip.createProperty(this, true));
        properties.add(bar_length = propBarLength.createProperty(this, 10.0));
        properties.add(step_increment = propStepIncrement.createProperty(this, 1.0));
        properties.add(page_increment = propPageIncrement.createProperty(this, 10.0));
        properties.add(enabled = propEnabled.createProperty(this, true));
    }

    /** @return 'minimum' property */
    public WidgetProperty<Double> propMinimum()
    {
        return minimum;
    }

    /** @return 'maximum' property */
    public WidgetProperty<Double> propMaximum()
    {
        return maximum;
    }

    /** @return 'limits_from_pv' property*/
    public WidgetProperty<Boolean> propLimitsFromPV()
    {
        return limits_from_pv;
    }

    /** @return 'horizontal' property */
    public WidgetProperty<Boolean> propHorizontal()
    {
        return horizontal;
    }

    /** @return 'show_value_tip' property */
    public WidgetProperty<Boolean> propShowValueTip()
    {
        return show_value_tip;
    }

    /** @return 'bar_length' property */
    public WidgetProperty<Double> propBarLength()
    {
        return bar_length;
    }

    /** @return 'step_increment' property */
    public WidgetProperty<Double> propStepIncrement()
    {
        return step_increment;
    }

    /** @return 'page_increment' property */
    public WidgetProperty<Double> propPageIncrement()
    {
        return page_increment;
    }

    /** @return 'enabled' property */
    public WidgetProperty<Boolean> propEnabled()
    {
        return enabled;
    }
}