/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.util.Collection;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.rcp.Plugin;
import org.csstudio.display.builder.representation.javafx.WidgetInfoDialog;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.vtype.pv.PV;
import org.eclipse.jface.action.Action;
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
    public void run()
    {
        final WidgetRuntime<?> runtime = WidgetRuntime.ofWidget(widget);
        final Collection<PV> pvs = runtime.getPVs();
        WidgetInfoDialog dialog = new WidgetInfoDialog(widget, pvs);
        dialog.show();
    }
}
