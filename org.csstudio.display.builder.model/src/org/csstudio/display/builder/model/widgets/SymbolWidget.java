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
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newFilenamePropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propEnabled;

import java.util.Collections;
import java.util.List;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;

/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 19 Jun 2017
 */
public class SymbolWidget extends PVWidget {

    private final static String DEFAULT_SYMBOL = "platform:/plugin/org.csstudio.display.builder.model/icons/default_symbol.png"; //$NON-NLS-1$

    public static final WidgetDescriptor WIDGET_DESCRIPTOR = new WidgetDescriptor(
            "symbol",
            WidgetCategory.MONITOR,
            "Symbol",
            "platform:/plugin/org.csstudio.display.builder.model/icons/symbol.png",
            "A container of symbols displayed depending of the value of a PV"
        ) {
            @Override
            public Widget createWidget ( ) {
                return new SymbolWidget();
            }
        };

    public static final WidgetPropertyDescriptor<Boolean>                       propPreserveRatio = newBooleanPropertyDescriptor (WidgetPropertyCategory.BEHAVIOR, "preserve_ratio", Messages.WidgetProperties_PreserveRatio);
    /** 'symbol' property: element for list of 'symbols' property */
    private static final WidgetPropertyDescriptor<String>                       propSymbol        = newFilenamePropertyDescriptor(WidgetPropertyCategory.WIDGET,   "symbol",         Messages.WidgetProperties_Symbol);

    /** 'items' property: list of items (string properties) for combo box */
    public static final ArrayWidgetProperty.Descriptor<WidgetProperty<String> > propSymbols       = new ArrayWidgetProperty.Descriptor< WidgetProperty<String> >(
        WidgetPropertyCategory.WIDGET,
        "symbols",
        Messages.WidgetProperties_Symbols, (widget, index) -> propSymbol.createProperty(widget, DEFAULT_SYMBOL)
    );

    private volatile WidgetProperty<Boolean>                     enabled;
    private volatile WidgetProperty<Boolean>                     preserve_ratio;
    private volatile ArrayWidgetProperty<WidgetProperty<String>> symbols;

    /**
     * @param type Widget type.
     * @param default_width Default widget width.
     * @param default_height Default widget height.
     */
    public SymbolWidget ( ) {
        super(WIDGET_DESCRIPTOR.getType(), 100, 100);
    }

    public WidgetProperty<Boolean> propEnabled ( ) {
        return enabled;
    }

    public WidgetProperty<Boolean> propPreserveRatio ( ) {
        return preserve_ratio;
    }

    public ArrayWidgetProperty<WidgetProperty<String>> propSymbols ( ) {
        return symbols;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        properties.add(symbols        = propSymbols.createProperty(this, Collections.emptyList()));

        properties.add(enabled        = propEnabled.createProperty(this, true));
        properties.add(preserve_ratio = propPreserveRatio.createProperty(this, true));

    }

}
