/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import org.csstudio.display.builder.rcp.Messages;
import org.csstudio.display.builder.rcp.Plugin;
import org.csstudio.display.builder.rcp.RuntimeViewPart;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Action to re-load current display
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ReloadDisplayAction extends Action
{
    public ReloadDisplayAction()
    {
        super(Messages.ReloadDisplay,
              AbstractUIPlugin.imageDescriptorFromPlugin(Plugin.ID, "icons/refresh.gif"));
    }

    @Override
    public void run()
    {
        final RuntimeViewPart view = RuntimeViewPart.getActiveDisplay();
        if (view != null)
            view.loadDisplayFile(view.getDisplayInfo());
    }
}
