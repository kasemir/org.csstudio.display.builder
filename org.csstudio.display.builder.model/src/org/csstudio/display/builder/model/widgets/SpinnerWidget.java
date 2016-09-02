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
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayForegroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFormat;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayPrecision;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

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

    public static final WidgetPropertyDescriptor<Double> behaviorStepIncrement =
            CommonWidgetProperties.newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "step_increment", Messages.Spinner_StepIncrement);

    public static final WidgetPropertyDescriptor<Double> behaviorPageIncrement =
            CommonWidgetProperties.newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "page_increment", Messages.Spinner_PageIncrement);

    public static final WidgetPropertyDescriptor<Boolean> displayButtonsOnLeft =
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
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
        properties.add(background = displayBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.WRITE_BACKGROUND)));
        properties.add(foreground = displayForegroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.TEXT)));
        properties.add(format = displayFormat.createProperty(this, FormatOption.DECIMAL));
        properties.add(precision = displayPrecision.createProperty(this, -1));
        properties.add(value = runtimeValue.createProperty(this, null));
        properties.add(minimum = behaviorMinimum.createProperty(this, 0.0));
        properties.add(maximum = behaviorMaximum.createProperty(this, 100.0));
        properties.add(limits_from_pv = behaviorLimitsFromPV.createProperty(this, true));
        properties.add(step_increment = behaviorStepIncrement.createProperty(this, 1.0));
        properties.add(page_increment = behaviorPageIncrement.createProperty(this, 10.0));
        properties.add(buttons_on_left = displayButtonsOnLeft.createProperty(this, false));
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return pv_name;
    }

    /** @return Display 'background_color' */
    public WidgetProperty<WidgetColor> displayBackgroundColor()
    {
        return background;
    }

    /** @return Display 'foreground_color' */
    public WidgetProperty<WidgetColor> displayForegroundColor()
    {
        return foreground;
    }

    /** @return Display 'format' */
    public WidgetProperty<FormatOption> displayFormat()
    {
        return format;
    }

    /** @return Display 'precision' */
    public WidgetProperty<Integer> displayPrecision()
    {
        return precision;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
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

    /** @return Behavior 'limits_from_pv' */
    public WidgetProperty<Boolean> behaviorLimitsFromPV()
    {
        return limits_from_pv;
    }

    /** @return Behavior 'step_increment' */
    public WidgetProperty<Double> behaviorStepIncrement()
    {
        return step_increment;
    }

    /** @return Behavior 'step_increment' */
    public WidgetProperty<Double> behaviorPageIncrement()
    {
        return page_increment;
    }

    /** @return Display 'buttons_on_left' */
    public WidgetProperty<Boolean> displayButtonsOnLeft()
    {
        return buttons_on_left;
    }
}
