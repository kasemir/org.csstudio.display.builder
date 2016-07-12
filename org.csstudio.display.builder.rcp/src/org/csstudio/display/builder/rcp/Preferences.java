/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

/** Preference settings
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Preferences
{
    public static final String COLOR_FILE = "color_file";
    public static final String FONT_FILE = "font_file";
    public static final String TOP_DISPLAYS = "top_displays";
    public static final String UNDO_SIZE = "undo_size";
    public static final String SHOW_RUNTIME_STACKS = "show_runtime_stacks";

    public static String getColorFile()
    {
        return getPreference(COLOR_FILE,
                             "platform:/plugin/org.csstudio.display.builder.model/examples/color.def");
    }

    public static String getFontFile()
    {
        return getPreference(FONT_FILE,
                             "platform:/plugin/org.csstudio.display.builder.model/examples/font.def");
    }

    public static String getTopDisplays()
    {
        return getPreference(TOP_DISPLAYS, "");
    }

    public static int getUndoSize()
    {
        return Integer.parseInt(getPreference(UNDO_SIZE, "100"));
    }

    public static boolean showPlaceholders()
    {
        return Boolean.parseBoolean(getPreference(SHOW_RUNTIME_STACKS, "false"));
    }

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
