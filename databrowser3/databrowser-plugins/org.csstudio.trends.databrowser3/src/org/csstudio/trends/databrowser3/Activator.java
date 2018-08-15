/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3;

import java.util.Dictionary;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.util.ResourceUtil;
import org.csstudio.javafx.rtplot.util.NamedThreadFactory;
import org.csstudio.utility.singlesource.SingleSourcePlugin;
import org.csstudio.utility.singlesource.UIHelper.UI;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import javafx.scene.image.Image;

/** Eclipse Plugin Activator
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Activator extends AbstractUIPlugin
{
    /** Plug-in ID defined in MANIFEST.MF */
    final public static String PLUGIN_ID = "org.csstudio.trends.databrowser3";

    /** Checkbox images */
    final public static String ICON_UNCHECKED = "icons/unchecked.gif",
            ICON_CHECKED = "icons/checked.gif";

    /** Singleton instance */
    private static Activator plugin;

    /** Logger for this plugin */
    public static final Logger logger = Logger.getLogger(PLUGIN_ID);

    /** Thread pool, mostly for fetching archived data
    *
    *  <p>No upper limit for threads.
    *  Removes all threads after 10 seconds
    */
   public static final ScheduledExecutorService thread_pool;

   /** Width of the display in pixels. Used to scale negative plot_bins */
   public static int display_pixel_width = 0;

   static
   {
       // After 10 seconds, delete all idle threads
       thread_pool = Executors.newScheduledThreadPool(0, new NamedThreadFactory("DataBrowser"));
      ((ThreadPoolExecutor)thread_pool).setKeepAliveTime(10, TimeUnit.SECONDS);
   }

    /** {@inheritDoc} */
    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);

        try {
            // Determine width of widest monitor
            for (Monitor monitor : Display.getCurrent().getMonitors())
            {
                final int wid = monitor.getBounds().width;
                if (wid > display_pixel_width)
                    display_pixel_width = wid;
            }
        } catch ( Exception ex ) {
            logger.log(Level.WARNING, "Errors determining display pixel width.", ex);
        }
        if (display_pixel_width <= 0)
        {
            logger.log(Level.WARNING, "Cannot determine display pixel width, using 1000");
            display_pixel_width = 1000;
        }

        if (SingleSourcePlugin.getUIHelper().getUI() == UI.RAP)
        {
            // Is this necessary?
            // RAPCorePlugin adds the "server" scope for all plugins,
            // but starts too late...
            Platform.getPreferencesService().setDefaultLookupOrder(
                    PLUGIN_ID, null,
                    new String[]
                            {
                                    InstanceScope.SCOPE,
                                    ConfigurationScope.SCOPE,
                                    "server",
                                    DefaultScope.SCOPE
                            });
        }
        plugin = this;
    }

    /** {@inheritDoc} */
    @Override
    public void stop(BundleContext context) throws Exception
    {
        plugin = null;
        super.stop(context);
    }

    /** @return the shared instance */
    public static Activator getDefault()
    {
        return plugin;
    }

    /** @return Thread pool */
    public static ExecutorService getThreadPool()
    {
        return thread_pool;
    }

    /** Obtain image descriptor from file within plugin.
     *  @param path Path within plugin to image file
     *  @return {@link ImageDescriptor}
     */
    public ImageDescriptor getImageDescriptor(final String path)
    {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /** Obtain image from file within plugin.
     *  Uses registry to avoid duplicates and for disposal
     *  @param path Path within plugin to image file
     *  @return {@link Image}
     */
    public org.eclipse.swt.graphics.Image getImage(final String path)
    {
        org.eclipse.swt.graphics.Image image = getImageRegistry().get(path);
        if (image == null)
        {
            image = getImageDescriptor(path).createImage();
            getImageRegistry().put(path, image);
        }
        return image;
    }


    /** @param base_name Icon base name (no path, no extension)
     *  @return Image
     *  @throws Exception on error
     */
    public static Image getIcon(final String base_name) throws Exception
    {
        String path = "platform:/plugin/org.csstudio.trends.databrowser3/icons/" + base_name + ".png";
        return new Image(ResourceUtil.openPlatformResource(path));
    }

    /** @param base_name Icon base name (no path, no extension)
     *  @return Image
     *  @throws Exception on error
     */
    public static Image getRTPlotIcon(final String base_name) throws Exception
    {
        String path = org.csstudio.javafx.rtplot.Activator.IconPath + base_name + ".png";
        return new Image(ResourceUtil.openPlatformResource(path));
    }

    public static ImageDescriptor getRTPlotIconID(final String base_name)
    {
        String path = org.csstudio.javafx.rtplot.Activator.IconPath + base_name + ".png";
        try
        {
            return ImageDescriptor.createFromImageData(new ImageData(ResourceUtil.openPlatformResource(path)));
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Cannot load image '" + path + "'", e);
            e.printStackTrace();
        }
        return null;
    }

    /** @return Version code */
    public String getVersion()
    {
        final Dictionary<String, String> headers = getBundle().getHeaders();
        return headers.get("Bundle-Version");
    }

    /** @return Logger for this plugin */
    public static Logger getLogger()
    {
        return logger;
    }
}
