/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.model.widgets;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;

/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 19 Jun 2017
 */
public class SymbolWidget extends PVWidget {

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

    /**
     * @param type Widget type.
     * @param default_width Default widget width.
     * @param default_height Default widget height.
     */
    public SymbolWidget ( ) {
        super(WIDGET_DESCRIPTOR.getType(), 120, 120);
    }

}
