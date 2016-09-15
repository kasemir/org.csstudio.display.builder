/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.List;

import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.MultiStateLEDWidget;
import org.csstudio.display.builder.model.widgets.MultiStateLEDWidget.StateWidgetProperty;
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
        final List<StateWidgetProperty> states = model_widget.propStates().getValue();
        final int N = states.size();
        final Color[] colors = new Color[N+1];
        for (int i=0; i<N; ++i)
            colors[i] = JFXUtil.convert(states.get(i).color().getValue());
        colors[N] = JFXUtil.convert(model_widget.propFallbackColor().getValue());
        return colors;
    }

    @Override
    protected int computeColorIndex(final VType value)
    {
        final int number = VTypeUtil.getValueNumber(value).intValue();
        final List<StateWidgetProperty> states = model_widget.propStates().getValue();
        final int N = states.size();
        for (int i=0; i<N; ++i)
            if (number == states.get(i).state().getValue())
                return i;
        // Use fallback color
        return N;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        // Not really listening to changes in any of the state's colors or state values
        model_widget.propStates().addUntypedPropertyListener(this::configChanged);
    }
}
