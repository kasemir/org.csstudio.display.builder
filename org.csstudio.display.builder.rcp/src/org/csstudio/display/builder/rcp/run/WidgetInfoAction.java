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
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.vtype.pv.PV;
import org.diirt.vtype.VType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Action that displays information about a widget
 *  @author Kay Kasemir
 */
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

        // Text dump of widget info
        final StringBuilder buf = new StringBuilder();
        buf.append("Widget Name: ").append(widget.getName()).append("\n");
        buf.append("Widget Type: ").append(widget.getType()).append("\n");
        buf.append("\n");
        final Collection<PV> pvs = runtime.getPVs();
        if (! pvs.isEmpty())
        {
            buf.append("PVs:\n");
            for (PV pv : pvs)
            {
                buf.append(pv.getName()).append(" - ");
                final VType value = pv.read();
                if (value == null)
                    buf.append("Disconnected\n");
                else
                    buf.append(value).append("\n");
            }
        }
        // XXX Should display in dialog out of which user can copy/paste the text
        // Could also use a fancier dialog with sections for each PV etc.
        MessageDialog.openInformation(null, "Widget Info", buf.toString());
    }
}
