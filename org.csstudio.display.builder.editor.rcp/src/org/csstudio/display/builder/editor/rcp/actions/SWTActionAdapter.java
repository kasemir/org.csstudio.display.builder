/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import static org.csstudio.display.builder.editor.rcp.Plugin.logger;

import java.util.logging.Level;

import org.csstudio.display.builder.editor.actions.ActionDescription;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/** Adapt generic {@link ActionDescription} to SWT {@link Action}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class SWTActionAdapter extends Action
{
    private ActionDescription description;

    public SWTActionAdapter(final ActionDescription description)
    {
        this.description = description;
        setToolTipText(description.getToolTip());
        try
        {
            setImageDescriptor(ImageDescriptor.createFromURL(description.getIconURL()));
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot get icon for " + description, ex);
        }
    }

    @Override
    public void run()
    {
        description.run(true);
    }
}
