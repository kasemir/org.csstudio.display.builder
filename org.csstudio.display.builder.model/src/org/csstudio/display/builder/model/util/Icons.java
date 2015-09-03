/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.util;

import java.io.FileInputStream;
import java.io.InputStream;

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
        // TODO Handle "plugin://.." type path for icons inside Eclipse plugin
        // This only works for tests that execute in the 'main' directory of a plugin,
        // so "../" leads to the parent of all pluging sources, from which we
        // then locate "specific_plugin/dir/file.png"
        final String resolved = path.replace("platform:/plugin/", "../");
        return new FileInputStream(resolved);
    }
}
