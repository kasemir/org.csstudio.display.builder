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
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;

import eu.hansolo.medusa.Gauge;


/**
 * Widget displaying and editing a numeric PV value.
 *
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 25 Jan 2017
 */
public class GaugeWidget extends VisibleWidget {

    public static final WidgetDescriptor WIDGET_DESCRIPTOR = new WidgetDescriptor(
        "gauge",
        WidgetCategory.CONTROL,
        "Gauge",
        "platform:/plugin/org.csstudio.display.builder.model/icons/gauge.png",
        "Gauge that can read/write a numeric PV"
    ) {
        @Override
        public Widget createWidget ( ) {
            return new GaugeWidget();
        }
    };

    /**
     * This enumeration is used to reduce the choices from the original enumeration.
     *
     * @author claudiorosati, European Spallation Source ERIC
     * @version 1.0.0 25 Jan 2017
     */
    public enum Skin {

        BAR(Gauge.SkinType.BAR),
        DIGITAL(Gauge.SkinType.DIGITAL),
        FLAT(Gauge.SkinType.FLAT),
        GAUGE(Gauge.SkinType.GAUGE),
        MODERN(Gauge.SkinType.MODERN),
        SECTION(Gauge.SkinType.SECTION),
        SIMPLE(Gauge.SkinType.SIMPLE),
        SIMPLE_DIGITAL(Gauge.SkinType.SIMPLE_DIGITAL),
        SIMPLE_SECTION(Gauge.SkinType.SIMPLE_SECTION),
        SLIM(Gauge.SkinType.SLIM),
        SPACE_X(Gauge.SkinType.SPACE_X),
        TINY(Gauge.SkinType.TINY);

        private final Gauge.SkinType skinType;

        Skin ( Gauge.SkinType skinType ) {
            this.skinType = skinType;
        }

        public Gauge.SkinType skinType ( ) {
            return skinType;
        }

    }

    public static final WidgetPropertyDescriptor<Skin>         propSkin                  = new WidgetPropertyDescriptor<Skin>                 (WidgetPropertyCategory.WIDGET,   "skin",                     Messages.WidgetProperties_Skin) {
        @Override
        public EnumWidgetProperty<Skin> createProperty ( Widget widget, Skin defaultValue ) {
            return new EnumWidgetProperty<>(this, widget, defaultValue);
        }
    };

    private volatile WidgetProperty<Skin> skin;

    public GaugeWidget ( ) {
        super(WIDGET_DESCRIPTOR.getType(), 120, 120);
    }

    public WidgetProperty<Skin> propSkin ( ) {
        return skin;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        properties.add(skin                  = propSkin.createProperty(this, Skin.SIMPLE_SECTION));

    }

}
