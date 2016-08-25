package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorEnabled;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorLimitsFromPV;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMaximum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMinimum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
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
import org.diirt.vtype.VType;

/** Widget that can read/write numeric PV via scrollbar
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class ScrollBarWidget extends VisibleWidget
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

    /** Display 'horizontal': Use horizontal orientation */ //TODO: ?add to CommonWidgetProperties
    public static final WidgetPropertyDescriptor<Boolean> displayHorizontal =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "horizontal", Messages.Horizontal);

    /** Display 'show_value_tip': Show value tip */
    public static final WidgetPropertyDescriptor<Boolean> displayShowValueTip =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_value_tip", Messages.ScrollBar_ShowValueTip);

    /** Behavior 'bar_length': Bar length: length visible */
    public static final WidgetPropertyDescriptor<Double> behaviorBarLength =
        newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "bar_length", Messages.ScrollBar_BarLength);

    /** Behavior 'step_increment': Regular/unit increment */
    public static final WidgetPropertyDescriptor<Double> behaviorStepIncrement =
        newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "step_increment", Messages.Spinner_StepIncrement);

    /** Behavior 'page increment': Increment on page up/down */
    public static final WidgetPropertyDescriptor<Double> behaviorPageIncrement =
        newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "page_increment", Messages.Spinner_PageIncrement);

    private volatile WidgetProperty<VType> value;
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
        properties.add(behaviorPVName.createProperty(this, ""));
        properties.add(value = runtimeValue.createProperty(this, null));
        properties.add(displayBorderAlarmSensitive.createProperty(this, true));
        properties.add(minimum = behaviorMinimum.createProperty(this, 0.0));
        properties.add(maximum = behaviorMaximum.createProperty(this, 100.0));
        properties.add(limits_from_pv = behaviorLimitsFromPV.createProperty(this, false));
        properties.add(horizontal = displayHorizontal.createProperty(this, true));
        properties.add(show_value_tip = displayShowValueTip.createProperty(this, true));
        properties.add(bar_length = behaviorBarLength.createProperty(this, 10.0));
        properties.add(step_increment = behaviorStepIncrement.createProperty(this, 1.0));
        properties.add(page_increment = behaviorPageIncrement.createProperty(this, 10.0));
        properties.add(enabled = behaviorEnabled.createProperty(this, true));
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

    /** @return Display 'show_value_tip' */
    public WidgetProperty<Boolean> displayShowValueTip()
    {
        return show_value_tip;
    }

    /** @return Behavior 'bar_length' */
    public WidgetProperty<Double> behaviorBarLength()
    {
        return bar_length;
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

    /** @return Behavior 'enabled' */
    public WidgetProperty<Boolean> behaviorEnabled()
    {
        return enabled;
    }
}