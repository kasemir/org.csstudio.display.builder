package org.csstudio.javafx.rtplot.internal;

import javafx.scene.paint.Color;

public class RegionOfInterest
{
    private final String name;
    private final Color color;
    private volatile double x, y, width, height;

    public RegionOfInterest(final String name, final Color color,
            final double x, final double y, final double width, final double height)
    {
        this.name = name;
        this.color = color;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String getName()
    {
        return name;
    }

    public Color getColor()
    {
        return color;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getWidth()
    {
        return width;
    }

    public double getHeight()
    {
        return height;
    }
}
