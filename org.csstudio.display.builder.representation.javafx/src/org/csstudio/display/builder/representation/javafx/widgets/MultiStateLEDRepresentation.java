/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.MultiStateLEDWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.VType;

import javafx.scene.paint.Color;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class MultiStateLEDRepresentation extends BaseLEDRepresentation<MultiStateLEDWidget>
{
    @Override
    protected Color[] createColors()
    {
        return new Color[]
        {
            JFXUtil.convert(new WidgetColor(0, 0, 255)),
            JFXUtil.convert(new WidgetColor(0, 255, 0)),
            JFXUtil.convert(new WidgetColor(255, 0, 0)),
        };
    }

    @Override
    protected int computeColorIndex(final VType value)
    {
        return VTypeUtil.getValueNumber(value).intValue();
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
// TODO
//        model_widget.offColor().addUntypedPropertyListener(this::configChanged);
//        model_widget.onColor().addUntypedPropertyListener(this::configChanged);
    }
}
