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

import org.csstudio.display.builder.model.DisplayModel;
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
            logger.fine("Found WorkspaceResourceHelper");
            return helper;
        }
        catch (ClassNotFoundException ex)
        {   // OK to not have a WorkspaceResourceHelperImpl
            logger.fine("No WorkspaceResourceHelper");
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

    private static String[] splitPath(final String path)
    {
        // If path starts with "/",
        // this would result in a list initial empty element.
        if (path.startsWith("/"))
            return path.substring(1).split("/");
        return path.split("/");
    }

    /** Obtain a relative path
     *
     *  <p>Returns original 'path' if it cannot be expressed
     *  relative to the 'parent'.
     *  @param parent Parent file, for example "/one/of/my/directories/parent.bob"
     *  @param path Path to make relative, for example "/one/of/my/alternate_dirs/example.bob"
     *  @return Relative path, e.d. "../alternate_dirs/example.bob"
     */
    public static String getRelativePath(final String parent, String path)
    {
        // If path already appears to be relative, leave it that way
        path = normalize(path);
        if (! isAbsolute(path))
            return path;

        // Locate common path elements
        final String[] parent_elements = splitPath(getDirectory(parent));
        final String[] path_elements = splitPath(path);
        final int len = Math.min(parent_elements.length, path_elements.length);
        int common;
        for (common=0; common<len; ++common)
            if (! parent_elements[common].equals(path_elements[common]))
                break;
        final int difference = parent_elements.length - common;

        // Go 'up' from the parent directory to the common directory
        final StringBuilder relative = new StringBuilder();
        for (int up = difference; up > 0; --up)
        {
            if (relative.length() > 0)
                relative.append("/");
            relative.append("..");
        }

        // Go down from common directory
        for (/**/; common<path_elements.length; ++common)
        {
            if (relative.length() > 0)
                relative.append("/");
            relative.append(path_elements[common]);
        }

        return relative.toString();
    }

    /** Normalize path
     *
     *  <p>Patch windows-type path with '\' into
     *  forward slashes,
     *  and collapse ".." up references.
     *
     *  @param path Path that may use Windows '\' or ".."
     *  @return Path with only '/' and up-references resolved
     */
    public static String normalize(String path)
    {
        // Pattern: '\(?!\)', i.e. backslash _not_ followed by another one.
        // Each \ is doubled as \\ to get one '\' into the string,
        // then doubled once more to tell regex that we want a '\'
        path = path.replaceAll("\\\\(?!\\\\)", "/");
        // Collapse "something/../" into "something/"
        int up = path.indexOf("/../");
        while (up >=0)
        {
            final int prev = path.lastIndexOf('/', up-1);
            if (prev >= 0)
                path = path.substring(0, prev) + path.substring(up+3);
            else
                break;
            up = path.indexOf("/../");
        }
        return path;
    }

    /** Obtain directory of file. For URL, this is the path up to the last element
     *
     *  <p>For a <code>null</code> path, the location will also be <code>null</code>.
     *
     *  @param path Complete path, i.e. "/some/location/resource"
     *  @return Location, i.e. "/some/location" without trailing "/", or "."
     */
    public static String getDirectory(String path)
    {
        if (path == null)
            return null;
        // Remove last segment from parent_display to get path
        path = normalize(path);
        int sep = path.lastIndexOf('/');
        if (sep >= 0)
            return path.substring(0, sep);
        return ".";
    }

    /** Obtain the local path for a resource
     *
     *  <p>When the workspace is supported, this
     *  translates a workspace location into an absolute
     *  location.
     *
     *  <p>Note that the resource must not exist:
     *  This can also be used to translate
     *    /some/workspace/file.txt
     *  into the absolute
     *    /location/of/workspace/some/workspace/file.txt
     *  that the caller wants to create,
     *  i.e. the file does not exist, yet.
     *  The parent directory of the resource, however,
     *  must exist.
     *
     *  @param resource_name Resource that may be relative to workspace
     *  @return Location in local file system or <code>null</code>
     */
    public static String getLocalPath(final String resource_name)
    {
        if (workspace_helper != null)
        {
            final String absolute = workspace_helper.getLocalPath(resource_name);
            if (absolute != null)
                return absolute;
        }

        final File file = new File(resource_name);
        final File parent = file.getParentFile();
        if (parent != null  &&  parent.exists())
            return file.getAbsolutePath();

        return null;
    }

    /** Obtain a workspace resource for local path
     *
    *  <p>When the workspace is supported, this
    *  translates absolute file system location
    *  into a workspace path.
    *
    *  <p>Note that the resource must exist in the workspace.
    *
    *  @param local_name Absolute file system resource
    *  @return Location in workspace or <code>null</code>
    */
   public static String getWorkspacePath(final String local_name)
   {
       if (workspace_helper != null)
           return workspace_helper.getWorkspacePath(local_name);
       return null;
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
        String result = getDirectory(parent_display) + "/" + display_path;
        result = normalize(result);

        return result;
    }

    /** Attempt to resolve a resource relative to a display
     *
     *  <p>For *.opi files, checks if there is an updated .bob file.
     *
     *  @param model {@link DisplayModel}
     *  @param resource_name Resource path. If relative, it is resolved relative to the parent display
     *  @return Resolved file name. May also be the original name if no idea how to adjust it
     */
    public static String resolveResource(final DisplayModel model, final String resource_name)
    {
        final String parent_display = model.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
        return resolveResource(parent_display, resource_name);
    }

    /** Attempt to resolve a resource relative to a display
     *
     *  <p>For *.opi files, checks if there is an updated .bob file.
     *
     *  @param parent_display Path to a 'parent' file, may be <code>null</code>
     *  @param resource_name Resource path. If relative, it is resolved relative to the parent display
     *  @return Resolved file name. May also be the original name if no idea how to adjust it
     */
    public static String resolveResource(final String parent_display, final String resource_name)
    {
        logger.log(Level.FINE, "Resolving {0} relative to {1}", new Object[] { resource_name, parent_display });

        if (resource_name.endsWith(".opi"))
        {   // Check if there is an updated file for a legacy resource
            final String updated_resource = resource_name.substring(0, resource_name.length()-3) + DisplayModel.FILE_EXTENSION;
            final String test = doResolveResource(parent_display, updated_resource);
            if (test != null)
            {
                logger.log(Level.FINE, "Using updated {0} instead of {1}", new Object[] { test, resource_name });
                return test;
            }
        }
        final String result = doResolveResource(parent_display, resource_name);
        if (result != null)
            return result;

        // TODO Search along a configurable list of lookup paths?

        // Give up, returning original name
        return resource_name;
    }

    /** Attempt to resolve a resource relative to a display
     *
     *  <p>Checks for URL, including somewhat expensive access test,
     *  workspace resource,
     *  and finally plain file.
     *
     * @param parent_display
     * @param resource_name
     * @return
     */
    private static String doResolveResource(final String parent_display, final String resource_name)
    {
        // Actual, existing URL?
        if (canOpenUrl(resource_name))
        {
            logger.log(Level.FINE, "Using URL {0}", resource_name);
            return resource_name;
        }

        // .. relative to parent?
        final String combined = combineDisplayPaths(parent_display, resource_name);
        if (canOpenUrl(combined))
        {
            logger.log(Level.FINE, "Using URL {0}", combined);
            return combined;
        }

        // Check for workspace resource
        if (workspace_helper != null)
        {
            if (workspace_helper.isWorkspaceResource(resource_name))
            {
                logger.log(Level.FINE, "Using worspace resource {0}", resource_name);
                return resource_name;
            }
            if (workspace_helper.isWorkspaceResource(combined))
            {
                logger.log(Level.FINE, "Using worspace resource {0}", combined);
                return combined;
            }
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

        // Give up
        return null;
    }

    /** Check if a resource doesn't just look like a URL
     *  but can actually be opened
     *  @param resource_name Path to resource, presumably "http://.."
     *  @return <code>true</code> if indeed an exiting URL
     */
    private static boolean canOpenUrl(final String resource_name)
    {
        if (! isURL(resource_name))
            return false;
        // This implementation is expensive:
        // On success, caller will soon open the URL again.
        // In practice, not too bad because second time around
        // is usually quite fast as result of web server cache.
        //
        // Alternative would be to always return the stream as
        // a result, updating all callers from
        //
        //  resolved = ModelResourceUtil.resolveResource(parent_display, display_file);
        //  stream = ModelResourceUtil.openResourceStream(resolved)
        //
        // to just
        //
        //  stream = ModelResourceUtil.resolveResource(parent_display, display_file);
        //
        // This can break code which really just needs the resolved name.

        try
        {
            final InputStream stream = openURL(resource_name);
            stream.close();
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /** Open a file, web location, ..
     *
     *  @param resource_name Path to file, "platform:", "http:/.."
     *  @return {@link InputStream}
     *  @throws Exception on error
     */
    public static InputStream openResourceStream(final String resource_name) throws Exception
    {
//        {   // Artificial delay to simulate slow file access (1..5 seconds)
//            final long milli = Math.round(1000 + Math.random()*4000);
//            Thread.sleep(milli);
//        }

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
