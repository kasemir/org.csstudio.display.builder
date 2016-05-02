/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.util;

import static org.csstudio.display.builder.model.ModelPlugin.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Level;

import org.csstudio.display.builder.model.Preferences;
import org.csstudio.display.builder.util.ResourceUtil;

/** Helper for handling resources: File, web link.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ModelResourceUtil extends ResourceUtil
{
    private static int timeout_ms = Preferences.getReadTimeout();

    private static WorkspaceResourceHelper workspace_helper = initializeWRHelper();

    private static WorkspaceResourceHelper initializeWRHelper()
    {
        try
        {
            @SuppressWarnings("unchecked")
            final Class<WorkspaceResourceHelper> clazz =
                (Class<WorkspaceResourceHelper>)Class.forName(WorkspaceResourceHelper.IMPL);
            final WorkspaceResourceHelper helper = clazz.newInstance();
            logger.config("Found WorkspaceResourceHelper");
            return helper;
        }
        catch (ClassNotFoundException ex)
        {   // OK to not have a WorkspaceResourceHelperImpl
            logger.config("No WorkspaceResourceHelper");
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "Cannot create WorkspaceResourceHelper", ex);
        }
        return null;
    }

    /** org.csstudio.display.builder.rcp calls this to support workspace files
     *  @param workspace_helper
     */
    public static void setWorkspaceHelper(final WorkspaceResourceHelper workspace_helper)
    {
        ModelResourceUtil.workspace_helper = workspace_helper;
    }

    // Many basic String operations since paths
    // may include " ", which URL won't handle,
    // or start with "https://", which File won't handle.

    private static boolean isURL(final String path)
    {
        return path.startsWith("http://")  ||
               path.startsWith("https://");
    }

    private static boolean isAbsolute(final String path)
    {
        return path.startsWith("/")  ||
               isURL(path);
    }

    /** Normalize a windows-type path with '\'
     *  @param path Path that may use Windows '\'
     *  @return Path with only '/'
     */
    public static String normalize(final String path)
    {
        // Pattern: '\(?!\)', i.e. backslash _not_ followed by another one.
        // Each \ is doubled as \\ to get one '\' into the string,
        // then doubled once more to tell regex that we want a '\'
        return path.replaceAll("\\\\(?!\\\\)", "/");
    }

    /** Obtain location, i.e. directory of file or URL up to the last element
     *  @param path Complete path, i.e. "/some/location/resource"
     *  @return Location, i.e. "/some/location" without trailing "/", or "."
     */
    public static String getLocation(final String path)
    {
        // Remove last segment from parent_display to get path
        int sep = path.lastIndexOf('/');
        if (sep >= 0)
            return path.substring(0, sep);
        return ".";
    }

    /** Combine display paths
     *  @param parent_display Path to a 'parent' file, may be <code>null</code>
     *  @param display_path Display file. If relative, it is resolved relative to the parent display
     *  @return Combined path
     */
    public static String combineDisplayPaths(String parent_display, String display_path)
    {
        // Anything in the parent?
        if (parent_display == null  ||  parent_display.isEmpty())
            return display_path;

        display_path = normalize(display_path);

        // Is display already absolute?
        if (isAbsolute(display_path))
            return display_path;

        parent_display = normalize(parent_display);

        // Remove last segment from parent_display to get path
        String result = getLocation(parent_display) + "/" + display_path;

        // Collapse  "some/path/remove/../else/file.opi"
        int up = result.indexOf("/../");
        while (up >= 0)
        {   // Locate start of path segment before "/../"
            final int sep = result.lastIndexOf('/', up-1);
            if (sep < 0)
                return result;
            result = result.substring(0, sep) + result.substring(up + 3);
            up = result.indexOf("/../");
        }

        return result;
    }

    /** Attempt to resolve a resource relative to a display
     *  @param parent_display Path to a 'parent' file, may be <code>null</code>
     *  @param resource_name Resource path. If relative, it is resolved relative to the parent display
     *  @return Resolved file name. May also be the original name if no idea how to adjust it
     */
    public static String resolveResource(final String parent_display, final String resource_name)
    {
        logger.log(Level.FINE, "Resolving {0} relative to {1}", new Object[] { resource_name, parent_display });

        if (isURL(resource_name))
        {
            // Appears to be URL?
            logger.log(Level.FINE, "Using URL {0}", resource_name);
            return resource_name;
        }

        // .. relative to parent?
        final String combined = combineDisplayPaths(parent_display, resource_name);
        if (isURL(combined))
        {
            logger.log(Level.FINE, "Using URL {0}", combined);
            return combined;
        }

        // Can display be opened as file?
        File file = new File(resource_name);
        if (file.exists())
        {
            logger.log(Level.FINE, "Found file {0}", file);
            return file.getAbsolutePath();
        }

        file = new File(combined);
        if (file.exists())
        {
            logger.log(Level.FINE, "Found file {0}", file);
            return file.getAbsolutePath();
        }

        // TODO Search along a configurable list of lookup paths

        // Give up, return the original name
        return resource_name;
    }

    /** Open a file, web location, ..
     *
     *  @param resource_name Path to file, "platform:", "http:/.."
     *  @return {@link InputStream}
     *  @throws Exception on error
     */
    public static InputStream openResourceStream(final String resource_name) throws Exception
    {
        if (resource_name.startsWith("platform:"))
            return openPlatformResource(resource_name);

        if (resource_name.startsWith("http"))
            return openURL(resource_name);

        if (workspace_helper != null)
        {
            final InputStream stream = workspace_helper.openWorkspaceResource(resource_name);
            if (stream != null)
                return stream;
        }
        return new FileInputStream(resource_name);
    }

    /** Open URL for "http", "https", "ftp", ..
     *  @param resource_name URL specification
     *  @return {@link InputStream}
     *  @throws Exception on error
     */
    protected static InputStream openURL(final String resource_name) throws Exception
    {
        return openURL(resource_name, timeout_ms);
    }
}
