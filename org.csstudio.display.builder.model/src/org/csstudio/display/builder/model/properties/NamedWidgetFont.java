/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

/** Description of a named font
 *  @author Kay Kasemir
 */
// Implementation avoids AWT, SWT, JavaFX color
public class NamedWidgetFont extends WidgetFont
{
    private final String name;

    /** Construct named font
     *  @param name Name of the color
     *  @param family Font family: "Liberation Sans"
     *  @param style  Font style: Bold, italic?
     *  @param size   Size (height)
     */
    public NamedWidgetFont(final String name, final String family, final WidgetFontStyle style, final int size)
    {
        super(family, style, size);
        this.name = name;
    }

    /** @return Name */
    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
