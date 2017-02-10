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
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;


/**
 * Widget displaying and editing a numeric PV value.
 *
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 25 Jan 2017
 */
public class LinearMeterWidget extends BaseGaugeWidget {

    public static final WidgetDescriptor WIDGET_DESCRIPTOR = new WidgetDescriptor(
        "linear-meter",
        WidgetCategory.CONTROL,
        "Linear Meter",
        "platform:/plugin/org.csstudio.display.builder.model/icons/linear-meter.png",
        "Linear meter that can read/write a numeric PV"
    ) {
        @Override
        public Widget createWidget ( ) {
            return new LinearMeterWidget();
        }
    };

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    public static final WidgetPropertyDescriptor<Orientation>  propOrientation    = new WidgetPropertyDescriptor<Orientation>(WidgetPropertyCategory.WIDGET,  "orientation",    Messages.WidgetProperties_Orientation) {
        @Override
        public EnumWidgetProperty<Orientation> createProperty ( Widget widget, Orientation defaultValue ) {
            return new EnumWidgetProperty<>(this, widget, defaultValue);
        }
    };

    public static final WidgetPropertyDescriptor<Boolean>      propHighlightBar   = newBooleanPropertyDescriptor             (WidgetPropertyCategory.DISPLAY, "highligh_bar",   Messages.WidgetProperties_HighlightBar);
    public static final WidgetPropertyDescriptor<Boolean>      propHighlightZones = newBooleanPropertyDescriptor             (WidgetPropertyCategory.DISPLAY, "highligh_zones", Messages.WidgetProperties_HighlightZones);

    private volatile WidgetProperty<Boolean>      highligh_bar;
    private volatile WidgetProperty<Boolean>      highligh_zones;
    private volatile WidgetProperty<Orientation>  orientation;

    public LinearMeterWidget ( ) {
        super(WIDGET_DESCRIPTOR.getType(), 350, 80);
    }

    public WidgetProperty<Boolean> propHighlightBar ( ) {
        return highligh_bar;
    }

    public WidgetProperty<Boolean> propHighlightZones ( ) {
        return highligh_zones;
    }

    public WidgetProperty<Orientation> propOrientation ( ) {
        return orientation;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        properties.add(orientation    = propOrientation.createProperty(this, Orientation.HORIZONTAL));

        properties.add(highligh_bar   = propHighlightBar.createProperty(this, true));
        properties.add(highligh_zones = propHighlightZones.createProperty(this, true));

    }

}
