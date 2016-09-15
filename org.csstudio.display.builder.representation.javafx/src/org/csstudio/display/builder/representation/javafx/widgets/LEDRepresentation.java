/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.LEDWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.VType;

import javafx.scene.paint.Color;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class LEDRepresentation extends BaseLEDRepresentation<LEDWidget>
{
    @Override
    protected Color[] createColors()
    {
        return new Color[]
        {
            JFXUtil.convert(model_widget.propOffColor().getValue()),
            JFXUtil.convert(model_widget.propOnColor().getValue())
        };
    }

    @Override
    protected int computeColorIndex(final VType value)
    {
        int number = VTypeUtil.getValueNumber(value).intValue();
        final int bit = model_widget.propBit().getValue();
        if (bit >= 0)
            number &= (1 << bit);
        return number == 0 ? 0 : 1;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.propOffColor().addUntypedPropertyListener(this::configChanged);
        model_widget.propOnColor().addUntypedPropertyListener(this::configChanged);
    }
}
