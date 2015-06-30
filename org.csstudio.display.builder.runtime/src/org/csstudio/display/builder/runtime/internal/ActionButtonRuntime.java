/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.widgets.ActionButtonWidget;
import org.csstudio.display.builder.representation.WidgetRepresentation;
import org.csstudio.display.builder.representation.WidgetRepresentationListener;
import org.csstudio.display.builder.runtime.ActionUtil;
import org.csstudio.display.builder.runtime.WidgetRuntime;

/** Runtime for the ActionButtonWidget
 *
 *  <p>Handles invoked actions
 *
 *  @author Kay Kasemir
 */
public class ActionButtonRuntime extends WidgetRuntime<ActionButtonWidget> implements WidgetRepresentationListener
{
    public ActionButtonRuntime(final ActionButtonWidget widget)
    {
        super(widget);
    }

    /** Start: Connect to PVs, ...
     *  @throws Exception on error
     */
    @Override
    public void start() throws Exception
    {
        super.start();

        final WidgetRepresentation<?, ?, ActionButtonWidget> representation = widget.getUserData(Widget.USER_DATA_REPRESENTATION);
        if (representation != null)
            representation.addListener(this);
    }

    @Override
    public void stop()
    {
        final WidgetRepresentation<?, ?, ActionButtonWidget> representation = widget.getUserData(Widget.USER_DATA_REPRESENTATION);
        if (representation != null)
            representation.removeListener(this);

        super.stop();
    }

    // WidgetRepresentationListener
    @Override
    public void handleAction(final Widget widget, final ActionInfo action)
    {
        ActionUtil.handleAction(widget, action);
    }
}
