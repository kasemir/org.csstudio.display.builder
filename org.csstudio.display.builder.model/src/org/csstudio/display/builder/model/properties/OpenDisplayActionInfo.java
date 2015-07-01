/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.Objects;

import org.csstudio.display.builder.model.macros.Macros;

/** Information about an action that opens a display
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class OpenDisplayActionInfo extends ActionInfo
{
    public static enum Target
    {
        /** Replace current display */
        REPLACE,

        /** Open a new tab in existing window */
        TAB,

        /** Open a new window */
        WINDOW;
    }

    private final String path;
    private final Macros macros;
    private final Target target;

    /** @param description Action description
     *  @param path Path to the display
     *  @param macros Macros
     *  @param target Where to show the display
     */
    public OpenDisplayActionInfo(final String description, final String path, final Macros macros, final Target target)
    {
        super(description);
        this.path = Objects.requireNonNull(path);
        this.macros = macros;
        this.target = target;
    }

    /** @return Path to the display */
    public String getPath()
    {
        return path;
    }

    /** @return Macros */
    public Macros getMacros()
    {
        return macros;
    }

    /** @return Where to show the display */
    public Target getTarget()
    {
        return target;
    }

    @Override
    public String toString()
    {
        String info = "OpenDisplayAction '" + getDescription() + "', " + path + " [" + target + "]";
        final String macro_info = macros.toString();
        if (macro_info.isEmpty())
            return info;
        return info + ", macros " + macro_info;
    }
}
