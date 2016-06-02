package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorLimitsFromPV;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMaximum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMinimum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
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
        newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "step_increment", Messages.Spinner_StepIncrement);

    /** Behavior 'page_increment': Increment on page up/down */
    public static final WidgetPropertyDescriptor<Double> behaviorPageIncrement =
        newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "page_increment", Messages.Spinner_PageIncrement);

    //XXX: The following are common scaled-widget properties

    /** Position 'major_tick_step_hint': Minimum space, in pixels, between major tick marks. */
    public static final WidgetPropertyDescriptor<Integer> positionMajorTickStepHint =
            newIntegerPropertyDescriptor(WidgetPropertyCategory.POSITION, "major_tick_step_hint", Messages.WidgetProperties_MajorTickStepHint);

    //define in defineProperties() when representation implemented
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

    /** Display 'show_markers': Show tick markers for HI, HIHI, LO, & LOLO levels. */
    public static final WidgetPropertyDescriptor<Boolean> displayShowMarkers =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_markers", Messages.WidgetProperties_ShowMarkers);

    /** Display 'level_hi': Level of HI value for widget*/
    public static final WidgetPropertyDescriptor<Double> displayLevelHi =
            newDoublePropertyDescriptor(WidgetPropertyCategory.DISPLAY, "level_hi", Messages.WidgetProperties_LevelHi);

    /** Display 'level_hihi': Level of HIHI value for widget*/
    public static final WidgetPropertyDescriptor<Double> displayLevelHiHi =
            newDoublePropertyDescriptor(WidgetPropertyCategory.DISPLAY, "level_hihi", Messages.WidgetProperties_LevelHiHi);

    /** Display 'level_lo': Level of LO value for widget*/
    public static final WidgetPropertyDescriptor<Double> displayLevelLo =
            newDoublePropertyDescriptor(WidgetPropertyCategory.DISPLAY, "level_lo", Messages.WidgetProperties_LevelLo);

    /** Display 'level_lolo': Level of LO value for widget*/
    public static final WidgetPropertyDescriptor<Double> displayLevelLoLo =
            newDoublePropertyDescriptor(WidgetPropertyCategory.DISPLAY, "level_lolo", Messages.WidgetProperties_LevelLoLo);

    /** Display 'show_hi': Whether to show HI marker*/
    public static final WidgetPropertyDescriptor<Boolean> displayShowHi =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_hi", Messages.WidgetProperties_ShowHi);

    /** Display 'show_hihi': Whether to show HIHI marker*/
    public static final WidgetPropertyDescriptor<Boolean> displayShowHiHi =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_hihi", Messages.WidgetProperties_ShowHiHi);

    /** Display 'show_lo': Whether to show LO marker*/
    public static final WidgetPropertyDescriptor<Boolean> displayShowLo =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_lo", Messages.WidgetProperties_ShowLo);

    /** Display 'show_lo': Whether to show LOLO marker*/
    public static final WidgetPropertyDescriptor<Boolean> displayShowLoLo =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_lolo", Messages.WidgetProperties_ShowLoLo);

    private volatile WidgetProperty<VType> value;
    private volatile WidgetProperty<Boolean> horizontal;
    private volatile WidgetProperty<Double> page_increment;
    private volatile WidgetProperty<Double> step_increment;
    private volatile WidgetProperty<WidgetColor> foreground; //define in defineProperties() when representation implemented
    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<WidgetColor> fill_color; //define in defineProperties() when representation implemented
    //potential future properties: 'color_fillbackground' 'bar_background', 'thumb_color'
    //scaled-widget properties:
    private volatile WidgetProperty<Double> minimum;
    private volatile WidgetProperty<Double> maximum;
    private volatile WidgetProperty<Boolean> limits_from_pv;
    private volatile WidgetProperty<Integer> major_tick_step_hint;
    private volatile WidgetProperty<WidgetFont> scale_font; //define in defineProperties() when representation implemented
    private volatile WidgetProperty<Boolean> show_scale;
    private volatile WidgetProperty<Boolean> show_minor_ticks;
    private volatile WidgetProperty<String> scale_format;
    private volatile WidgetProperty<Boolean> show_markers;
    private volatile WidgetProperty<Double> level_hi;
    private volatile WidgetProperty<Double> level_hihi;
    private volatile WidgetProperty<Double> level_lo;
    private volatile WidgetProperty<Double> level_lolo;
    private volatile WidgetProperty<Boolean> show_hi;
    private volatile WidgetProperty<Boolean> show_hihi;
    private volatile WidgetProperty<Boolean> show_lo;
    private volatile WidgetProperty<Boolean> show_lolo;

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
        properties.add(background =
                displayBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.BACKGROUND)));
        //scaled-widget properties:
        properties.add(minimum = behaviorMinimum.createProperty(this, 0.0));
        properties.add(maximum = behaviorMaximum.createProperty(this, 100.0));
        properties.add(limits_from_pv = behaviorLimitsFromPV.createProperty(this, false));
        properties.add(major_tick_step_hint = positionMajorTickStepHint.createProperty(this, 20));
        properties.add(show_scale = displayShowScale.createProperty(this, true));
        properties.add(show_minor_ticks = displayShowMinorTicks.createProperty(this, true));
        properties.add(scale_format = displayScaleFormat.createProperty(this, ""));
        properties.add(show_markers = displayShowMarkers.createProperty(this, false));
        properties.add(level_hihi = displayLevelHiHi.createProperty(this, 90.0));
        properties.add(level_hi = displayLevelHi.createProperty(this, 80.0));
        properties.add(level_lo = displayLevelLo.createProperty(this, 20.0));
        properties.add(level_lolo = displayLevelLoLo.createProperty(this, 10.0));
        properties.add(show_hihi = displayShowHiHi.createProperty(this, true));
        properties.add(show_hi = displayShowHi.createProperty(this, true));
        properties.add(show_lo = displayShowLo.createProperty(this, true));
        properties.add(show_lolo = displayShowLoLo.createProperty(this, true));
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
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

    //scaled widget properties:
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

    /** @return Position 'major_tick_step_hint' */
    public WidgetProperty<Integer> positionMajorTickStepHint()
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

    /** @return Display 'show_markers' */
    public WidgetProperty<Boolean> displayShowMarkers()
    {
        return show_markers;
    }

    /** @return Display 'level_hi' */
    public WidgetProperty<Double> displayLevelHi()
    {
        return level_hi;
    }

    /** @return Display 'level_hihi' */
    public WidgetProperty<Double> displayLevelHiHi()
    {
        return level_hihi;
    }

    /** @return Display 'level_lo' */
    public WidgetProperty<Double> displayLevelLo()
    {
        return level_lo;
    }

    /** @return Display 'level_lolo' */
    public WidgetProperty<Double> displayLevelLoLo()
    {
        return level_lolo;
    }

    /** @return Display 'show_hi' */
    public WidgetProperty<Boolean> displayShowHi()
    {
        return show_hi;
    }

    /** @return Display 'show_hihi' */
    public WidgetProperty<Boolean> displayShowHiHi()
    {
        return show_hihi;
    }

    /** @return Display 'show_lo' */
    public WidgetProperty<Boolean> displayShowLo()
    {
        return show_lo;
    }

    /** @return Display 'show_lolo' */
    public WidgetProperty<Boolean> displayShowLoLo()
    {
        return show_lolo;
    }
}