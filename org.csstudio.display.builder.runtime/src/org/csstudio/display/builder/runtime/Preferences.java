/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime;

import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.PatternSyntaxException;

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
    public static final String PV_NAME_PATCHES = "pv_name_patches";

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

    /** @return PV {@link TextPatch}s */
    public static List<TextPatch> getPV_NamePatches()
    {
        final List<TextPatch> patches = new ArrayList<>();
        final String[] config = get(PV_NAME_PATCHES, "").split("(?<!\\[)@");

        if (config.length % 2 == 0)
        {
            for (int i=0; i<config.length; i+=2)
            {
                TextPatch patch;
                try
                {
                    patch = new TextPatch(config[i], config[i+1]);
                }
                catch (PatternSyntaxException ex)
                {
                    logger.log(Level.SEVERE, "Error in PV name patch '" + config[i] + "' -> '" + config[i+1] + "'", ex);
                    continue;
                }
                patches.add(patch);
                logger.config(patch.toString());
            }
        }
        else
            logger.log(Level.SEVERE, "Invalid setting for " + PV_NAME_PATCHES +
                                     ", need even number of items (pairs of pattern@replacement)");
        return patches;
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
