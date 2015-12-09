/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

/** Plugin information.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Plugin
{
    /** Plugin ID */
    public final static String ID = "org.csstudio.display.builder.rcp";

    /** @param key Preference key
     *  @param default_value Default value
     *  @return Preference text or default value
     */
    public static String getPreference(final String key, final String default_value)
    {
        final IPreferencesService prefs = Platform.getPreferencesService();
        return prefs.getString(Plugin.ID, key, default_value, null);
    }
}
