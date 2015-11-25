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

/** Helper for loading icons
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Icons
{
    /** @return Stream for icon's content
     *  @throws Exception on error
     */
    public static InputStream getStream(final String path) throws Exception
    {
        if (! path.startsWith("platform:"))
            throw new Exception("Only handling 'platform:' path");
        try
        {
            return new URL(path).openStream();
        }
        catch (Exception ex)
        {
            // Handle "platform://.." path during tests in the 'main' directory of a plugin,
            // so "../" leads to the parent of all plugin sources, from which we
            // then locate "specific_plugin/dir/file.png"
            final String resolved = path.replace("platform:/plugin/", "../");
            return new FileInputStream(resolved);
        }
    }
}
