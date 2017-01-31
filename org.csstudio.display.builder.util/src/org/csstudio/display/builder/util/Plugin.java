/*******************************************************************************
 * Copyright (c) 2015-2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.util;

import java.util.logging.Logger;

/** Plugin information
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Plugin
{
    // At this time not acting as plugin activator

    /** Plugin ID defined in manifest.mf */
    public static final String ID = "org.csstudio.display.builder.util";

    /** Logger for all code in this plugin */
    public static final Logger logger = Logger.getLogger(ID);
}
