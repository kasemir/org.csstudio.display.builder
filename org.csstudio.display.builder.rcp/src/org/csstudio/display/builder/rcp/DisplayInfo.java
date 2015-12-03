/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

/** Information about a display
 *  @author Kay Kasemir
 */
public class DisplayInfo
{
    private final String path, name;

    // TODO Add macros
    public DisplayInfo(final String path, final String name)
    {
        this.path = path;
        this.name = name;
    }

    public String getPath()
    {
        return path;
    }

    public String getName()
    {
        return name;
    }
}
