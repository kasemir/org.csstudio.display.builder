/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.rcp.Plugin;
import org.csstudio.display.builder.representation.javafx.WidgetInfoDialog;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.display.builder.runtime.pv.RuntimePV;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Action that displays information about a widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetInfoAction extends Action
{
    final Widget widget;

    public WidgetInfoAction(final Widget widget)
    {
        super(widget.getName(), AbstractUIPlugin.imageDescriptorFromPlugin(Plugin.ID, "icons/information.png"));
        this.widget = widget;
    }

    @Override
    public void runWithEvent(final Event event)
    {
        final WidgetRuntime<?> runtime = WidgetRuntime.ofWidget(widget);
        final List<WidgetInfoDialog.NameStateValue> pvs = new ArrayList<>();
        for (RuntimePV pv : runtime.getPVs())
            pvs.add(new WidgetInfoDialog.NameStateValue(pv.getName(), pv.isReadonly() ? "read-only" : "writable", pv.read()));
        final WidgetInfoDialog dialog = new WidgetInfoDialog(widget, pvs);
        if (event.display != null)
        {
            final Point mouse = event.display.getCursorLocation();
            dialog.setX(mouse.x);
            dialog.setY(mouse.y);
        }
        dialog.show();
    }
}
