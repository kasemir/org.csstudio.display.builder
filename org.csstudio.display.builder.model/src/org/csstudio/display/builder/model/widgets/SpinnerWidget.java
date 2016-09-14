/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propForegroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propFormat;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propLimitsFromPV;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propMaximum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propMinimum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propPageIncrement;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propPrecision;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propStepIncrement;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimePropValue;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.FormatOption;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.diirt.vtype.VType;

/** Widget that represents a spinner
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class SpinnerWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("spinner", WidgetCategory.CONTROL,
            "Spinner",
            "platform:/plugin/org.csstudio.display.builder.model/icons/Spinner.gif",
            "A spinner, with up/down arrows",
            Arrays.asList("org.csstudio.opibuilder.widgets.spinner"))
        {
            @Override
            public Widget createWidget()
            {
                return new SpinnerWidget();
            }
        };

    public static final WidgetPropertyDescriptor<Boolean> propButtonsOnLeft =
            CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "buttons_on_left", Messages.Spinner_ButtonsOnLeft);

    //TODO: spinner format uses only Decimal, Exponential, and Hex; also (new?) Engineering?

    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<WidgetColor> foreground;
    private volatile WidgetProperty<FormatOption> format; //includes decimal, exponential, and hex
    private volatile WidgetProperty<Integer> precision;
    private volatile WidgetProperty<VType> value;
    private volatile WidgetProperty<Double> minimum;
    private volatile WidgetProperty<Double> maximum;
    private volatile WidgetProperty<Boolean> limits_from_pv;
    //increments: configurable at Runtime from its context menu Configure Runtime Properties....
        //does this make runtime category?
    private volatile WidgetProperty<Double> step_increment;
    private volatile WidgetProperty<Double> page_increment;
    private volatile WidgetProperty<Boolean> buttons_on_left;

    public SpinnerWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(pv_name = propPVName.createProperty(this, ""));
        properties.add(format = propFormat.createProperty(this, FormatOption.DECIMAL));
        properties.add(precision = propPrecision.createProperty(this, -1));
        properties.add(foreground = propForegroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.TEXT)));
        properties.add(background = propBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.WRITE_BACKGROUND)));
        properties.add(value = runtimePropValue.createProperty(this, null));
        properties.add(minimum = propMinimum.createProperty(this, 0.0));
        properties.add(maximum = propMaximum.createProperty(this, 100.0));
        properties.add(limits_from_pv = propLimitsFromPV.createProperty(this, true));
        properties.add(step_increment = propStepIncrement.createProperty(this, 1.0));
        properties.add(page_increment = propPageIncrement.createProperty(this, 10.0));
        properties.add(buttons_on_left = propButtonsOnLeft.createProperty(this, false));
    }

    /** @return 'pv_name' property */
    public WidgetProperty<String> propPVName()
    {
        return pv_name;
    }

    /** @return 'background_color' property */
    public WidgetProperty<WidgetColor> propBackgroundColor()
    {
        return background;
    }

    /** @return 'foreground_color' property */
    public WidgetProperty<WidgetColor> propForegroundColor()
    {
        return foreground;
    }

    /** @return 'format' property */
    public WidgetProperty<FormatOption> propFormat()
    {
        return format;
    }

    /** @return 'precision' property */
    public WidgetProperty<Integer> propPrecision()
    {
        return precision;
    }

    /** @return Runtime 'value' property */
    public WidgetProperty<VType> runtimePropValue()
    {
        return value;
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

    /** @return 'limits_from_pv' property */
    public WidgetProperty<Boolean> propLimitsFromPV()
    {
        return limits_from_pv;
    }

    /** @return 'step_increment' property */
    public WidgetProperty<Double> propStepIncrement()
    {
        return step_increment;
    }

    /** @return 'step_increment' property */
    public WidgetProperty<Double> propPageIncrement()
    {
        return page_increment;
    }

    /** @return 'buttons_on_left' property */
    public WidgetProperty<Boolean> propButtonsOnLeft()
    {
        return buttons_on_left;
    }
}
