/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.ModelPlugin;
import org.csstudio.display.builder.rcp.run.RuntimeViewPart;
import org.csstudio.display.builder.runtime.ActionUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Action that opens a runtime display
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class OpenDisplayAction extends Action
{
    private final static ImageDescriptor icon =
        AbstractUIPlugin.imageDescriptorFromPlugin(ModelPlugin.ID, "icons/display.png");

    private final DisplayInfo info;

    public static ImageDescriptor getIcon()
    {
        return icon;
    }

    public OpenDisplayAction(final DisplayInfo info)
    {
        super(info.getName(), icon);
        this.info = info;
    }

    @Override
    public void run()
    {
        try
        {
            final RuntimeViewPart part = RuntimeViewPart.open(ActionUtil::handleClose);
            part.loadDisplayFile(info);
        }
        catch (Exception ex)
        {
            Logger.getLogger(getClass().getName())
                  .log(Level.WARNING, "Failed to open " + info, ex);
        }
    }
}
