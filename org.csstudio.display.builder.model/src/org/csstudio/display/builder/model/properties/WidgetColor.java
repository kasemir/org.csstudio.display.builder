/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

/** Description of a color
 *  @author Kay Kasemir
 */
// Implementation avoids AWT, SWT, JavaFX color
@SuppressWarnings("nls")
public class WidgetColor
{
    private final int red, green, blue;

    // TODO Add String name, and support for named colors

    /** Construct RGB color
     *  @param red Red component, range {@code 0-255}
     *  @param green Green component, range {@code 0-255}
     *  @param blue Blue component, range {@code 0-255}
     */
    public WidgetColor(final int red, final int green, final int blue)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /** @return Red component, range {@code 0-255} */
    public int getRed()
    {
        return red;
    }

    /** @return Green component, range {@code 0-255} */
    public int getGreen()
    {
        return green;
    }

    /** @return Blue component, range {@code 0-255} */
    public int getBlue()
    {
        return blue;
    }

    @Override
    public String toString()
    {
        return "RGB(" + red + "," + green + "," + blue + ")";
    }
}
