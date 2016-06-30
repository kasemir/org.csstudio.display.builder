/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.javafx;

import javafx.scene.paint.Color;

/** JavaFX Helper
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class JFXUtil
{
    /** Convert color into web-type RGB text
     *  @param color JavaFX {@link Color}
     *  @return RGB text of the form "#FF8080"
     */
    public static String webRGB(final Color color)
    {
        final int r = (int)Math.round(color.getRed() * 255.0);
        final int g = (int)Math.round(color.getGreen() * 255.0);
        final int b = (int)Math.round(color.getBlue() * 255.0);
        return String.format("#%02x%02x%02x" , r, g, b);
    }
}
