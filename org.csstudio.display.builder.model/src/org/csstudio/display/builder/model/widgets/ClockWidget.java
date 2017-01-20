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

    public static final WidgetPropertyDescriptor<WidgetColor> propBorderColor = CommonWidgetProperties.newColorPropertyDescriptor  (WidgetPropertyCategory.DISPLAY,  "border_color", Messages.WidgetProperties_BorderColor);
    public static final WidgetPropertyDescriptor<Double>      propBorderWidth = CommonWidgetProperties.newDoublePropertyDescriptor (WidgetPropertyCategory.DISPLAY,  "border_width", Messages.WidgetProperties_BorderWidth);
    public static final WidgetPropertyDescriptor<WidgetColor> propDateColor   = CommonWidgetProperties.newColorPropertyDescriptor  (WidgetPropertyCategory.DISPLAY,  "date_color",   Messages.WidgetProperties_DateColor);
    public static final WidgetPropertyDescriptor<Boolean>     propDateVisible = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "date_visible", Messages.WidgetProperties_DateVisible);
    public static final WidgetPropertyDescriptor<Boolean>     propRunning     = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "running",      Messages.WidgetProperties_Running);
    public static final WidgetPropertyDescriptor<Skin>        propSkin        = new WidgetPropertyDescriptor<Skin>                 (WidgetPropertyCategory.WIDGET,   "skin",         Messages.WidgetProperties_Skin) {
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
    private volatile WidgetProperty<Boolean>     running;
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

    public WidgetProperty<Boolean> propRunning ( ) {
        return running;
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

        properties.add(skin        = propSkin.createProperty(this, Skin.PLAIN));
        properties.add(borderWidth = propBorderWidth.createProperty(this, 4.7));
        properties.add(borderColor = propBorderColor.createProperty(this, new WidgetColor(153, 230, 230)));
        properties.add(dateVisible = propDateVisible.createProperty(this, false));
        properties.add(dateColor   = propBorderColor.createProperty(this, new WidgetColor(230, 153, 230)));
        properties.add(background  = propBackgroundColor.createProperty(this, new WidgetColor(230, 230, 153)));
        properties.add(transparent = propTransparent.createProperty(this, false));

        //  Properties not visible in the property sheet.
        running = propRunning.createProperty(this, true);

    }

}
