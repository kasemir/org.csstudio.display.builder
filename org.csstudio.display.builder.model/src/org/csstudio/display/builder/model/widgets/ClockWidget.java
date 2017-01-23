/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.model.widgets;


import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propTransparent;

import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;

import eu.hansolo.medusa.Clock;


/**
 * Widget displaying date and/or time.
 *
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 18 Jan 2017
 */
public class ClockWidget extends VisibleWidget {

    public static final WidgetDescriptor WIDGET_DESCRIPTOR = new WidgetDescriptor(
        "clock",
        WidgetCategory.MISC,
        "Clock",
        "platform:/plugin/org.csstudio.display.builder.model/icons/clock.png",
        "Simple clock"
    ) {
        @Override
        public Widget createWidget ( ) {
            return new ClockWidget();
        }
    };

    public enum Skin {

        CLOCK(Clock.ClockSkinType.CLOCK),
        DIGITAL(Clock.ClockSkinType.DIGITAL),
        INDUSTRIAL(Clock.ClockSkinType.INDUSTRIAL),
        PEAR(Clock.ClockSkinType.PEAR),
        PLAIN(Clock.ClockSkinType.PLAIN),
        SLIM(Clock.ClockSkinType.SLIM),
        TEXT(Clock.ClockSkinType.TEXT),
        TILE(Clock.ClockSkinType.TILE),
        YOTA2(Clock.ClockSkinType.YOTA2);

        private final Clock.ClockSkinType skinType;

        Skin ( Clock.ClockSkinType skinType ) {
            this.skinType = skinType;
        }

        public Clock.ClockSkinType skinType() {
            return skinType;
        }

    }

