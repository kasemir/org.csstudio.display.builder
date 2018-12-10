/*******************************************************************************
 * Copyright (c) 2014-2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot;

import java.awt.Color;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.csstudio.display.builder.util.ResourceUtil;
import org.csstudio.javafx.rtplot.util.NamedThreadFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

import javafx.scene.image.Image;

/** Not an actual Plugin Activator, but providing plugin-related helpers
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Activator
{
    /** Plugin ID defined in MANIFEST.MF */
    final public static String ID = "org.csstudio.javafx.rtplot";

    final public static Logger logger =  Logger.getLogger(ID);

    /** Thread pool for scrolling, throttling updates
     *  <p>No upper limit for threads.
     *  Removes all threads after 10 seconds
     */
    public static final ScheduledExecutorService thread_pool;

    static
    {
        thread_pool = Executors.newScheduledThreadPool(0, new NamedThreadFactory("RTPlot"));
        ((ThreadPoolExecutor)thread_pool).setKeepAliveTime(10, TimeUnit.SECONDS);
    }

    final public static String IconPath = "platform:/plugin/org.csstudio.javafx.rtplot/icons/";

    public static Image getIcon(final String base_name) throws Exception
    {
        String path = org.csstudio.javafx.rtplot.Activator.IconPath + base_name + ".png";
        return new Image(ResourceUtil.openPlatformResource(path));
    }

    public static Color getShadyFuture()
    {
        String spec = "128, 128, 128, 128";
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            spec = prefs.getString(ID, "shady_future", spec, null);
        final String[] rgba = spec.split("\\s*,\\s*");
        return new Color(Integer.parseInt(rgba[0]),Integer.parseInt(rgba[1]), Integer.parseInt(rgba[2]), Integer.parseInt(rgba[3]));
    }
}
