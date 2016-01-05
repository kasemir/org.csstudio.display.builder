/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.util;

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

/** Helper for handling resources
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ResourceUtil
{
    /** Used by trustAnybody() to only initialize once */
    private static boolean trusting_anybody = false;

    /** @param resource_name Path to "platform:" resource
     *  @return Stream for content
     *  @throws Exception on error
     */
    public static InputStream openPlatformResource(final String resource_name) throws Exception
    {
        if (! resource_name.startsWith("platform:"))
            throw new Exception("Only handling 'platform:' path, not " + resource_name);
        try
        {
            return new URL(resource_name).openStream();
        }
        catch (Exception ex)
        {
            // Handle "platform://.." path during tests in the 'main' directory of a plugin,
            // so "../" leads to the parent of all plugin sources, from which we
            // then locate "specific_plugin/dir/file.png"
            final String resolved = resource_name.replace("platform:/plugin/", "../");
            return new FileInputStream(resolved);
        }
    }

    /** Open URL for "http", "https", "ftp", ..
     *  @param resource_name URL specification
     *  @param timeout_ms Read timeout [milliseconds]
     *  @return {@link InputStream}
     *  @throws Exception on error
     */
    protected static InputStream openURL(final String resource_name, final int timeout_ms) throws Exception
    {
        if (resource_name.startsWith("https"))
            trustAnybody();

        final URL url = new URL(resource_name);
        final URLConnection connection = url.openConnection();
        connection.setReadTimeout(timeout_ms);
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
