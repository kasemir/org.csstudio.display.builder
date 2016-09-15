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
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newIntegerPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newStringPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propEnabled;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propHorizontal;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propLimitsFromPV;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propMaximum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propMinimum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propPageIncrement;
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

    //XXX: The following are common scaled-widget properties

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

    /** 'show_scale' property: Show scale for scaled widget. */
    public static final WidgetPropertyDescriptor<Boolean> propShowScale =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_scale", Messages.WidgetProperties_ShowScale);

    /** 'show_minor_ticks' property: Show tick marks on scale. */
    public static final WidgetPropertyDescriptor<Boolean> propShowMinorTicks =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_minor_ticks", Messages.WidgetProperties_ShowMinorTicks);

    /** 'scale_format' property: Formatter for scale labels; follows java DecimalFormat */
    public static final WidgetPropertyDescriptor<String> propScaleFormat =
        newStringPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "scale_format", Messages.WidgetProperties_ScaleFormat);

    /** 'show_markers' property: Show tick markers for HI, HIHI, LO, & LOLO levels. */
    public static final WidgetPropertyDescriptor<Boolean> propShowMarkers =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_markers", Messages.WidgetProperties_ShowMarkers);

    /** 'major_tick_step_hint' property: Minimum space, in pixels, between major tick marks. */
    public static final WidgetPropertyDescriptor<Integer> propMajorTickStepHint =
        newIntegerPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "major_tick_step_hint", Messages.WidgetProperties_MajorTickStepHint);

    /** 'level_hi' property: Level of HI value for widget*/
    public static final WidgetPropertyDescriptor<Double> propLevelHi =
        newDoublePropertyDescriptor(WidgetPropertyCategory.DISPLAY, "level_hi", Messages.WidgetProperties_LevelHi);

    /** 'level_hihi' property: Level of HIHI value for widget*/
    public static final WidgetPropertyDescriptor<Double> propLevelHiHi =
        newDoublePropertyDescriptor(WidgetPropertyCategory.DISPLAY, "level_hihi", Messages.WidgetProperties_LevelHiHi);

    /** 'level_lo' property: Level of LO value for widget*/
    public static final WidgetPropertyDescriptor<Double> propLevelLo =
        newDoublePropertyDescriptor(WidgetPropertyCategory.DISPLAY, "level_lo", Messages.WidgetProperties_LevelLo);

    /** 'level_lolo' property: Level of LO value for widget*/
    public static final WidgetPropertyDescriptor<Double> propLevelLoLo =
        newDoublePropertyDescriptor(WidgetPropertyCategory.DISPLAY, "level_lolo", Messages.WidgetProperties_LevelLoLo);

    /** 'show_hi' property: Whether to show HI marker*/
    public static final WidgetPropertyDescriptor<Boolean> propShowHi =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_hi", Messages.WidgetProperties_ShowHi);

    /** 'show_hihi' property: Whether to show HIHI marker*/
    public static final WidgetPropertyDescriptor<Boolean> propShowHiHi =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_hihi", Messages.WidgetProperties_ShowHiHi);

    /** 'show_lo' property: Whether to show LO marker*/
    public static final WidgetPropertyDescriptor<Boolean> propShowLo =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_lo", Messages.WidgetProperties_ShowLo);

    /** 'show_lo' property: Whether to show LOLO marker*/
    public static final WidgetPropertyDescriptor<Boolean> propShowLoLo =
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
    private volatile WidgetProperty<Boolean> enabled;
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
        properties.add(propPVName.createProperty(this, ""));
        properties.add(value = runtimePropValue.createProperty(this, null));
        properties.add(propBorderAlarmSensitive.createProperty(this, true));
        properties.add(horizontal = propHorizontal.createProperty(this, true));
        properties.add(step_increment = propStepIncrement.createProperty(this, 1.0));
        properties.add(page_increment = propPageIncrement.createProperty(this, 10.0));
        properties.add(background = propBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.BACKGROUND)));
        //scaled-widget properties:
        properties.add(minimum = propMinimum.createProperty(this, 0.0));
        properties.add(maximum = propMaximum.createProperty(this, 100.0));
        properties.add(limits_from_pv = propLimitsFromPV.createProperty(this, true));
        properties.add(enabled = propEnabled.createProperty(this, true));
        properties.add(show_scale = propShowScale.createProperty(this, true));
        properties.add(show_minor_ticks = propShowMinorTicks.createProperty(this, true));
        properties.add(major_tick_step_hint = propMajorTickStepHint.createProperty(this, 20));
        properties.add(scale_format = propScaleFormat.createProperty(this, ""));
        properties.add(show_markers = propShowMarkers.createProperty(this, false));
        properties.add(level_hihi = propLevelHiHi.createProperty(this, 90.0));
        properties.add(level_hi = propLevelHi.createProperty(this, 80.0));
        properties.add(level_lo = propLevelLo.createProperty(this, 20.0));
        properties.add(level_lolo = propLevelLoLo.createProperty(this, 10.0));
        properties.add(show_hihi = propShowHiHi.createProperty(this, true));
        properties.add(show_hi = propShowHi.createProperty(this, true));
        properties.add(show_lo = propShowLo.createProperty(this, true));
        properties.add(show_lolo = propShowLoLo.createProperty(this, true));
    }

    /** @return Runtime 'value' property */
    public WidgetProperty<VType> runtimePropValue()
    {
        return value;
    }

    /** @return 'horizontal' property */
    public WidgetProperty<Boolean> propHorizontal()
    {
        return horizontal;
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

    /** @return Display 'foreground' */
    public WidgetProperty<WidgetColor> displayForegroundColor()
    {
        return foreground;
    }

    /** @return 'background' property */
    public WidgetProperty<WidgetColor> propBackgroundColor()
    {
        return background;
    }

    /** @return Display 'fill_color' */
    public WidgetProperty<WidgetColor> displayFillColor()
    {
        return fill_color;
    }

    //scaled widget properties:
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

    /** @return 'enabled' property */
    public WidgetProperty<Boolean> propEnabled()
    {
        return enabled;
    }

    /** @return Display 'scale_font' */
    public WidgetProperty<WidgetFont> displayScaleFont()
    {
        return scale_font;
    }

    /** @return 'show_scale' property */
    public WidgetProperty<Boolean> propShowScale()
    {
        return show_scale;
    }

    /** @return 'major_tick_step_hint' property */
    public WidgetProperty<Integer> propMajorTickStepHint()
    {
        return major_tick_step_hint;
    }

    /** @return 'show_minor_ticks' property */
    public WidgetProperty<Boolean> propShowMinorTicks()
    {
        return show_minor_ticks;
    }

    /** @return 'scale_format' property */
    public WidgetProperty<String> propScaleFormat()
    {
        return scale_format;
    }

    /** @return 'show_markers' property */
    public WidgetProperty<Boolean> propShowMarkers()
    {
        return show_markers;
    }

    /** @return 'level_hi' property */
    public WidgetProperty<Double> propLevelHi()
    {
        return level_hi;
    }

    /** @return 'level_hihi' property */
    public WidgetProperty<Double> propLevelHiHi()
    {
        return level_hihi;
    }

    /** @return 'level_lo' property */
    public WidgetProperty<Double> propLevelLo()
    {
        return level_lo;
    }

    /** @return 'level_lolo' property */
    public WidgetProperty<Double> propLevelLoLo()
    {
        return level_lolo;
    }

    /** @return 'show_hi' property */
    public WidgetProperty<Boolean> propShowHi()
    {
        return show_hi;
    }

    /** @return 'show_hihi' property */
    public WidgetProperty<Boolean> propShowHiHi()
    {
        return show_hihi;
    }

    /** @return 'show_lo' property */
    public WidgetProperty<Boolean> propShowLo()
    {
        return show_lo;
    }

    /** @return 'show_lolo' property */
    public WidgetProperty<Boolean> propShowLoLo()
    {
        return show_lolo;
    }
}