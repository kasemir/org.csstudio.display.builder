/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import org.csstudio.display.builder.model.Messages;

/** Description of a font style
 *  @author Kay Kasemir
 */
public enum WidgetFontStyle
{
    REGULAR(Messages.FontStyle_Regular),
    ITALIC(Messages.FontStyle_Italic),
    BOLD(Messages.FontStyle_Bold),
    BOLD_ITALIC(Messages.FontStyle_BoldItalic);

    private final String name;

    private WidgetFontStyle(final String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
