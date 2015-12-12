/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt.widgets;

import org.csstudio.display.builder.model.widgets.RectangleWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/** Creates SWT item for model widget
 *  @author Kay Kasemir
 */
public class RectangleRepresentation extends SWTBaseRepresentation<Canvas, RectangleWidget>
{
    @Override
    protected Canvas createSWTControl(final Composite parent) throws Exception
    {   // Unfortunately, the canvas is not transparent..
        final Canvas canvas = new Canvas(parent, SWT.NO_FOCUS | SWT.NO_BACKGROUND | SWT.TRANSPARENT);
        canvas.addPaintListener(this::doPaint);
        return canvas;
    }

    private void doPaint(final PaintEvent event)
    {
        final GC gc = event.gc;
        final Point size = control.getSize();
        gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_MAGENTA));
        gc.drawRectangle(0, 0, size.x-1, size.y-1);
    }
}
