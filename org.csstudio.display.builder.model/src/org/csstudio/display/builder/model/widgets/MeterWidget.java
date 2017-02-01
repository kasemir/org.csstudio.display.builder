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
        BATTERY,
        BULLET_CHART,
        CHARGE,
        DASHBOARD,
        HORIZONTAL,
        INDICATOR,
        KPI,
        LEVEL,
        LINEAR,
        QUARTER,
        TILE_KPI,
        TILE_SPARK_LINE,
        TILE_TEXT_KPI,
        VERTICAL
    }

    public static final WidgetPropertyDescriptor<Skin>        propSkin       = new WidgetPropertyDescriptor<Skin>                 (WidgetPropertyCategory.WIDGET,   "skin",        Messages.WidgetProperties_Skin) {
        @Override
        public EnumWidgetProperty<Skin> createProperty ( Widget widget, Skin defaultValue ) {
            return new EnumWidgetProperty<>(this, widget, defaultValue);
        }
    };

    public static final WidgetPropertyDescriptor<String>      propTitle      = CommonWidgetProperties.newStringPropertyDescriptor (WidgetPropertyCategory.DISPLAY,  "title",       Messages.WidgetProperties_Title);

    public static final WidgetPropertyDescriptor<Boolean>     propAnimated   = CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "animated",    Messages.WidgetProperties_Animated);

    public static final WidgetPropertyDescriptor<WidgetColor> propTitleColor = CommonWidgetProperties.newColorPropertyDescriptor  (WidgetPropertyCategory.DISPLAY,  "title_color", Messages.WidgetProperties_TitleColor);

    private volatile WidgetProperty<Boolean>     animated;
    private volatile WidgetProperty<Skin>        skin;
    private volatile WidgetProperty<String>      title;
    private volatile WidgetProperty<WidgetColor> titleColor;

    public MeterWidget ( ) {
        super(WIDGET_DESCRIPTOR.getType(), 240, 120);
    }

    public WidgetProperty<Boolean> propAnimated ( ) {
        return animated;
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

        properties.add(skin       = propSkin.createProperty(this, Skin.HORIZONTAL));

        properties.add(title      = propTitle.createProperty(this, ""));

        properties.add(animated   = propAnimated.createProperty(this, true));

        properties.add(titleColor = propTitleColor.createProperty(this, new WidgetColor(136, 196, 136)));

    }

}