/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot;

import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

/** Region of Interest
 *
 *  <p>Region within an {@link RTImagePlot}.
 *
 *  @author Kay Kasemir
 */
public class RegionOfInterest
{
    private final String name;
    private final Color color;
    private volatile boolean visible;
    private volatile Rectangle2D region;

    /** Constructor
     *  @param name
     *  @param color
     *  @param visible
     *  @param x
     *  @param y
     *  @param width
     *  @param height
     */
    public RegionOfInterest(final String name, final Color color,
                            final boolean visible,
                            final double x, final double y, final double width, final double height)
    {
        this.name = name;
        this.color = color;
        this.visible = visible;
        this.region = new Rectangle2D(x, y, width, height);
    }

    /** @return Name of the region */
    public String getName()
    {
        return name;
    }

    /** @return Color of the region */
    public Color getColor()
    {
        return color;
    }

    /** @return Is region visible? */
    public boolean isVisible()
    {
        return visible;
    }

    /** @param visible Should region be visible? */
    public void setVisible(final boolean visible)
    {
        this.visible = visible;
        // Caller needs to request update of image
    }

    /** @return Region of interest within image */
    public Rectangle2D getRegion()
    {
        return region;
    }

    /** @param region Region of interest within image */
    public void setRegion(final Rectangle2D region)
    {
        this.region = region;
        // Caller needs to request update of image
    }
}
