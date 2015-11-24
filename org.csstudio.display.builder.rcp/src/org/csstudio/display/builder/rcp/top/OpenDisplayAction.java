/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.top;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.rcp.Plugin;
import org.csstudio.display.builder.rcp.run.RuntimeViewPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Action that opens a runtime display
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class OpenDisplayAction extends Action
{
    private final static ImageDescriptor icon = AbstractUIPlugin.imageDescriptorFromPlugin(Plugin.ID, "icons/display.png");

    private final String path;

    public static ImageDescriptor getIcon()
    {
        return icon;
    }

    public OpenDisplayAction(final DisplayInfo info)
    {
        super(info.getName(), icon);
        this.path = info.getPath();
    }

    @Override
    public void run()
    {
        try
        {
            final RuntimeViewPart part = RuntimeViewPart.open(getText());
            part.loadDisplayFile(path);
        }
        catch (Exception ex)
        {
            Logger.getLogger(getClass().getName())
                  .log(Level.WARNING, "Failed to open display " + path, ex);
        }
    }
}