    public static final WidgetPropertyDescriptor<WidgetColor> propBorderColor           = CommonWidgetProperties.newColorPropertyDescriptor  (WidgetPropertyCategory.DISPLAY,  "border_color",             Messages.WidgetProperties_BorderColor);
    public static final WidgetPropertyDescriptor<Double>      propBorderWidth           = CommonWidgetProperties.newDoublePropertyDescriptor (WidgetPropertyCategory.DISPLAY,  "border_width",             Messages.WidgetProperties_BorderWidth);
    public static final WidgetPropertyDescriptor<WidgetColor> propDateColor             = CommonWidgetProperties.newColorPropertyDescriptor  (WidgetPropertyCategory.DISPLAY,  "date_color",               Messages.WidgetProperties_DateColor);
    public static final WidgetPropertyDescriptor<Boolean>     propDateVisible           = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY,  "date_visible",             Messages.WidgetProperties_DateVisible);
    public static final WidgetPropertyDescriptor<Boolean>     propDiscreteHours         = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "discrete_hours",           Messages.WidgetProperties_DiscreteHours);
    public static final WidgetPropertyDescriptor<Boolean>     propDiscreteMinutes       = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "discrete_minutes",         Messages.WidgetProperties_DiscreteMinutes);
    public static final WidgetPropertyDescriptor<Boolean>     propDiscreteSeconds       = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "discrete_seconds",         Messages.WidgetProperties_DiscreteSeconds);
    public static final WidgetPropertyDescriptor<WidgetColor> propHourColor             = CommonWidgetProperties.newColorPropertyDescriptor  (WidgetPropertyCategory.DISPLAY,  "hour_color",               Messages.WidgetProperties_HourColor);
    public static final WidgetPropertyDescriptor<WidgetColor> propHourTickMarkColor     = CommonWidgetProperties.newColorPropertyDescriptor  (WidgetPropertyCategory.DISPLAY,  "hour_tick_mark_color",     Messages.WidgetProperties_HourTickMarkColor);
    public static final WidgetPropertyDescriptor<Boolean>     propHourTickMarkVisible   = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY,  "hour_tick_mark_visible",   Messages.WidgetProperties_HourTickMarkVisible);
    public static final WidgetPropertyDescriptor<WidgetColor> propKnobColor             = CommonWidgetProperties.newColorPropertyDescriptor  (WidgetPropertyCategory.DISPLAY,  "knob_color",               Messages.WidgetProperties_KnobColor);
    public static final WidgetPropertyDescriptor<WidgetColor> propMinuteColor           = CommonWidgetProperties.newColorPropertyDescriptor  (WidgetPropertyCategory.DISPLAY,  "minute_color",             Messages.WidgetProperties_MinuteColor);
    public static final WidgetPropertyDescriptor<WidgetColor> propMinuteTickMarkColor   = CommonWidgetProperties.newColorPropertyDescriptor  (WidgetPropertyCategory.DISPLAY,  "minute_tick_mark_color",   Messages.WidgetProperties_MinuteTickMarkColor);
    public static final WidgetPropertyDescriptor<Boolean>     propMinuteTickMarkVisible = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY,  "minute_tick_mark_visible", Messages.WidgetProperties_MinuteTickMarkVisible);
    public static final WidgetPropertyDescriptor<Boolean>     propRunning               = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "running",                  Messages.WidgetProperties_Running);
    public static final WidgetPropertyDescriptor<WidgetColor> propSecondColor           = CommonWidgetProperties.newColorPropertyDescriptor  (WidgetPropertyCategory.DISPLAY,  "second_color",             Messages.WidgetProperties_SecondColor);
    public static final WidgetPropertyDescriptor<Boolean>     propSecondVisible         = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY,  "second_visible",           Messages.WidgetProperties_SecondVisible);
    public static final WidgetPropertyDescriptor<Skin>        propSkin                  = new WidgetPropertyDescriptor<Skin>                 (WidgetPropertyCategory.WIDGET,   "skin",                     Messages.WidgetProperties_Skin) {
        @Override
        public EnumWidgetProperty<Skin> createProperty ( Widget widget, Skin defaultValue ) {
            return new EnumWidgetProperty<>(this, widget, defaultValue);
        }
    };

    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<WidgetColor> borderColor;
    private volatile WidgetProperty<Double>      borderWidth;
    private volatile WidgetProperty<WidgetColor> dateColor;
    private volatile WidgetProperty<Boolean>     dateVisible;
    private volatile WidgetProperty<Boolean>     discreteHours;
    private volatile WidgetProperty<Boolean>     discreteMinutes;
    private volatile WidgetProperty<Boolean>     discreteSeconds;
    private volatile WidgetProperty<WidgetColor> hourColor;
    private volatile WidgetProperty<WidgetColor> hourTickMarkColor;
    private volatile WidgetProperty<Boolean>     hourTickMarkVisible;
    private volatile WidgetProperty<WidgetColor> knobColor;
    private volatile WidgetProperty<WidgetColor> minuteColor;
    private volatile WidgetProperty<WidgetColor> minuteTickMarkColor;
    private volatile WidgetProperty<Boolean>     minuteTickMarkVisible;
    private volatile WidgetProperty<Boolean>     running;
    private volatile WidgetProperty<WidgetColor> secondColor;
    private volatile WidgetProperty<Boolean>     secondVisible;
    private volatile WidgetProperty<Skin>        skin;
    private volatile WidgetProperty<Boolean>     transparent;

    public ClockWidget ( ) {
        super(WIDGET_DESCRIPTOR.getType(), 120, 120);
    }

    public WidgetProperty<WidgetColor> propBackgroundColor ( ) {
        return background;
    }

    public WidgetProperty<WidgetColor> propBorderColor ( ) {
        return borderColor;
    }

    public WidgetProperty<Double> propBorderWidth ( ) {
        return borderWidth;
    }

    public WidgetProperty<WidgetColor> propDateColor ( ) {
        return dateColor;
    }

    public WidgetProperty<Boolean> propDateVisible ( ) {
        return dateVisible;
    }

    public WidgetProperty<Boolean> propDiscreteHours ( ) {
        return discreteHours;
    }

    public WidgetProperty<Boolean> propDiscreteMinutes ( ) {
        return discreteMinutes;
    }

    public WidgetProperty<Boolean> propDiscreteSeconds ( ) {
        return discreteSeconds;
    }

    public WidgetProperty<WidgetColor> propHourColor ( ) {
        return hourColor;
    }

    public WidgetProperty<WidgetColor> propHourTickMarkColor ( ) {
        return hourTickMarkColor;
    }

    public WidgetProperty<Boolean> propHourTickMarkVisible ( ) {
        return hourTickMarkVisible;
    }

    public WidgetProperty<WidgetColor> propKnobColor ( ) {
        return knobColor;
    }

    public WidgetProperty<WidgetColor> propMinuteColor ( ) {
        return minuteColor;
    }

    public WidgetProperty<WidgetColor> propMinuteTickMarkColor ( ) {
        return minuteTickMarkColor;
    }

    public WidgetProperty<Boolean> propMinuteTickMarkVisible ( ) {
        return minuteTickMarkVisible;
    }

    public WidgetProperty<Boolean> propRunning ( ) {
        return running;
    }

    public WidgetProperty<WidgetColor> propSecondColor ( ) {
        return secondColor;
    }

    public WidgetProperty<Boolean> propSecondVisible ( ) {
        return secondVisible;
    }

    public WidgetProperty<Skin> propSkin ( ) {
        return skin;
    }

    public WidgetProperty<Boolean> propTransparent ( ) {
        return transparent;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        properties.add(skin = propSkin.createProperty(this, Skin.PLAIN));

        properties.add(borderWidth           = propBorderWidth.createProperty(this, 4.7));
        properties.add(borderColor           = propBorderColor.createProperty(this, new WidgetColor(153, 230, 230)));
        properties.add(dateVisible           = propDateVisible.createProperty(this, false));
        properties.add(dateColor             = propDateColor.createProperty(this, new WidgetColor(102, 51, 102)));
        properties.add(hourColor             = propHourColor.createProperty(this, new WidgetColor(255, 127, 80)));
        properties.add(hourTickMarkVisible   = propHourTickMarkVisible.createProperty(this, true));
        properties.add(hourTickMarkColor     = propHourTickMarkColor.createProperty(this, new WidgetColor(196, 127, 80)));
        properties.add(knobColor             = propKnobColor.createProperty(this, new WidgetColor(196, 127, 80)));
        properties.add(minuteColor           = propMinuteColor.createProperty(this, new WidgetColor(255, 136, 98)));
        properties.add(minuteTickMarkVisible = propMinuteTickMarkVisible.createProperty(this, true));
        properties.add(minuteTickMarkColor   = propMinuteTickMarkColor.createProperty(this, new WidgetColor(196, 136, 98)));
        properties.add(secondVisible         = propSecondVisible.createProperty(this, true));
        properties.add(secondColor           = propSecondColor.createProperty(this, new WidgetColor(98, 196, 136)));
        properties.add(background            = propBackgroundColor.createProperty(this, new WidgetColor(230, 230, 153)));
        properties.add(transparent           = propTransparent.createProperty(this, false));

        properties.add(discreteHours = propDiscreteHours.createProperty(this, false));
        properties.add(discreteMinutes = propDiscreteMinutes.createProperty(this, false));
        properties.add(discreteSeconds = propDiscreteSeconds.createProperty(this, false));

        //  Properties not visible in the property sheet.
        running = propRunning.createProperty(this, true);

    }

}
