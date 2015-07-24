/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.util;

import java.util.function.Consumer;

import javafx.geometry.Rectangle2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/** Rubber band type selector
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Rubberband
{
    private final Pane pane;
    private final Consumer<Rectangle2D> handler;
    private final Rectangle rect;
    private boolean active = false;
    private double x0, y0, x1, y1;

    /** Create rubber band handler
     *  @param pane Pane in which rubber band will react to mouse click/drag/release,
     *              and where it displays the rubber band rectangle as a child node
     *  @param rubberband_handler Handler that will be invoked with the selected region
     */
    public Rubberband(final Pane pane, final Consumer<Rectangle2D> rubberband_handler)
    {
        this.pane = pane;
        this.handler = rubberband_handler;
        pane.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleStart);
        pane.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleDrag);
        pane.addEventFilter(MouseEvent.MOUSE_RELEASED, this::handleStop);
        rect = new Rectangle(0, 0, 0, 0);
        rect.setArcWidth(5);
        rect.setArcHeight(5);
        rect.getStyleClass().add("rubberband");
    }

    private void handleStart(final MouseEvent event)
    {
        active = true;
        x0 = event.getX();
        y0 = event.getY();
        rect.setX(x0);
        rect.setY(y0);
        rect.setWidth(1);
        rect.setHeight(1);
        pane.getChildren().add(rect);
    }

    private void handleDrag(final MouseEvent event)
    {
        if (! active)
            return;
        x1 = event.getX();
        y1 = event.getY();
        rect.setX(Math.min(x0, x1));
        rect.setY(Math.min(y0, y1));
        rect.setWidth(Math.abs(x1 - x0));
        rect.setHeight(Math.abs(y1 - y0));
    }

    private void handleStop(final MouseEvent event)
    {
        if (! active)
            return;
        x1 = event.getX();
        y1 = event.getY();
        pane.getChildren().remove(rect);
        active = false;
        handler.accept(new Rectangle2D(Math.min(x0, x1), Math.min(y0, y1),
                                       Math.abs(x1 - x0), Math.abs(y1 - y0)));
    }
}
