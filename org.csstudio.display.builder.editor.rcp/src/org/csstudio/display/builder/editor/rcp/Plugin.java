/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Plugin Info
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Plugin
{
    /** Plugin ID */
    public static final String ID = "org.csstudio.display.builder.editor.rcp";

    public static ImageDescriptor getIcon(final String name)
    {
        return AbstractUIPlugin.imageDescriptorFromPlugin(ID, "icons/" + name);
    }
}
