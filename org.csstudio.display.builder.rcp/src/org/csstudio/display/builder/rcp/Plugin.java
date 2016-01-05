/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.persist.WidgetFontService;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/** Plugin initialization and helpers
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Plugin implements BundleActivator
{
    /** Plugin ID */
    public final static String ID = "org.csstudio.display.builder.rcp";

    @Override
    public void start(final BundleContext context) throws Exception
    {
        final String color_file = getPreference("color_file",
                                                "platform:/plugin/org.csstudio.display.builder.model/examples/color.def");
        WidgetColorService.loadColors(color_file);

        final String font_file = getPreference("font_file",
                                               "platform:/plugin/org.csstudio.display.builder.model/examples/font.def");
        WidgetFontService.loadFonts(font_file);
    }

    @Override
    public void stop(final BundleContext context) throws Exception
    {
        // NOP
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
