/*******************************************************************************
 * Copyright (c) 2015-2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.javafx.swt;

import static org.csstudio.javafx.swt.Plugin.logger;

import java.lang.reflect.Method;
import java.util.logging.Level;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import com.sun.javafx.cursor.CursorFrame;

import javafx.beans.value.ChangeListener;
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
    /** @param scene JavaFX Scene where cursor is monitored
     *  @param display SWT Display where the scene's cursor is set
     */
    private static final boolean JAVA_8 = System.getProperty("java.version")
            .startsWith("1.8.0");
    
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

            // Custom cursor, convert image
            final ImageCursor cursor = (ImageCursor) newCursor;
            final ImageData imageData = ImageConverter.convertToSWT(cursor.getImage());
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
        if (JAVA_8) {
            scene.cursorProperty().addListener(cursor_listener);
        }
    }
}
