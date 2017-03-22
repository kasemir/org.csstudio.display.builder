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
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newStringPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propEnabled;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propLimitsFromPV;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propMaximum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propMinimum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propPrecision;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propTransparent;

import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.WidgetColor;

/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 8 Feb 2017
 */
public abstract class BaseGaugeWidget extends PVWidget {

    public static final WidgetPropertyDescriptor<Boolean>     propUnitFromPV   = newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "unit_from_pv",  Messages.WidgetProperties_UnitFromPV);

    public static final WidgetPropertyDescriptor<Double>      propLevelHiHi    = newDoublePropertyDescriptor (WidgetPropertyCategory.DISPLAY,  "level_hihi",    Messages.WidgetProperties_LevelHiHi);
    public static final WidgetPropertyDescriptor<Double>      propLevelHigh    = newDoublePropertyDescriptor (WidgetPropertyCategory.DISPLAY,  "level_high",    Messages.WidgetProperties_LevelHigh);
    public static final WidgetPropertyDescriptor<Double>      propLevelLoLo    = newDoublePropertyDescriptor (WidgetPropertyCategory.DISPLAY,  "level_lolo",    Messages.WidgetProperties_LevelLoLo);
    public static final WidgetPropertyDescriptor<Double>      propLevelLow     = newDoublePropertyDescriptor (WidgetPropertyCategory.DISPLAY,  "level_low",     Messages.WidgetProperties_LevelLow);
    public static final WidgetPropertyDescriptor<Boolean>     propShowHiHi     = newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY,  "show_hihi",     Messages.WidgetProperties_ShowHiHi);
    public static final WidgetPropertyDescriptor<Boolean>     propShowHigh     = newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY,  "show_high",     Messages.WidgetProperties_ShowHigh);
    public static final WidgetPropertyDescriptor<Boolean>     propShowLoLo     = newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY,  "show_lolo",     Messages.WidgetProperties_ShowLoLo);
    public static final WidgetPropertyDescriptor<Boolean>     propShowLow      = newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY,  "show_low",      Messages.WidgetProperties_ShowLow);
    public static final WidgetPropertyDescriptor<String>      propTitle        = newStringPropertyDescriptor (WidgetPropertyCategory.DISPLAY,  "title",         Messages.WidgetProperties_Title);

    public static final WidgetPropertyDescriptor<WidgetColor> propTitleColor   = newColorPropertyDescriptor  (WidgetPropertyCategory.MISC,     "title_color",   Messages.WidgetProperties_TitleColor);
    public static final WidgetPropertyDescriptor<String>      propUnit         = newStringPropertyDescriptor (WidgetPropertyCategory.MISC,     "unit",          Messages.WidgetProperties_Unit);
    public static final WidgetPropertyDescriptor<WidgetColor> propUnitColor    = newColorPropertyDescriptor  (WidgetPropertyCategory.MISC,     "unit_color",    Messages.WidgetProperties_UnitColor);
    public static final WidgetPropertyDescriptor<WidgetColor> propValueColor   = newColorPropertyDescriptor  (WidgetPropertyCategory.MISC,     "value_color",   Messages.WidgetProperties_ValueColor);
    public static final WidgetPropertyDescriptor<Boolean>     propValueVisible = newBooleanPropertyDescriptor(WidgetPropertyCategory.MISC,     "value_visible", Messages.WidgetProperties_ValueVisible);

    private volatile WidgetProperty<WidgetColor> background_color;
    private volatile WidgetProperty<Boolean>     enabled;
    private volatile WidgetProperty<Double>      level_high;
    private volatile WidgetProperty<Double>      level_hihi;
    private volatile WidgetProperty<Double>      level_lolo;
    private volatile WidgetProperty<Double>      level_low;
    private volatile WidgetProperty<Boolean>     limits_from_pv;
    private volatile WidgetProperty<Double>      maximum;
    private volatile WidgetProperty<Double>      minimum;
    private volatile WidgetProperty<Integer>     precision;
    private volatile WidgetProperty<Boolean>     show_high;
    private volatile WidgetProperty<Boolean>     show_hihi;
    private volatile WidgetProperty<Boolean>     show_lolo;
    private volatile WidgetProperty<Boolean>     show_low;
    private volatile WidgetProperty<String>      title;
    private volatile WidgetProperty<WidgetColor> title_color;
    private volatile WidgetProperty<Boolean>     transparent;
    private volatile WidgetProperty<String>      unit;
    private volatile WidgetProperty<WidgetColor> unit_color;
    private volatile WidgetProperty<Boolean>     unit_from_pv;
    private volatile WidgetProperty<WidgetColor> value_color;
    private volatile WidgetProperty<Boolean>     value_visible;

    /**
     * @param type Widget type.
     * @param default_width Default widget width.
     * @param default_height Default widget height.
     */
    public BaseGaugeWidget ( final String type, final int default_width, final int default_height ) {
        super(type, default_width, default_height);
    }

    public WidgetProperty<WidgetColor> propBackgroundColor ( ) {
        return background_color;
    }

    public WidgetProperty<Boolean> propEnabled ( ) {
        return enabled;
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

    public WidgetProperty<Integer> propPrecision ( ) {
        return precision;
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

    public WidgetProperty<String> propTitle ( ) {
        return title;
    }

    public WidgetProperty<WidgetColor> propTitleColor ( ) {
        return title_color;
    }

    public WidgetProperty<Boolean> propTransparent ( ) {
        return transparent;
    }

    public WidgetProperty<String> propUnit ( ) {
        return unit;
    }

    public WidgetProperty<WidgetColor> propUnitColor ( ) {
        return unit_color;
    }

    public WidgetProperty<Boolean> propUnitFromPV ( ) {
        return unit_from_pv;
    }

    public WidgetProperty<WidgetColor> propValueColor ( ) {
        return value_color;
    }

    public WidgetProperty<Boolean> propValueVisible ( ) {
        return value_visible;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        properties.add(background_color = propBackgroundColor.createProperty(this, new WidgetColor(255, 254, 253)));
        properties.add(transparent      = propTransparent.createProperty(this, true));

        properties.add(precision        = propPrecision.createProperty(this, -1));
        properties.add(level_hihi       = propLevelHiHi.createProperty(this, 90.0));
        properties.add(level_high       = propLevelHigh.createProperty(this, 80.0));
        properties.add(level_low        = propLevelLow.createProperty(this, 20.0));
        properties.add(level_lolo       = propLevelLoLo.createProperty(this, 10.0));
        properties.add(show_hihi        = propShowHiHi.createProperty(this, true));
        properties.add(show_high        = propShowHigh.createProperty(this, true));
        properties.add(show_low         = propShowLow.createProperty(this, true));
        properties.add(show_lolo        = propShowLoLo.createProperty(this, true));
        properties.add(title            = propTitle.createProperty(this, ""));

        properties.add(minimum          = propMinimum.createProperty(this, 0.0));
        properties.add(maximum          = propMaximum.createProperty(this, 100.0));
        properties.add(limits_from_pv   = propLimitsFromPV.createProperty(this, true));
        properties.add(unit_from_pv     = propUnitFromPV.createProperty(this, true));
        properties.add(enabled          = propEnabled.createProperty(this, true));

        properties.add(title_color      = propTitleColor.createProperty(this, new WidgetColor(7, 5, 3)));
        properties.add(unit             = propUnit.createProperty(this, ""));
        properties.add(unit_color       = propUnitColor.createProperty(this, new WidgetColor(7, 5, 3)));
        properties.add(value_color      = propValueColor.createProperty(this, new WidgetColor(10, 180, 140)));
        properties.add(value_visible    = propValueVisible.createProperty(this, true));

    }

}
