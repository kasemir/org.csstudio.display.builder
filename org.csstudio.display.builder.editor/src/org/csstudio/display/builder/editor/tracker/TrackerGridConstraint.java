/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.tracker;


import org.csstudio.display.builder.representation.ToolkitRepresentation;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;


/**
 * Constraint on the movement of the Tracker that snaps to a gird
 *
 * @author Kay Kasemir
 */
public class TrackerGridConstraint extends TrackerConstraint {

    private final ToolkitRepresentation<Parent, Node> toolkit;

    /** @param size Size of grid squares */
    public TrackerGridConstraint ( final ToolkitRepresentation<Parent, Node> toolkit ) {
        this.toolkit = toolkit;
    }

    @Override
    public Point2D constrain ( final double x, final double y ) {

         int grid_x = toolkit.getGridStepX();
         int grid_y = toolkit.getGridStepY();

        return new Point2D(Math.floor(( x + grid_x / 2 ) / grid_x) * grid_x, Math.floor(( y + grid_y / 2 ) / grid_y) * grid_y);

    }
}
