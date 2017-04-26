/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.javafx;

import java.util.logging.Logger;

/** Plugin information
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Activator // Could become plugin activator
{
    /** Plugin ID */
    public static final String ID = "org.csstudio.javafx";

    /** Logger for plugin */
    public static final Logger logger = Logger.getLogger(ID);

    /** @param base_name Icon base name (no path, no extension)
     *  @return Icon path
     */
    public static String getIcon(final String base_name)
    {
        return "platform:/plugin/org.csstudio.javafx/icons/" + base_name + ".png";
    }
}
