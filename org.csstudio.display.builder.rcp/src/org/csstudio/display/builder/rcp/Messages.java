/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import org.eclipse.osgi.util.NLS;

/** Externalized texts
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.csstudio.display.builder.rcp.messages";

    // Keep in alphabetical order!
    public static String NavigateBack;
    public static String NavigateBack_TT;
    public static String NavigateForward;
    public static String NavigateForward_TT;
    public static String TopDisplays;
    public static String Zoom_All;
    public static String Zoom_Height;
    public static String Zoom_Width;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
