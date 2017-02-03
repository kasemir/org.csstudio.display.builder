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
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newStringPropertyDescriptor;

import java.util.List;
import java.util.Locale;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;


/**
 * Widget displaying date and/or time.
 *
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 23 Jan 2017
 */
@SuppressWarnings("nls")
public class DigitalClockWidget extends VisibleWidget {

    public static final WidgetDescriptor WIDGET_DESCRIPTOR = new WidgetDescriptor(
        "digital_clock",
        WidgetCategory.MISC,
        "DigitalClock",
        "platform:/plugin/org.csstudio.display.builder.model/icons/digital-clock.png",
        "Digital clock"
    ) {
        @Override
        public Widget createWidget ( ) {
            return new DigitalClockWidget();
        }
    };

    public enum Design {
        AMBER,
        BEIGE,
        BLACK,
        BLACK_RED,
        BLACK_YELLOW,
        BLUE,
        BLUE2,
        BLUE_BLACK,
        BLUE_BLUE,
        BLUE_DARKBLUE,
        BLUE_GRAY,
        BLUE_LIGHTBLUE,
        BLUE_LIGHTBLUE2,
        DARKAMBER,
        DARKBLUE,
        DARKGREEN,
        DARKPURPLE,
        GRAY,
        GRAY_PURPLE,
        GREEN,
        GREEN_BLACK,
        GREEN_DARKGREEN,
        LIGHTBLUE,
        LIGHTGREEN,
        LIGHTGREEN_BLACK,
        ORANGE,
        PURPLE,
        RED,
        RED_DARKRED,
        SECTIONS,
        STANDARD,
        STANDARD_GREEN,
        WHITE,
        YELLOW,
        YELLOW_BLACK,
        YOCTOPUCE
    }

    public static final WidgetPropertyDescriptor<Design>  propLcdDesign         = new WidgetPropertyDescriptor<Design>(WidgetPropertyCategory.WIDGET,   "lcd_design",          Messages.WidgetProperties_LcdDesign) {
        @Override
        public EnumWidgetProperty<Design> createProperty ( Widget widget, Design defaultValue ) {
            return new EnumWidgetProperty<>(this, widget, defaultValue);
        }
    };

    public static final WidgetPropertyDescriptor<Boolean> propDateVisible       = newBooleanPropertyDescriptor        (WidgetPropertyCategory.DISPLAY,  "date_visible",        Messages.WidgetProperties_DateVisible);
    public static final WidgetPropertyDescriptor<Boolean> propSecondVisible     = newBooleanPropertyDescriptor        (WidgetPropertyCategory.DISPLAY,  "second_visible",      Messages.WidgetProperties_SecondVisible);
    public static final WidgetPropertyDescriptor<String>  propTitle             = newStringPropertyDescriptor         (WidgetPropertyCategory.DISPLAY,  "title",               Messages.WidgetProperties_Title);
    public static final WidgetPropertyDescriptor<Boolean> propTitleVisible      = newBooleanPropertyDescriptor        (WidgetPropertyCategory.DISPLAY,  "title_visible",       Messages.WidgetProperties_TitleVisible);

    public static final WidgetPropertyDescriptor<Boolean> propRunning           = newBooleanPropertyDescriptor        (WidgetPropertyCategory.BEHAVIOR, "running",             Messages.WidgetProperties_Running);

    public static final WidgetPropertyDescriptor<Boolean> propLcdCrystalEnabled = newBooleanPropertyDescriptor        (WidgetPropertyCategory.MISC,     "lcd_crystal_enabled", Messages.WidgetProperties_LcdCrystalEnabled);
    public static final WidgetPropertyDescriptor<String>  propLocale            = newStringPropertyDescriptor         (WidgetPropertyCategory.MISC,     "locale",              Messages.WidgetProperties_Locale);
    public static final WidgetPropertyDescriptor<Boolean> propShadowsEnabled    = newBooleanPropertyDescriptor        (WidgetPropertyCategory.MISC,     "shadows_enabled",     Messages.WidgetProperties_ShadowsEnabled);

    private volatile WidgetProperty<Boolean> dateVisible;
    private volatile WidgetProperty<Boolean> lcdCrystalEnabled;
    private volatile WidgetProperty<Design>  lcdDesign;
    private volatile WidgetProperty<String>  locale;
    private volatile WidgetProperty<Boolean> running;
    private volatile WidgetProperty<Boolean> secondVisible;
    private volatile WidgetProperty<Boolean> shadowsEnabled;
    private volatile WidgetProperty<String>  title;
    private volatile WidgetProperty<Boolean> titleVisible;

    public DigitalClockWidget ( ) {
        super(WIDGET_DESCRIPTOR.getType(), 170, 90);
    }

    public WidgetProperty<Boolean> propDateVisible ( ) {
        return dateVisible;
    }

    public WidgetProperty<Boolean> propLcdCrystalEnabled ( ) {
        return lcdCrystalEnabled;
    }

    public WidgetProperty<Design> propLcdDesign ( ) {
        return lcdDesign;
    }

    public WidgetProperty<String> propLocale ( ) {
        return locale;
    }

    public WidgetProperty<Boolean> propRunning ( ) {
        return running;
    }

    public WidgetProperty<Boolean> propSecondVisible ( ) {
        return secondVisible;
    }

    public WidgetProperty<Boolean> propShadowsEnabled ( ) {
        return shadowsEnabled;
    }

    public WidgetProperty<String> propTitle ( ) {
        return title;
    }

    public WidgetProperty<Boolean> propTitleVisible ( ) {
        return titleVisible;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        properties.add(lcdDesign         = propLcdDesign.createProperty(this, Design.SECTIONS));

        properties.add(dateVisible       = propDateVisible.createProperty(this, true));
        properties.add(secondVisible     = propSecondVisible.createProperty(this, true));
        properties.add(title             = propTitle.createProperty(this, ""));
        properties.add(titleVisible      = propTitleVisible.createProperty(this, false));

        properties.add(lcdCrystalEnabled = propLcdCrystalEnabled.createProperty(this, true));
        properties.add(locale            = propLocale.createProperty(this, Locale.getDefault().toLanguageTag()));
        properties.add(shadowsEnabled    = propShadowsEnabled.createProperty(this, true));

        //  Properties not visible in the property sheet.
        running = propRunning.createProperty(this, true);

    }

}
