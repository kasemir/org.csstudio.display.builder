/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.csstudio.display.builder.util.UtilPlugin;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/** JavaFX Helper
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class JFXUtil
{
    private static double font_calibration = 1.0;

    static
    {
        try
        {
            font_calibration = new JFXFontCalibration().getCalibrationFactor();
        }
        catch (Exception ex)
        {
            Logger.getLogger(JFXUtil.class.getName())
                  .log(Level.SEVERE, "Cannot initialize Java FX", ex);
            font_calibration = 1.0;
        }
    }

    /** Convert model color into JFX color
     *  @param color {@link WidgetColor}
     *  @return {@link Color}
     */
    public static Color convert(final WidgetColor color)
    {
        return Color.rgb(color.getRed(), color.getGreen(), color.getBlue());
    }

    /** Convert model color into web-type RGB text
     *  @param color {@link WidgetColor}
     *  @return RGB text of the form "#FF8080"
     */
    public static String webRGB(final WidgetColor color)
    {
        return String.format("#%02X%02X%02X",  color.getRed(), color.getGreen(), color.getBlue());
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
        final double calibrated = font.getSize() * font_calibration;
        switch (font.getStyle())
        {
        case BOLD:
            return Font.font(font.getFamily(), FontWeight.BOLD, calibrated);
        case ITALIC:
            return Font.font(font.getFamily(), FontPosture.ITALIC, calibrated);
        case BOLD_ITALIC:
            return Font.font(font.getFamily(), FontWeight.BOLD, FontPosture.ITALIC, calibrated);
        default:
            return Font.font(font.getFamily(), calibrated);
        }
    }

    /** @param image_path Path to an image, may use "plugin://.."
     *  @return ImageView
     */
    public static ImageView getImageView(final String image_path)
    {
        try
        {
            return new ImageView(new Image(UtilPlugin.getStream(image_path)));
        }
        catch (Exception ex)
        {
            Logger.getLogger(JFXUtil.class.getName())
                  .log(Level.WARNING, "Cannot load " + image_path, ex);
        }
        return null;
    }

    /** Name of icon in this plugin */
    public static ImageView getIcon(final String name)
    {
        return getImageView("platform:/plugin/org.csstudio.display.builder.representation.javafx/icons/" + name);
    }
}
