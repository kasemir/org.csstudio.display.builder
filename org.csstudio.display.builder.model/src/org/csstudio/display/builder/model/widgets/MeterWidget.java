/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.model.widgets;


import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newColorPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newDoublePropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newLongPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newStringPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propLimitsFromPV;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propMaximum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propMinimum;

import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;


/**
 * Widget displaying and editing a numeric PV value.
 *
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 25 Jan 2017
 */
public class MeterWidget extends PVWidget {

    public static final WidgetDescriptor WIDGET_DESCRIPTOR = new WidgetDescriptor(
        "meter",
        WidgetCategory.CONTROL,
        "Meter",
        "platform:/plugin/org.csstudio.display.builder.model/icons/meter.png",
        "Meter that can read/write a numeric PV"
    ) {
        @Override
        public Widget createWidget ( ) {
            return new MeterWidget();
        }
    };

    /**
     * This enumeration is used to reduce the choices from the original enumeration.
     *
     * @author claudiorosati, European Spallation Source ERIC
     * @version 1.0.0 25 Jan 2017
     */
    public enum Skin {
        AMP,
        GAUGE,
        HORIZONTAL,
        LINEAR_H,
        LINEAR_V,
        QUARTER,
        THREE_QUARTERS,
        VERTICAL
    }

    public static final WidgetPropertyDescriptor<Skin>        propSkin           = new WidgetPropertyDescriptor<Skin>(WidgetPropertyCategory.WIDGET,   "skin",           Messages.WidgetProperties_Skin) {
        @Override
        public EnumWidgetProperty<Skin> createProperty ( Widget widget, Skin defaultValue ) {
            return new EnumWidgetProperty<>(this, widget, defaultValue);
        }
    };

    public static final WidgetPropertyDescriptor<Double>      propLevelHiHi         = newDoublePropertyDescriptor    (WidgetPropertyCategory.DISPLAY,  "level_hihi",         Messages.WidgetProperties_LevelHiHi);
    public static final WidgetPropertyDescriptor<Double>      propLevelHigh         = newDoublePropertyDescriptor    (WidgetPropertyCategory.DISPLAY,  "level_high",         Messages.WidgetProperties_LevelHigh);
    public static final WidgetPropertyDescriptor<Double>      propLevelLoLo         = newDoublePropertyDescriptor    (WidgetPropertyCategory.DISPLAY,  "level_lolo",         Messages.WidgetProperties_LevelLoLo);
    public static final WidgetPropertyDescriptor<Double>      propLevelLow          = newDoublePropertyDescriptor    (WidgetPropertyCategory.DISPLAY,  "level_low",          Messages.WidgetProperties_LevelLow);
    public static final WidgetPropertyDescriptor<Boolean>     propShowHiHi          = newBooleanPropertyDescriptor   (WidgetPropertyCategory.DISPLAY,  "show_hihi",          Messages.WidgetProperties_ShowHiHi);
    public static final WidgetPropertyDescriptor<Boolean>     propShowHigh          = newBooleanPropertyDescriptor   (WidgetPropertyCategory.DISPLAY,  "show_high",          Messages.WidgetProperties_ShowHigh);
    public static final WidgetPropertyDescriptor<Boolean>     propShowLoLo          = newBooleanPropertyDescriptor   (WidgetPropertyCategory.DISPLAY,  "show_lolo",          Messages.WidgetProperties_ShowLoLo);
    public static final WidgetPropertyDescriptor<Boolean>     propShowLow           = newBooleanPropertyDescriptor   (WidgetPropertyCategory.DISPLAY,  "show_low",           Messages.WidgetProperties_ShowLow);
    public static final WidgetPropertyDescriptor<Boolean>     propHighlightZones    = newBooleanPropertyDescriptor   (WidgetPropertyCategory.DISPLAY,  "highligh_zones",     Messages.WidgetProperties_HighlightZones);
    public static final WidgetPropertyDescriptor<String>      propTitle             = newStringPropertyDescriptor    (WidgetPropertyCategory.DISPLAY,  "title",              Messages.WidgetProperties_Title);

    public static final WidgetPropertyDescriptor<Boolean>     propAnimated          = newBooleanPropertyDescriptor   (WidgetPropertyCategory.BEHAVIOR, "animated",           Messages.WidgetProperties_Animated);
    public static final WidgetPropertyDescriptor<Long>        propAnimationDuration = newLongPropertyDescriptor      (WidgetPropertyCategory.BEHAVIOR, "animation_duration", Messages.WidgetProperties_AnimationDuration, 10L, 10000L);

