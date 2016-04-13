/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

/** Preference settings
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Preferences
{
    public static final String PYTHON_PATH = "python_path";
    public static final String PV_FACTORY = "pv_factory";

    /** @return Python path */
    public static String getPythonPath()
    {
        return get(PYTHON_PATH, "");
    }

    /** @return PV Factory */
    public static String getPV_Factory()
    {
        return get(PV_FACTORY, "vtype.pv");
    }

    private static String get(final String setting, final String default_value)
    {
        String value = default_value;
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            value = prefs.getString(RuntimePlugin.ID, setting, value, null);
        return value;
    }
}
