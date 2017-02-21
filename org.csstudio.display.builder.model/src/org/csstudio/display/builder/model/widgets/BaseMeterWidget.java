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

import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;


/**
 * Widget displaying and editing a numeric PV value.
 *
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 20 Feb 2017
 */
public abstract class BaseMeterWidget extends BaseGaugeWidget {

    public enum LCDDesign {
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

    public static final WidgetPropertyDescriptor<Boolean>   propHighlightZones = newBooleanPropertyDescriptor           (WidgetPropertyCategory.BEHAVIOR, "highligh_zones", Messages.WidgetProperties_HighlightZones);
    public static final WidgetPropertyDescriptor<LCDDesign> propLcdDesign      = new WidgetPropertyDescriptor<LCDDesign>(WidgetPropertyCategory.MISC,     "lcd_design",     Messages.WidgetProperties_LcdDesign) {
        @Override
        public EnumWidgetProperty<LCDDesign> createProperty ( Widget widget, LCDDesign defaultValue ) {
            return new EnumWidgetProperty<>(this, widget, defaultValue);
        }
    };
    public static final WidgetPropertyDescriptor<Boolean>   propLcdVisible     = newBooleanPropertyDescriptor           (WidgetPropertyCategory.MISC,     "lcd_visible",    Messages.WidgetProperties_LcdVisible);

    private volatile WidgetProperty<Boolean>   highligh_zones;
    private volatile WidgetProperty<LCDDesign> lcdDesign;
    private volatile WidgetProperty<Boolean>   lcdVisible;

    /**
     * @param type Widget type.
     * @param default_width Default widget width.
     * @param default_height Default widget height.
     */
    public BaseMeterWidget ( final String type, final int default_width, final int default_height ) {
        super(type, default_width, default_height);
    }

    public WidgetProperty<Boolean> propHighlightZones ( ) {
        return highligh_zones;
    }

    public WidgetProperty<LCDDesign> propLcdDesign ( ) {
        return lcdDesign;
    }

    public WidgetProperty<Boolean> propLcdVisible ( ) {
        return lcdVisible;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        properties.add(highligh_zones = propHighlightZones.createProperty(this, true));

        properties.add(lcdDesign      = propLcdDesign.createProperty(this, LCDDesign.SECTIONS));
        properties.add(lcdVisible     = propLcdVisible.createProperty(this, true));

    }

}