    public static final WidgetPropertyDescriptor<WidgetColor> propTitleColor        = newColorPropertyDescriptor     (WidgetPropertyCategory.DISPLAY,  "title_color",        Messages.WidgetProperties_TitleColor);

    private volatile WidgetProperty<Boolean>     animated;
    private volatile WidgetProperty<Long>        animation_duration;
    private volatile WidgetProperty<Boolean>     highligh_zones;
    private volatile WidgetProperty<Double>      level_high;
    private volatile WidgetProperty<Double>      level_hihi;
    private volatile WidgetProperty<Double>      level_lolo;
    private volatile WidgetProperty<Double>      level_low;
    private volatile WidgetProperty<Boolean>     limits_from_pv;
    private volatile WidgetProperty<Double>      maximum;
    private volatile WidgetProperty<Double>      minimum;
    private volatile WidgetProperty<Boolean>     show_high;
    private volatile WidgetProperty<Boolean>     show_hihi;
    private volatile WidgetProperty<Boolean>     show_lolo;
    private volatile WidgetProperty<Boolean>     show_low;
    private volatile WidgetProperty<Skin>        skin;
    private volatile WidgetProperty<String>      title;
    private volatile WidgetProperty<WidgetColor> titleColor;

    public MeterWidget ( ) {
        super(WIDGET_DESCRIPTOR.getType(), 240, 120);
    }

    public WidgetProperty<Boolean> propAnimated ( ) {
        return animated;
    }

    public WidgetProperty<Long> propAnimationDuration ( ) {
        return animation_duration;
    }

    public WidgetProperty<Boolean> propHighlightZones ( ) {
        return highligh_zones;
    }

    public WidgetProperty<Double> propLevelHiHi ( ) {
        return level_hihi;
    }

    public WidgetProperty<Double> propLevelHight ( ) {
        return level_high;
    }

    public WidgetProperty<Double> propLevelLoLo ( ) {
        return level_lolo;
    }

    public WidgetProperty<Double> propLevelLow ( ) {
        return level_low;
    }

    public WidgetProperty<Boolean> propLimitsFromPV ( ) {
        return limits_from_pv;
    }

    public WidgetProperty<Double> propMaximum ( ) {
        return maximum;
    }

    public WidgetProperty<Double> propMinimum ( ) {
        return minimum;
    }

    public WidgetProperty<Boolean> propShowHiHi ( ) {
        return show_hihi;
    }

    public WidgetProperty<Boolean> propShowHigh ( ) {
        return show_high;
    }

    public WidgetProperty<Boolean> propShowLoLo ( ) {
        return show_lolo;
    }

    public WidgetProperty<Boolean> propShowLow ( ) {
        return show_low;
    }

    public WidgetProperty<Skin> propSkin ( ) {
        return skin;
    }

    public WidgetProperty<String> propTitle ( ) {
        return title;
    }

    public WidgetProperty<WidgetColor> propTitleColor ( ) {
        return titleColor;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        properties.add(skin               = propSkin.createProperty(this, Skin.HORIZONTAL));

        properties.add(level_hihi         = propLevelHiHi.createProperty(this, 90.0));
        properties.add(level_high         = propLevelHigh.createProperty(this, 80.0));
        properties.add(level_low          = propLevelLow.createProperty(this, 20.0));
        properties.add(level_lolo         = propLevelLoLo.createProperty(this, 10.0));
        properties.add(show_hihi          = propShowHiHi.createProperty(this, true));
        properties.add(show_high          = propShowHigh.createProperty(this, true));
        properties.add(show_low           = propShowLow.createProperty(this, true));
        properties.add(show_lolo          = propShowLoLo.createProperty(this, true));
        properties.add(highligh_zones     = propHighlightZones.createProperty(this, true));
        properties.add(title              = propTitle.createProperty(this, ""));

        properties.add(minimum            = propMinimum.createProperty(this, 0.0));
        properties.add(maximum            = propMaximum.createProperty(this, 100.0));
        properties.add(limits_from_pv     = propLimitsFromPV.createProperty(this, true));
        properties.add(animated           = propAnimated.createProperty(this, false));
        properties.add(animation_duration = propAnimationDuration.createProperty(this, 800L));

        properties.add(titleColor         = propTitleColor.createProperty(this, new WidgetColor(136, 196, 136)));

    }

}
