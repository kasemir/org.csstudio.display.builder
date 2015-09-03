/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.io.InputStream;
import java.util.Objects;

import org.csstudio.display.builder.model.util.Icons;

/** Information about an action
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ActionInfo
{
    private final String description;
    private final String icon_path;

    /** @param description Action description
     *  @param icon_path Path to icon
     */
    public ActionInfo(final String description, final String icon_path)
    {
        this.description = Objects.requireNonNull(description);
        this.icon_path = icon_path;
    }

    /** @return Action description */
    public String getDescription()
    {
        return description;
    }

    /** @return Stream for icon's content
     *  @throws Exception on error
     */
    public InputStream getIconStream() throws Exception
    {
        return Icons.getStream(icon_path);
    }

    @Override
    public String toString()
    {
        return "Action '" + description + "'";
    }
}
