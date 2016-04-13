/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import java.util.logging.Logger;

import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.persist.WidgetFontService;
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

    public final static Logger logger = Logger.getLogger(Plugin.class.getName());

    @Override
    public void start(final BundleContext context) throws Exception
    {
        WidgetColorService.loadColors(Preferences.getColorFile());
        WidgetFontService.loadFonts(Preferences.getFontFile());
    }

    @Override
    public void stop(final BundleContext context) throws Exception
    {
        // NOP
    }
}
