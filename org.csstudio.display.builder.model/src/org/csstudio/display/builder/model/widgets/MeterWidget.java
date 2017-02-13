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
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newIntegerPropertyDescriptor;

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
public class MeterWidget extends BaseGaugeWidget {

    public static final WidgetDescriptor WIDGET_DESCRIPTOR = new WidgetDescriptor(
        "meter",
        WidgetCategory.MONITOR,
        "Meter",
        "platform:/plugin/org.csstudio.display.builder.model/icons/meter.png",
        "Meter that can read a numeric PV"
    ) {
        @Override
        public Widget createWidget ( ) {
            return new MeterWidget();
        }
    };

    /**
     * The position of the knov in non-LINEAR meters.
     *
     * @author Claudio Rosati, European Spallation Source ERIC
     * @version 1.0.0 7 Feb 2017
     */
    public enum KnobPosition {
        BOTTOM_CENTER,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        CENTER,
        CENTER_LEFT,
        CENTER_RIGHT,
        TOP_CENTER,
        TOP_LEFT,
        TOP_RIGHT
    }

    /**
     * This enumeration is used to reduce the choices from the original enumeration.
     *
     * @author Claudio Rosati, European Spallation Source ERIC
     * @version 1.0.0 25 Jan 2017
     */
    public enum Skin {
        AMP,
        GAUGE,
        HORIZONTAL,
        QUARTER,
        THREE_QUARTERS,
        VERTICAL
    }

    public static final WidgetPropertyDescriptor<Skin>         propSkin           = new WidgetPropertyDescriptor<Skin>        (WidgetPropertyCategory.WIDGET,   "skin",            Messages.WidgetProperties_Skin) {
        @Override
        public EnumWidgetProperty<Skin> createProperty ( Widget widget, Skin defaultValue ) {
            return new EnumWidgetProperty<>(this, widget, defaultValue);
        }
    };
    public static final WidgetPropertyDescriptor<KnobPosition> propKnobPosition   = new WidgetPropertyDescriptor<KnobPosition>(WidgetPropertyCategory.WIDGET,   "knob_position",   Messages.WidgetProperties_KnobPosition) {
        @Override
        public EnumWidgetProperty<KnobPosition> createProperty ( Widget widget, KnobPosition defaultValue ) {
            return new EnumWidgetProperty<>(this, widget, defaultValue);
        }
    };

    public static final WidgetPropertyDescriptor<Boolean>      propHighlightZones = newBooleanPropertyDescriptor              (WidgetPropertyCategory.BEHAVIOR, "highligh_zones",  Messages.WidgetProperties_HighlightZones);

    public static final WidgetPropertyDescriptor<Boolean>      propAverage        = newBooleanPropertyDescriptor              (WidgetPropertyCategory.MISC,     "average",         Messages.WidgetProperties_Average);
    public static final WidgetPropertyDescriptor<WidgetColor>  propAverageColor   = newColorPropertyDescriptor                (WidgetPropertyCategory.MISC,     "average_color",   Messages.WidgetProperties_AverageColor);
    public static final WidgetPropertyDescriptor<Integer>      propAverageSamples = newIntegerPropertyDescriptor              (WidgetPropertyCategory.MISC,     "average_samples", Messages.WidgetProperties_AverageSamples, 1, 1000);

    private volatile WidgetProperty<Boolean>      average;
    private volatile WidgetProperty<WidgetColor>  average_color;
    private volatile WidgetProperty<Integer>      average_samples;
    private volatile WidgetProperty<Boolean>      highligh_zones;
    private volatile WidgetProperty<KnobPosition> knob_position;
    private volatile WidgetProperty<Skin>         skin;

    public MeterWidget ( ) {
        super(WIDGET_DESCRIPTOR.getType(), 240, 120);
    }

    public WidgetProperty<Boolean> propAverage ( ) {
        return average;
    }

    public WidgetProperty<WidgetColor> propAverageColor ( ) {
        return average_color;
    }

    public WidgetProperty<Integer> propAverageSamples ( ) {
        return average_samples;
    }

    public WidgetProperty<Boolean> propHighlightZones ( ) {
        return highligh_zones;
    }

    public WidgetProperty<KnobPosition> propKnobPosition ( ) {
        return knob_position;
    }

    public WidgetProperty<Skin> propSkin ( ) {
        return skin;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        properties.add(skin            = propSkin.createProperty(this, Skin.HORIZONTAL));
        properties.add(knob_position   = propKnobPosition.createProperty(this, KnobPosition.BOTTOM_CENTER));

        properties.add(highligh_zones  = propHighlightZones.createProperty(this, true));

        properties.add(average         = propAverage.createProperty(this, false));
        properties.add(average_color   = propAverageColor.createProperty(this, new WidgetColor(13, 23, 251)));
        properties.add(average_samples = propAverageSamples.createProperty(this, 100));

    }

}
