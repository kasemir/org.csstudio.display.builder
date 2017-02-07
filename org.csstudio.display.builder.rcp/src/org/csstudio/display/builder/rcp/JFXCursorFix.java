/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import static org.csstudio.display.builder.rcp.Plugin.logger;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.sun.javafx.cursor.CursorFrame;

import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;

/** Helper for showing the JavaFX cursor in the FXCanvas
 *  @author Kay Kasemir based on GEF FXCanvasEx info, see below
 */
// CursorFrame is restricted API, but currently without alternative
@SuppressWarnings({"nls","restriction"})
public class JFXCursorFix
{
    // From http://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet156.java
    static ImageData convertToSWT(final BufferedImage bufferedImage)
    {
        if (bufferedImage.getColorModel() instanceof DirectColorModel)
        {
            DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
            PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
            ImageData data = new ImageData(bufferedImage.getWidth(),
                                           bufferedImage.getHeight(), colorModel.getPixelSize(),
                                           palette);
            for (int y = 0; y < data.height; y++)
                for (int x = 0; x < data.width; x++)
                {
                    int rgb = bufferedImage.getRGB(x, y);
                    int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
                    data.setPixel(x, y, pixel);
                    if (colorModel.hasAlpha())
                        data.setAlpha(x, y, (rgb >> 24) & 0xFF);
                }
            return data;
        }
        else if (bufferedImage.getColorModel() instanceof IndexColorModel)
        {
            IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
            int size = colorModel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++)
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
            PaletteData palette = new PaletteData(rgbs);
            ImageData data = new ImageData(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++)
                for (int x = 0; x < data.width; x++)
                {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            return data;
        }
        return null;
    }

    /** @param scene JavaFX Scene where cursor is monitored
     *  @param display SWT Display where the scene's cursor is set
     */
    public static void apply(final Scene scene, final Display display)
    {
        // Track JFX cursor, update SWT cursor
        final ChangeListener<Cursor> cursor_listener = (prop, old, newCursor) ->
        {
            // Standard cursors and null are handled by FXCanvas.
            // Image-based cursors need to be translated into SWT cursor
            // with code based on GEF FXCanvasEx,
            // https://github.com/eclipse/gef4/blob/master/org.eclipse.gef4.fx.swt/src/org/eclipse/gef4/fx/swt/canvas/FXCanvasEx.java
            if (! (newCursor instanceof ImageCursor))
                return;

            // custom cursor, convert image
            final ImageCursor cursor = (ImageCursor) newCursor;
            // SWTFXUtils.fromFXImage() converts JFX Image into SWT image,
            // but adds direct dependency to the jfxswt.jar which is hard
            // to get onto the IDE classpath.
            // To convert JFX to AWT..
            final javafx.scene.image.Image jfx_image = cursor.getImage();
            final BufferedImage awt_image = SwingFXUtils.fromFXImage(jfx_image, null);
            // .. and then to SWT
            final ImageData imageData = convertToSWT(awt_image);
            final double hotspotX = cursor.getHotspotX();
            final double hotspotY = cursor.getHotspotY();
            org.eclipse.swt.graphics.Cursor swtCursor = new org.eclipse.swt.graphics.Cursor(
                    display, imageData, (int) hotspotX, (int) hotspotY);
            // Set platform cursor on CursorFrame so that it can be
            // retrieved by FXCanvas' HostContainer
            // which ultimately sets the cursor on the FXCanvas
            try
            {
                final Method currentCursorFrameAccessor =
                    Cursor.class.getDeclaredMethod("getCurrentFrame", new Class[] {});
                currentCursorFrameAccessor.setAccessible(true);
                final CursorFrame currentCursorFrame = (CursorFrame) currentCursorFrameAccessor.invoke(newCursor, new Object[] {});
                currentCursorFrame.setPlatforCursor(org.eclipse.swt.graphics.Cursor.class, swtCursor);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot update SWT cursor from JFX cursor", ex);
            }
        };
        scene.cursorProperty().addListener(cursor_listener);
    }
}
