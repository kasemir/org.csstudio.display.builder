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

import javafx.scene.image.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

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
    private static Logger logger = Logger.getLogger(PLUGIN_ID);

    final public static ExecutorService thread_pool = Executors.newCachedThreadPool(new NamedThreadFactory("DataBrowserJobs"));

    /** {@inheritDoc} */
    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
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
