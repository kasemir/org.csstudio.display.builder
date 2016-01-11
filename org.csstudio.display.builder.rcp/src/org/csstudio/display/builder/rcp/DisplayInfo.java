/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import org.csstudio.display.builder.model.macros.Macros;

/** Information about a display
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DisplayInfo
{
    private final String path, name;
    private final Macros macros;

    public DisplayInfo(final String path, final String name, final Macros macros)
    {
        this.path = path;
        this.name = name;
        this.macros = macros;
    }

    public String getPath()
    {
        return path;
    }

    public String getName()
    {
        return name;
    }

    public Macros getMacros()
    {
        return macros;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + path.hashCode();
        result = prime * result + macros.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (! (obj instanceof DisplayInfo))
            return false;
        final DisplayInfo other = (DisplayInfo) obj;
        return name.equals(other.name) &&
               path.equals(other.path) &&
               macros.equals(other.macros);
    }

    @Override
    public String toString()
    {
        return "Display '" + name + "', file " + path + ", macros " + macros;

    }
}
