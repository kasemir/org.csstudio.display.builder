package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorLimitsFromPV;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMaximum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMinimum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;

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
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "horizontal", Messages.WidgetProperties_Horizontal);

    /** Display 'show_value_tip': Use horizontal orientation */
    public static final WidgetPropertyDescriptor<Boolean> displayShowValueTip =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_value_tip", Messages.ScrollBar_ShowValueTip);

    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<Double> minimum;
    private volatile WidgetProperty<Double> maximum;
    private volatile WidgetProperty<Boolean> limits_from_pv;
    private volatile WidgetProperty<Boolean> horizontal;
    private volatile WidgetProperty<Boolean> show_value_tip;
    //display: show_value_tip, horizontal,
    //behavior: bar_length (?? confusing description & behavior in old CSS)
    //behavior: step_increment, page_increment

    public ScrollBarWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }



    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
        properties.add(displayBorderAlarmSensitive.createProperty(this, true));
        properties.add(minimum = behaviorMinimum.createProperty(this, 0.0));
        properties.add(maximum = behaviorMaximum.createProperty(this, 0.0));
        properties.add(limits_from_pv = behaviorLimitsFromPV.createProperty(this, false));
        properties.add(horizontal = displayHorizontal.createProperty(this, true));
        properties.add(show_value_tip = displayShowValueTip.createProperty(this, true));
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return pv_name;
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

}