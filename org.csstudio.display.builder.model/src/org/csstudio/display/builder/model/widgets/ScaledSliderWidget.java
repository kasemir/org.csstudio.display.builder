package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorLimitsFromPV;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMaximum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMinimum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFillColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayForegroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newDoublePropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newIntegerPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newStringPropertyDescriptor;
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
import org.csstudio.display.builder.model.properties.FontWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
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

    /** Behavior 'page_increment': Increment on page up/down */
    public static final WidgetPropertyDescriptor<Double> behaviorPageIncrement =
        newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "page_increment", Messages.Increments_PageIncrement);

    //XXX: The following are common scaled-widget properties

    /** Display 'major_tick_step_hint': Minimum space, in pixels, between major tick marks. */
    public static final WidgetPropertyDescriptor<Integer> displayMajorTickStepHint =
            newIntegerPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "major_tick_step_hint", Messages.WidgetProperties_MajorTickStepHint);

    /** Display 'scale_font': Font for scale */
    public static final WidgetPropertyDescriptor<WidgetFont> displayScaleFont =
        new WidgetPropertyDescriptor<WidgetFont>(
            WidgetPropertyCategory.DISPLAY, "scale_font", Messages.WidgetProperties_Font)
    {
        @Override
        public WidgetProperty<WidgetFont> createProperty(final Widget widget,
                                                         final WidgetFont font)
        {
            return new FontWidgetProperty(this, widget, font);
        }
    };

    /** Display 'show_scale': Show scale for scaled widget. */
    public static final WidgetPropertyDescriptor<Boolean> displayShowScale =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_scale", Messages.WidgetProperties_ShowScale);

    /** Display 'show_minor_ticks': Show tick marks on scale. */
    public static final WidgetPropertyDescriptor<Boolean> displayShowMinorTicks =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_minor_ticks", Messages.WidgetProperties_ShowMinorTicks);

    /** Display 'scale_format': Formatter for scale labels; follows java DecimalFormat */
    public static final WidgetPropertyDescriptor<String> displayScaleFormat =
            newStringPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "scale_format", Messages.WidgetProperties_ScaleFormat);

    private volatile WidgetProperty<VType> value;
    private volatile WidgetProperty<Boolean> horizontal;
    private volatile WidgetProperty<Double> page_increment;
    private volatile WidgetProperty<Double> step_increment;
    private volatile WidgetProperty<WidgetColor> foreground; //TODO: represent foreground
    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<WidgetColor> fill_color; //TODO: represent fill_color
    //color_fillbackground //bar background
    //thumb_color
    //scaled-widget properties:
    private volatile WidgetProperty<Double> minimum;
    private volatile WidgetProperty<Double> maximum;
    private volatile WidgetProperty<Boolean> limits_from_pv;
    private volatile WidgetProperty<Integer> major_tick_step_hint;
    private volatile WidgetProperty<WidgetFont> scale_font;
    private volatile WidgetProperty<Boolean> show_scale;
    private volatile WidgetProperty<Boolean> show_minor_ticks;
    private volatile WidgetProperty<String> scale_format;

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
        properties.add(horizontal = displayHorizontal.createProperty(this, true));
        properties.add(step_increment = behaviorStepIncrement.createProperty(this, 1.0));
        properties.add(page_increment = behaviorPageIncrement.createProperty(this, 10.0));
        properties.add(foreground =
                displayForegroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.TEXT)));
        properties.add(background =
                displayBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.BACKGROUND)));
        properties.add(fill_color = displayFillColor.createProperty(this, new WidgetColor(60, 255, 60)));
        //scaled-widget properties:
        properties.add(minimum = behaviorMinimum.createProperty(this, 0.0));
        properties.add(maximum = behaviorMaximum.createProperty(this, 100.0));
        properties.add(limits_from_pv = behaviorLimitsFromPV.createProperty(this, false));
        properties.add(major_tick_step_hint = displayMajorTickStepHint.createProperty(this, 20));
        properties.add(show_scale = displayShowScale.createProperty(this, true));
        properties.add(show_minor_ticks = displayShowMinorTicks.createProperty(this, true));
        properties.add(scale_format = displayScaleFormat.createProperty(this, ""));
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
    public WidgetProperty<WidgetColor> displayForegroundColor()
    {
        return foreground;
    }

    /** @return Display 'background' */
    public WidgetProperty<WidgetColor> displayBackgroundColor()
    {
        return background;
    }

    /** @return Display 'fill_color' */
    public WidgetProperty<WidgetColor> displayFillColor()
    {
        return fill_color;
    }

    /** @return Display 'major_tick_step_hint' */
    public WidgetProperty<Integer> displayMajorTickStepHint()
    {
        return major_tick_step_hint;
    }

    /** @return Display 'scale_font' */
    public WidgetProperty<WidgetFont> displayScaleFont()
    {
        return scale_font;
    }

    /** @return Display 'show_scale' */
    public WidgetProperty<Boolean> displayShowScale()
    {
        return show_scale;
    }

    /** @return Display 'show_minor_ticks' */
    public WidgetProperty<Boolean> displayShowMinorTicks()
    {
        return show_minor_ticks;
    }

    /** @return Display 'scale_format' */
    public WidgetProperty<String> displayScaleFormat()
    {
        return scale_format;
    }

}