/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import javafx.geometry.Point2D;

/** Constraint on the movement of the Tracker that snaps to a gird
 *  @author Kay Kasemir
 */
public class TrackerGridConstraint implements TrackerConstraint
{
    private final int grid_x;
    private final int grid_y;

    /** @param size Size of grid squares */
    public TrackerGridConstraint(final int size)
    {
        this.grid_x = size;
        this.grid_y = size;
    }

    @Override
    public Point2D constrain(final double x, final double y)
    {
        return new Point2D(
            Math.floor((x + grid_x/2) / grid_x) * grid_x,
            Math.floor((y + grid_y/2) / grid_y) * grid_y);
    }
}
