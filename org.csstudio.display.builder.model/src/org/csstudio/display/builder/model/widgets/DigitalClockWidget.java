/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.model.widgets;


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

import eu.hansolo.medusa.LcdDesign;


/**
 * Widget displaying date and/or time.
 *
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 23 Jan 2017
 */
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

    public static final WidgetPropertyDescriptor<Boolean>   propDateVisible       = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY,  "date_visible",        Messages.WidgetProperties_DateVisible);
    public static final WidgetPropertyDescriptor<Boolean>   propLcdCrystalEnabled = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY,  "lcd_crystal_enabled", Messages.WidgetProperties_LcdCrystalEnabled);
    public static final WidgetPropertyDescriptor<LcdDesign> propLcdDesign         = new WidgetPropertyDescriptor<LcdDesign>            (WidgetPropertyCategory.WIDGET,   "lcd_design",          Messages.WidgetProperties_LcdDesign) {
        @Override
        public EnumWidgetProperty<LcdDesign> createProperty ( Widget widget, LcdDesign defaultValue ) {
            return new EnumWidgetProperty<>(this, widget, defaultValue);
        }
    };
    public static final WidgetPropertyDescriptor<Boolean>   propRunning           = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "running",             Messages.WidgetProperties_Running);
    public static final WidgetPropertyDescriptor<Boolean>   propSecondVisible     = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY,  "second_visible",      Messages.WidgetProperties_SecondVisible);
    public static final WidgetPropertyDescriptor<Boolean>   propShadowsEnabled    = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY,  "shadows_enabled",     Messages.WidgetProperties_ShadowsEnabled);
    public static final WidgetPropertyDescriptor<String>    propTitle             = CommonWidgetProperties.newStringPropertyDescriptor (WidgetPropertyCategory.DISPLAY,  "title",               Messages.WidgetProperties_Title);
    public static final WidgetPropertyDescriptor<Boolean>   propTitleVisible      = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY,  "title_visible",       Messages.WidgetProperties_TitleVisible);

    private volatile WidgetProperty<Boolean>     dateVisible;
    private volatile WidgetProperty<Boolean>     lcdCrystalEnabled;
    private volatile WidgetProperty<LcdDesign>   lcdDesign;
    private volatile WidgetProperty<Boolean>     running;
    private volatile WidgetProperty<Boolean>     secondVisible;
    private volatile WidgetProperty<Boolean>     shadowsEnabled;
    private volatile WidgetProperty<String>      title;
    private volatile WidgetProperty<Boolean>     titleVisible;

    public DigitalClockWidget ( ) {
        super(WIDGET_DESCRIPTOR.getType(), 170, 90);
    }

    public WidgetProperty<Boolean> propDateVisible ( ) {
        return dateVisible;
    }

    public WidgetProperty<Boolean> propLcdCrystalEnabled ( ) {
        return lcdCrystalEnabled;
    }

    public WidgetProperty<LcdDesign> propLcdDesign ( ) {
        return lcdDesign;
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

        properties.add(lcdDesign         = propLcdDesign.createProperty(this, LcdDesign.AMBER));

        properties.add(lcdCrystalEnabled = propLcdCrystalEnabled.createProperty(this, true));
        properties.add(dateVisible       = propDateVisible.createProperty(this, true));
        properties.add(secondVisible     = propSecondVisible.createProperty(this, true));
        properties.add(shadowsEnabled    = propShadowsEnabled.createProperty(this, true));
        properties.add(title             = propTitle.createProperty(this, ""));
        properties.add(titleVisible      = propTitleVisible.createProperty(this, false));

        //  Properties not visible in the property sheet.
        running = propRunning.createProperty(this, true);

    }

}
