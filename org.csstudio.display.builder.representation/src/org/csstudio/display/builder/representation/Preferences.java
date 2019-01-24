/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

/** Preference settings
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Preferences
{
    public static final String ID = "org.csstudio.display.builder.representation";
    // For explanation see preferences.ini

    public static int getLogPeriodSeconds()
    {
        int secs = 5;
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            secs = prefs.getInt(ID, "performance_log_period_secs", secs, null);
        return secs;
    }

    public static int getLogThresholdMillisec()
    {
        int milli = 20;
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            milli = prefs.getInt(ID, "performance_log_threshold_ms", milli, null);
        return milli;
    }

    public static int getUpdateAccumulationMillisec()
    {
        int milli = 20;
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            milli = prefs.getInt(ID, "update_accumulation_time", milli, null);
        return milli;
    }

    public static int getUpdateDelayMillisec()
    {
        int milli = 100;
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            milli = prefs.getInt(ID, "update_delay", milli, null);
        return milli;
    }

    public static int getPlotUpdateDelayMillisec()
    {
        int milli = 100;
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            milli = prefs.getInt(ID, "plot_update_delay", milli, null);
        return milli;
    }

    public static int getImageUpdateDelayMillisec()
    {
        int milli = 100;
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            milli = prefs.getInt(ID, "image_update_delay", milli, null);
        return milli;
    }

}
