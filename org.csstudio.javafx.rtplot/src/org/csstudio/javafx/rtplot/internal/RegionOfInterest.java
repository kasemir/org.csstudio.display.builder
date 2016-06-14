/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot.internal;

import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

/** Region of Interest
 *  @author Kay Kasemir
 */
public class RegionOfInterest
{
    private final String name;
    private final Color color;
    private volatile Rectangle2D region;

    public RegionOfInterest(final String name, final Color color,
            final double x, final double y, final double width, final double height)
    {
        this.name = name;
        this.color = color;
        this.region = new Rectangle2D(x, y, width, height);
    }

    public String getName()
    {
        return name;
    }

    public Color getColor()
    {
        return color;
    }

    public Rectangle2D getRegion()
    {
        return region;
    }
}
