/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import java.io.ByteArrayInputStream;

import org.csstudio.display.builder.model.macros.MacroXMLUtil;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.XMLTags;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.w3c.dom.Element;

/** Preference settings
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Preferences
{
    public static final String READ_TIMEOUT = "read_timeout";

    public static final String LEGACY_FONT_CALIBRATION = "legacy_font_calibration";

    public static final String MACROS = "macros";

    /** @return Read timeout [ms] */
    public static int getReadTimeout()
    {
        int timeout = 10000;
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            timeout = prefs.getInt(ModelPlugin.ID, READ_TIMEOUT, timeout, null);
        return timeout;
    }

    /** @return Legacy font size calibration */
    public static double getLegacyFontCalibration()
    {
        double factor = 0.75;
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            factor = prefs.getDouble(ModelPlugin.ID, LEGACY_FONT_CALIBRATION, factor, null);
        return factor;
    }

    /** @return Global macros set in preferences, or <code>null</code> */
    public static Macros getMacros()
    {
        // Fall-back value used in MacroHierarchyUnitTest
        String macro_def = "<macros><EXAMPLE_MACRO>Value from Preferences</EXAMPLE_MACRO><TEST>true</TEST></macros>";
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            macro_def = prefs.getString(ModelPlugin.ID, MACROS, macro_def, null);
        if (macro_def.isEmpty())
            return null;
        try
        {
            final ByteArrayInputStream stream = new ByteArrayInputStream(macro_def.getBytes());
            final Element root = XMLUtil.openXMLDocument(stream, XMLTags.MACROS);
            return MacroXMLUtil.readMacros(root);
        }
        catch (Exception ex)
        {
            return null;
        }
    }
}
