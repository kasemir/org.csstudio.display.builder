/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime;

import static org.csstudio.display.builder.model.ModelPlugin.logger;

import java.io.InputStream;
import java.util.logging.Level;

import org.csstudio.display.builder.util.ResourceUtil;

/** Widget runtime action
 *
 *  <p>The {@link WidgetRuntime} for a widget
 *  can contribute runtime actions to the context
 *  menu of the widget.
 *
 *  @author Kay Kasemir
 */
public abstract class RuntimeAction implements Runnable
{
    protected String description, icon_path;

    /** @param description Description to show to user
     *  @param icon_path Full path to icon, "platform:/plugin/org.csstudio.javafx.rtplot/icons/toolbar.png"
     */
    public RuntimeAction(final String description, final String icon_path)
    {
        this.description = description;
        this.icon_path = icon_path;

    }

    /** @return Action description */
    public String getDescription()
    {
        return description;
    }

    /** @return Stream for icon's content or <code>null</code> */
    public InputStream getIconStream()
    {
        try
        {
            return ResourceUtil.openPlatformResource(icon_path);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot obtain icon", ex);
            return null;
        }
    }
}
