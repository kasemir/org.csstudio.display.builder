/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import org.csstudio.display.builder.model.Messages;

/** Rotation, restricted to 90 degree steps
 *  @author Kay Kasemir
 */
public enum RotationStep
{
    NONE(Messages.Rotation_0, 0.0),
    NINETY(Messages.Rotation_90, 90.0),
    ONEEIGHTY(Messages.Rotation_180, 180.0),
    MINUS_NINETY(Messages.Rotation_270, 270.0);

    private final String label;
    private final double angle;

    private RotationStep(final String label, final double angle)
    {
        this.label = label;
        this.angle = angle;
    }

    /** @return Rotation angle in degrees */
    public double getAngle()
    {
        return angle;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
