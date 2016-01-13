/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorLimitsFromPV;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMaximum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMinimum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFillColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.diirt.vtype.VType;

/** Widget that displays a progress bar
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ProgressBarWidget extends Widget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("progressbar", WidgetCategory.MONITOR,
            "Progress Bar",
            "platform:/plugin/org.csstudio.display.builder.model/icons/progressbar.png",
            "Bar graph widget that 'fills' relative to numeric value of a PV",
            Arrays.asList("org.csstudio.opibuilder.widgets.progressbar"))
    {
        @Override
        public Widget createWidget()
        {
            return new ProgressBarWidget();
        }
    };

    private WidgetProperty<String> pv_name;
    private WidgetProperty<Boolean> limits_from_pv;
    private WidgetProperty<Double> minimum;
    private WidgetProperty<Double> maximum;
    private WidgetProperty<WidgetColor> fill_color;
    private WidgetProperty<VType> value;

    public ProgressBarWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(fill_color = displayFillColor.createProperty(this, new WidgetColor(60, 255, 60)));
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
        properties.add(limits_from_pv = behaviorLimitsFromPV.createProperty(this, true));
        properties.add(minimum = behaviorMinimum.createProperty(this, 0.0));
        properties.add(maximum = behaviorMaximum.createProperty(this, 100.0));
        properties.add(value = runtimeValue.createProperty(this, null));
    }

    /** @return Display 'fill_color' */
    public WidgetProperty<WidgetColor> displayFillColor()
    {
        return fill_color;
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return pv_name;
    }

    /** @return Behavior 'limits_from_pv' */
    public WidgetProperty<Boolean> behaviorLimitsFromPV()
    {
        return limits_from_pv;
    }

    /** @return Behavior 'minimum' */
    public WidgetProperty<Double> behaviorMinimum()
    {
        return minimum;
    }

    /** @return Behavior 'maximum' */
    public WidgetProperty<Double> behaviorMaximum()
    {
        return maximum;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
    }
}
