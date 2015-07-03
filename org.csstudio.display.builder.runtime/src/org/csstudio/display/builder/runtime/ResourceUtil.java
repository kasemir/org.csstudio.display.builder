/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/** Helper for handling resources: File, web link.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ResourceUtil
{
    /** Used by trustAnybody() to only initialize once */
    private static boolean trusting_anybody = false;

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

    /** Combine display paths
     *  @param parent_display Path to a 'parent' file, may be <code>null</code>
     *  @param display_file Display file. If relative, it is resolved relative to the parent display
     *  @return Combined path
     */
    public static String combineDisplayPaths(String parent_display, String display_file)
    {
        // Anything in the parent?
        if (parent_display == null  ||  parent_display.isEmpty())
            return display_file;

        display_file = normalize(display_file);

        // Is display already absolute?
        if (isAbsolute(display_file))
            return display_file;

        parent_display = normalize(parent_display);

        // Remove last segment from parent_display to get path
        int sep = parent_display.lastIndexOf('/');
        if (sep >= 0)
            parent_display = parent_display.substring(0, sep);

        String result = parent_display + "/" + display_file;

        // Collapse  "some/path/remove/../else/file.opi"
        int up = result.indexOf("/../");
        while (up >= 0)
        {
            sep = result.lastIndexOf('/', up-1);
            if (sep < 0)
                return result;

            result = result.substring(0, sep) + result.substring(up + 3);
            up = result.indexOf("/../");
        }

        return result;
    }

    /** Attempt to resolve a display file
     *  @param parent_display Path to a 'parent' file, may be <code>null</code>
     *  @param display_file Display file. If relative, it is resolved relative to the parent display
     *  @return Resolved file name. May also be the original name if no idea how to adjust it
     */
    public static String resolveDisplay(final String parent_display, final String display_file)
    {
        // Can display be opened as file?
        File file = new File(display_file);
        if (file.exists())
            return file.getAbsolutePath();

        // .. relative to parent?
        file = new File(combineDisplayPaths(parent_display, display_file));
        if (file.exists())
            return file.getAbsolutePath();

        // Can display be opened as URL?
        if (isURL(display_file))
            return display_file;

        // .. relative to parent?
        final String combined = combineDisplayPaths(parent_display, display_file);
        if (isURL(combined))
            return combined;

        // TODO Search along a configurable list of lookup paths

        // Give up, return the original name
        return display_file;
    }

    /** Open a file, web location, ..
     *
     *  @param resource_name Path to file or "http:/.."
     *  @return {@link InputStream}
     *  @throws Exception on error
     */
    // TODO Handle Workspace location
    public static InputStream openInputStream(final String resource_name) throws Exception
    {
        if (resource_name.startsWith("http"))
            return openURL(resource_name);

        return new FileInputStream(resource_name);
    }

    /** Open URL
     *  @param resource_name URL specification
     *  @return {@link InputStream}
     *  @throws Exception on error
     */
    private static InputStream openURL(final String resource_name) throws Exception
    {
        if (resource_name.startsWith("https"))
            trustAnybody();

        final URL url = new URL(resource_name);
        final URLConnection connection = url.openConnection();
        connection.setReadTimeout(10000); // TODO Preference for read timeout
        return connection.getInputStream();
    }

    /** Allow https:// access to self-signed certificates
     *  @throws Exception on error
     */
    // From Eric Berryman's code in org.csstudio.opibuilder.util.ResourceUtil.
    private static synchronized void trustAnybody() throws Exception
    {
        if (trusting_anybody)
            return;

        // Create a trust manager that does not validate certificate chains.
        final TrustManager[] trustAllCerts = new TrustManager[]
        {
            new X509TrustManager()
            {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType)
                { /* NOP */ }
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType)
                { /* NOP */ }
            }
        };
        final SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // All-trusting host name verifier
        final HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        trusting_anybody = true;
    }
}
