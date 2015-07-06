/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.util.List;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;

import javafx.geometry.Point2D;

/** Constraint on the movement of the Tracker
 *  @author Kay Kasemir
 */
public interface TrackerConstraint
{
    /** Configure tracker
     *  @param model Current model
     *  @param selected_widgets Selected widgets
     */
    default public void configure(DisplayModel model, List<Widget> selected_widgets) {}

    /** Constrain the movement of the tracker
     *
     *  <p>Called with an x/y coordinate, implementation
     *  may return a modified coordinate to restrict the valid
     *  coordinate space.
     *
     *  @param x Requested X position
     *  @param y Requested Y position
     *
     *  @return Constrained coordinate
     */
    public Point2D constrain(double x, double y);
}
