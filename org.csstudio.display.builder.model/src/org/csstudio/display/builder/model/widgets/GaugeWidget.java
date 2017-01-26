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
import eu.hansolo.medusa.GaugeDesign;


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
     * This enumeration is a trick to have the original enumeration sorted.
     * This because the combo box showing the values is not made to sort the values.
     *
     * @author claudiorosati, European Spallation Source ERIC
     * @version 1.0.0 25 Jan 2017
     */
    public enum BorderDesign {

        BLACK_METAL(GaugeDesign.BLACK_METAL),
        BRASS(GaugeDesign.BRASS),
        ENZO(GaugeDesign.ENZO),
        FLAT(GaugeDesign.FLAT),
        GOLD(GaugeDesign.GOLD),
        METAL(GaugeDesign.METAL),
        NONE(GaugeDesign.NONE),
        STEEL(GaugeDesign.STEEL),
        TILTED_BLACK(GaugeDesign.TILTED_BLACK),
        TRANSPARENT(GaugeDesign.TRANSPARENT);

        private final GaugeDesign design;

        BorderDesign ( GaugeDesign design ) {
            this.design = design;
        }

        public GaugeDesign design ( ) {
            return design;
        }

    }

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
        TINY(Gauge.SkinType.TINY);

        private final Gauge.SkinType skinType;

        Skin ( Gauge.SkinType skinType ) {
            this.skinType = skinType;
        }

        public Gauge.SkinType skinType ( ) {
            return skinType;
        }

    }

    public static final WidgetPropertyDescriptor<BorderDesign> propBorderDesign          = new WidgetPropertyDescriptor<BorderDesign>         (WidgetPropertyCategory.WIDGET,   "border_design",            Messages.WidgetProperties_BorderDesign) {
        @Override
        public EnumWidgetProperty<BorderDesign> createProperty ( Widget widget, BorderDesign defaultValue ) {
            return new EnumWidgetProperty<>(this, widget, defaultValue);
        }
    };
    public static final WidgetPropertyDescriptor<Skin>         propSkin                  = new WidgetPropertyDescriptor<Skin>                 (WidgetPropertyCategory.WIDGET,   "skin",                     Messages.WidgetProperties_Skin) {
        @Override
        public EnumWidgetProperty<Skin> createProperty ( Widget widget, Skin defaultValue ) {
            return new EnumWidgetProperty<>(this, widget, defaultValue);
        }
    };

    private volatile WidgetProperty<BorderDesign> borderDesign;
    private volatile WidgetProperty<Skin>         skin;

    public GaugeWidget ( ) {
        super(WIDGET_DESCRIPTOR.getType(), 120, 120);
    }

    public WidgetProperty<BorderDesign> propBorderDesign ( ) {
        return borderDesign;
    }

    public WidgetProperty<Skin> propSkin ( ) {
        return skin;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        properties.add(borderDesign          = propBorderDesign.createProperty(this, BorderDesign.NONE));
        properties.add(skin                  = propSkin.createProperty(this, Skin.SIMPLE_SECTION));

    }

}
