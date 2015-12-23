/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** UI independent list of {@link Point}s
 *  @author Kay Kasemir
 */
public class Points implements Iterable<Point>
{
    private final List<Double> points = new ArrayList<>();

    /** @return Number of points */
    public int size()
    {
        return points.size() / 2;
    }

    /** @param index Index of point in list
     *  @return Point
     */
    public Point get(final int index)
    {
        return new Point(getX(index), getY(index));
    }

    /** @param index Index of point in list
     *  @return X coordinate of Point
     */
    public double getX(final int index)
    {
        return points.get(index*2);
    }

    /** @param index Index of point in list
     *  @return Y coordinate of Point
     */
    public double getY(final int index)
    {
        return points.get(index*2+1);
    }

    /** Add point to end of list
     *  @param x X coordinate of Point
     *  @param y Y coordinate of Point
     */
    public void add(final double x, final double y)
    {
        points.add(x);
        points.add(y);
    }

    /** Add point to list
     *  @param index Index in list where point is added. Existing points move "up" towards end of list.
     *  @param x X coordinate of Point
     *  @param y Y coordinate of Point
     */
    public void insert(final int index, final double x, final double y)
    {
        points.add(index*2, x);
        points.add(index*2+1, y);
    }

    /** Delete point from list
     *  @param index Index of point in list
     */
    public void delete(final int index)
    {
        points.remove(index*2);
        points.remove(index*2);
    }

    /** @param index Index of point in list
     *  @param x New x coordinate of Point
     *  @param y New y coordinate of Point
     */
    public void set(final int index, final double x, final double y)
    {
        points.set(index*2, x);
        points.set(index*2+1, y);
    }

    /** @param index Index of point in list
     *  @param x New x coordinate of Point
     */
    public void setX(final int index, final double x)
    {
        points.set(index*2, x);
    }

    /** @param index Index of point in list
     *  @param y New y coordinate of Point
     */
    public void setY(final int index, final double y)
    {
        points.set(index*2+1, y);
    }

    /** @return Array of point coordinates <code>[ x0, y0, x1, y1, ...]</code>
     */
    public Double[] asDoubleArray()
    {
        return points.toArray(new Double[points.size()]);
    }

    private class PointIterator implements Iterator<Point>
    {
        private int index = 0;

        @Override
        public boolean hasNext()
        {
            return index < size();
        }

        @Override
        public Point next()
        {
            return get(index++);
        }
    }

    /** @return Iterator over {@link Point}s */
    @Override
    public Iterator<Point> iterator()
    {
        return new PointIterator();
    }
}
