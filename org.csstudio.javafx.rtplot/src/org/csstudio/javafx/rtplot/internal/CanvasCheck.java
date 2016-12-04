/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot.internal;

import static org.csstudio.javafx.rtplot.Activator.logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

import com.sun.javafx.sg.prism.GrowableDataBuffer;

import javafx.scene.canvas.Canvas;

/** Check canvas for overflow
 *  @author Kay Kasemir
 */
@SuppressWarnings("restriction")
public class CanvasCheck
{
    /** Hack: Access to
     *  boolean Canvas#isRendererFallingBehind() and
     *  GrowableDataBuffer current
     *  to check for buffered updates that eventually
     *  exhaust memory.
     */
    private static final Method isRendererFallingBehind_method = initFallingBehind();
    private static final Field current_field = initCurrent();

    private static Method initFallingBehind()
    {
        try
        {
            final Method method = Canvas.class.getDeclaredMethod("isRendererFallingBehind");
            method.setAccessible(true);
            return method;
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot access Canvas#isRendererFallingBehind()", ex);
        }
        return null;
    }

    private static Field initCurrent()
    {
        try
        {
            final Field field = Canvas.class.getDeclaredField("current");
            field.setAccessible(true);
            return field;
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot access Canvas#current", ex);
        }
        return null;
    }

    /** @param canvas Canvas to check
     *  @return Is render buffer of Canvas likely to overflow?
     */
    public static boolean inOverflow(final Canvas canvas)
    {
        boolean overflow = false;
        try
        {
            if (isRendererFallingBehind_method != null)
            {
                final Boolean behind = (Boolean) isRendererFallingBehind_method.invoke(canvas);
                if (behind)
                {
                    logger.log(Level.WARNING, "Canvas renderer is falling behind");
                    overflow = true;
                }
            }
            if (current_field != null)
            {
                final GrowableDataBuffer current = (GrowableDataBuffer) current_field.get(canvas);
                if (current != null  &&  current.writeObjectPosition() > 1)
                {
                    logger.log(Level.WARNING, "Canvas render buffer values: " + current.writeValuePosition() + ", objects: " + current.writeObjectPosition());
                    overflow = true;
                }
            }
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot check Canvas rendering buffer", ex);
        }
        return overflow;
    }
}
