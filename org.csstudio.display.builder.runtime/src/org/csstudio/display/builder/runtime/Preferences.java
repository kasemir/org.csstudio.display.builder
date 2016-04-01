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

    /** @return Python path */
    public static String getPythonPath()
    {
        String path = "";
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            path = prefs.getString(RuntimePlugin.ID, PYTHON_PATH, path, null);
        return path;
    }
}
