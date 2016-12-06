/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.persist;

import static org.csstudio.display.builder.model.ModelPlugin.logger;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import org.csstudio.display.builder.model.Preferences;
import org.csstudio.display.builder.model.properties.NamedWidgetColor;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.model.util.ModelThreadPool;

/** Service that provides {@link NamedWidgetColors}
 *
 *  <p>Handles the loading and re-loading of colors
 *  in background thread
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetColorService
{
    /** Current set of named colors.
     *  When still in the process of loading,
     *  this future will be active, i.e. <code>! isDone()</code>.
     */
    private volatile static Future<NamedWidgetColors> colors = CompletableFuture.completedFuture(new NamedWidgetColors());

    /** Ask color service to load colors from a source.
     *
     *  <p>Service loads the colors in background thread.
     *
     *  @param color_resource Name of resource for named color
     */
    public static void loadColorsXXX(final String color_resource)
    {
        colors = ModelThreadPool.getExecutor().submit(() ->
        {
            final NamedWidgetColors colors = new NamedWidgetColors();
            try
            {
                final InputStream stream = ModelResourceUtil.openResourceStream(color_resource);
                logger.log(Level.CONFIG, "Loading named colors from {0}",  color_resource);
                colors.read(stream);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot load colors from " + color_resource, ex);
            }
            // In case of error, result may only contain partial content of file
            return colors;
        });
    }

    /** Ask color service to load colors from a source.
     *
     *  <p>Service loads the colors in background thread.
     *  The 'source' is called in that background thread
     *  to provide the input stream.
     *  The source should thus perform any potentially slow operation
     *  (open file, connect to http://) when called, not beforehand.
     *
     *  @param name   Name that identifies the source (for error messages)
     *  @param source Supplier of InputStream for named colors
     */
    public static void loadColors(final String name, final Callable<InputStream> source)
    {
        colors = ModelThreadPool.getExecutor().submit(() ->
        {
            final NamedWidgetColors colors = new NamedWidgetColors();
            try
            {
                final InputStream stream = source.call();
                logger.log(Level.CONFIG, "Loading named colors from {0}",  name);
                colors.read(stream);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot load colors from " + name, ex);
            }
            // In case of error, result may only contain partial content of file
            return colors;
        });
    }

    /** Obtain current set of named colors.
     *
     *  <p>If service is still in the process of loading
     *  named colors from a source, this call will delay
     *  a little bit to await completion.
     *  This method should thus be called off the UI thread.
     *
     *  <p>This method will not wait indefinitely, however.
     *  If loading colors from a source takes too long,
     *  it will log a warning and return a default set of colors.
     *
     *  @return {@link NamedWidgetColors}
     */
    public static NamedWidgetColors getColors()
    {
        // When in the process of loading, wait a little bit..
        try
        {
            return colors.get(Preferences.getReadTimeout(), TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException timeout)
        {
            logger.log(Level.WARNING, "Using default colors because color file is still loading");
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot obtain named colors", ex);
        }
        return new NamedWidgetColors();
    }

    /** Fetch named color
     *
     *  <p>Falls back to "Text" if requested color is not known.
     *
     *  @param name Name of color
     *  @return {@link NamedWidgetColor}
     */
    public static NamedWidgetColor getColor(final String name)
    {
        final Optional<NamedWidgetColor> color = getColors().getColor(name);
        if (color.isPresent())
            return color.get();
        logger.log(Level.WARNING, "Request for unknown named color {0}", name);
        return getColors().getColor(NamedWidgetColors.TEXT).get();
    }

    /** Resolve a named color
     *  @param name Named color
     *  @return Color as provided unless it was redefined
     */
    public static NamedWidgetColor resolve(final NamedWidgetColor color)
    {
        return getColors().resolve(color);
    }
}
