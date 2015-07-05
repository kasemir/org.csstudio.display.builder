/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import javafx.geometry.Point2D;

/** Constraint on the movement of the Tracker that allows any coordinate
 *  @author Kay Kasemir
 */
public class TrackerNullConstraint implements TrackerConstraint
{
    @Override
    public Point2D constrain(final double x, final double y)
    {
        return new Point2D(x, y);
    }
}
