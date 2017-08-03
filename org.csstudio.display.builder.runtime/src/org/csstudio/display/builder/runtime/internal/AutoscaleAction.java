/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import org.csstudio.display.builder.model.widgets.plots.PlotWidgetProperties.AxisWidgetProperty;
import org.csstudio.display.builder.runtime.RuntimeAction;

/** RuntimeAction to enable autoscale on an axis
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
class AutoscaleAction extends RuntimeAction
{
    private final AxisWidgetProperty axis;

    public AutoscaleAction(final AxisWidgetProperty axis)
    {
        super("Autoscale " + axis.title().getValue(),
              "platform:/plugin/org.csstudio.javafx.rtplot/icons/stagger.png");
        this.axis = axis;
    }

    @Override
    public void run()
    {
        axis.autoscale().setValue(true);
    }
}