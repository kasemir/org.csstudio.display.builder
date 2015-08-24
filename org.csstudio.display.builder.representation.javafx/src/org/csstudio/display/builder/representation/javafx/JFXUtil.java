/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/** JavaFX Helper
 *  @author Kay Kasemir
 */
public class JFXUtil
{
    /** Convert model color into JFX color
     *  @param color {@link WidgetColor}
     *  @return {@link Color}
     */
    public static Color convert(final WidgetColor color)
    {
        return Color.rgb(color.getRed(), color.getGreen(), color.getBlue());
    }

    /** Convert JFX color into model color
     *  @param color {@link Color}
     *  @return {@link WidgetColor}
     */
    public static WidgetColor convert(final Color color)
    {
        return new WidgetColor((int) (color.getRed() * 255),
                               (int) (color.getGreen() * 255),
                               (int) (color.getBlue() * 255));
    }

    /** Convert model font into JFX font
     *  @param font {@link WidgetFont}
     *  @return {@link Font}
     */
    public static Font convert(final WidgetFont font)
    {
        switch (font.getStyle())
        {
        case BOLD:
            return Font.font(font.getFamily(), FontWeight.BOLD, font.getSize());
        case ITALIC:
            return Font.font(font.getFamily(), FontPosture.ITALIC, font.getSize());
        case BOLD_ITALIC:
            return Font.font(font.getFamily(), FontWeight.BOLD, FontPosture.ITALIC, font.getSize());
        default:
            return Font.font(font.getFamily(), font.getSize());
        }
    }
}
