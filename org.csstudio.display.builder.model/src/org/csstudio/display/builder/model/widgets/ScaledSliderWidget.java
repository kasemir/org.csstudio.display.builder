package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorLimitsFromPV;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMaximum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMinimum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayForegroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newDoublePropertyDescriptor;
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
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.diirt.vtype.VType;

/** Widget that can read/write numeric PV via scaled slider
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class ScaledSliderWidget extends VisibleWidget
{
    /** Widget descriptor */
public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("scaledslider", WidgetCategory.CONTROL,
            "Scaled Slider",
            "platform:/plugin/org.csstudio.display.builder.model/icons/scaled_slider.gif",
            "A scaled slider that can read/write a numeric PV",
            Arrays.asList("org.csstudio.opibuilder.widgets.scaledslider"))
    {
        @Override
        public Widget createWidget()
        {
            return new ScaledSliderWidget();
        }
    };

    /** Display 'horizontal': Use horizontal orientation */
    public static final WidgetPropertyDescriptor<Boolean> displayHorizontal =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "horizontal", Messages.Horizontal);

    /** Behavior 'step_increment': Regular/unit increment */ //TODO: ?add to CommonWidgetProperties
    public static final WidgetPropertyDescriptor<Double> behaviorStepIncrement =
        newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "step_increment", Messages.Increments_StepIncrement);

    /** Behavior 'page increment': Increment on page up/down */
    public static final WidgetPropertyDescriptor<Double> behaviorPageIncrement =
        newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "page_increment", Messages.Increments_PageIncrement);

    private volatile WidgetProperty<VType> value;
    private volatile WidgetProperty<Double> minimum;
    private volatile WidgetProperty<Double> maximum;
    private volatile WidgetProperty<Boolean> limits_from_pv;
    private volatile WidgetProperty<Boolean> horizontal;
    private volatile WidgetProperty<Double> page_increment;
    private volatile WidgetProperty<Double> step_increment;
    private volatile WidgetProperty<WidgetColor> foreground;
    private volatile WidgetProperty<WidgetColor> background;
    //fill_color //value bar (filled-in)
    //color_fillbackground //bar background
    //thumb_color
    //scaled-widget properties

    public ScaledSliderWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }



    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(behaviorPVName.createProperty(this, ""));
        properties.add(value = runtimeValue.createProperty(this, null));
        properties.add(displayBorderAlarmSensitive.createProperty(this, true));
        properties.add(minimum = behaviorMinimum.createProperty(this, 0.0));
        properties.add(maximum = behaviorMaximum.createProperty(this, 100.0));
        properties.add(limits_from_pv = behaviorLimitsFromPV.createProperty(this, false));
        properties.add(horizontal = displayHorizontal.createProperty(this, true));
        properties.add(step_increment = behaviorStepIncrement.createProperty(this, 1.0));
        properties.add(page_increment = behaviorPageIncrement.createProperty(this, 10.0));
        properties.add(foreground =
                displayForegroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.TEXT)));
        properties.add(background =
                displayBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.BACKGROUND)));
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

    /** @return Display 'horizontal' */
    public WidgetProperty<Boolean> displayHorizontal()
    {
        return horizontal;
    }

    /** @return Behavior 'step_increment' */
    public WidgetProperty<Double> behaviorStepIncrement()
    {
        return step_increment;
    }

    /** @return Behavior 'page_increment' */
    public WidgetProperty<Double> behaviorPageIncrement()
    {
        return page_increment;
    }

    /** @return Display 'foreground' */
    public WidgetProperty<WidgetColor> displayForeground()
    {
        return foreground;
    }

    /** @return Display 'background' */
    public WidgetProperty<WidgetColor> displayBackground()
    {
        return background;
    }
}