/*******************************************************************************
 * Copyright (c) 2014-2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot.internal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.Trace;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.javafx.rtplot.data.PlotDataItem;
import org.csstudio.javafx.rtplot.data.PlotDataProvider;
import org.csstudio.javafx.rtplot.internal.util.GraphicsUtils;
import org.csstudio.javafx.rtplot.internal.util.IntList;
import org.csstudio.javafx.rtplot.internal.util.ScreenTransform;

/** Helper for painting a {@link Trace}
 *  @param <XTYPE> Data type of horizontal {@link Axis}
 *  @author Kay Kasemir
 */
public class TracePainter<XTYPE extends Comparable<XTYPE>>
{
    // Implementation notes:
    // gc.drawPolyline() is faster than gc.drawLine() calls
    // plus it works better when using dashed or wide lines,
    // but it requires an int[] array of varying size.
    // IntList turned out to be about 3x faster than ArrayList<Integer>.

    /** Initial {@link IntList} size */
    private static final int INITIAL_ARRAY_SIZE = 2048;

    /** Fudge to avoid clip errors
     *
     *  <p>When coordinates are way outside the clip region,
     *  clipping fails and graphics are 'aliases' into the visible range.
     *  By moving clipped coordinates just 'OUTSIDE' the allowed region,
     *  rounding errors inside the clipping implementation are avoided.
     *  Strictly speaking, we'd have to compute the intersection of
     *  lines with the clip region, but this is much easier to implement.
     */
    final private static int OUTSIDE = 1000;
    private int x_min, x_max, y_min, y_max;

    final private int clipX(final double x)
    {
        if (x < x_min)
            return x_min;
        if (x > x_max)
            return x_max;
        return (int)x;
    }

    final private int clipY(final int y)
    {
        if (y < y_min)
            return y_min;
        if (y > y_max)
            return y_max;
        return y;
    }

    /** @param gc GC
     *  @param bounds Clipping bounds within which to paint
     *  @param x_transform Coordinate transform used by the x axis
     *  @param trace Trace, has reference to its value axis
     */
    final public void paint(final Graphics2D gc, final Rectangle bounds,
                            final ScreenTransform<XTYPE> x_transform, final YAxisImpl<XTYPE> y_axis,
                            final Trace<XTYPE> trace)
    {
        x_min = bounds.x - OUTSIDE;
        x_max = bounds.x + bounds.width + OUTSIDE;
        y_min = bounds.y - OUTSIDE;
        y_max = bounds.y + bounds.height + OUTSIDE;

        final Color old_color = gc.getColor();
        final Color old_bg = gc.getBackground();
        final Stroke old_width = gc.getStroke();

        final Color color = GraphicsUtils.convert(trace.getColor());
        gc.setBackground(color);
        gc.setColor(color);

        // TODO Optimize drawing
        //
        // Determine first sample to draw via PlotDataSearch.findSampleLessOrEqual(),
        // then end drawing when reaching right end of area.
        //
        // Loop only once, performing drawMinMax, drawStdDev, drawValueStaircase in one loop
        //
        // For now, main point is that this happens in non-UI thread,
        // so the slower the better to test UI responsiveness.
        final PlotDataProvider<XTYPE> data = trace.getData();
        data.getLock().lock();
        try
        {
            final TraceType type = trace.getType();
            switch (type)
            {
            // TODO
//            case NONE:
//                break;
//            case AREA:
//                gc.setAlpha(50);
//                drawMinMaxArea(gc, x_transform, y_axis, data);
//                gc.setAlpha(255);
//                drawStdDevLines(gc, x_transform, y_axis, data, trace.getWidth());
//                drawValueStaircase(gc, x_transform, y_axis, data, trace.getWidth());
//                break;
//            case AREA_DIRECT:
//                gc.setAlpha(50);
//                drawMinMaxArea(gc, x_transform, y_axis, data);
//                gc.setAlpha(255);
//                drawStdDevLines(gc, x_transform, y_axis, data, trace.getWidth());
//                drawValueLines(gc, x_transform, y_axis, data, trace.getWidth());
//                break;
//            case LINES:
//                drawMinMaxLines(gc, x_transform, y_axis, data, trace.getWidth());
//                gc.setAlpha(50);
//                drawStdDevLines(gc, x_transform, y_axis, data, trace.getWidth());
//                gc.setAlpha(255);
//                drawValueStaircase(gc, x_transform, y_axis, data, trace.getWidth());
//                break;
//            case LINES_DIRECT:
//                drawMinMaxLines(gc, x_transform, y_axis, data, trace.getWidth());
//                gc.setAlpha(50);
//                drawStdDevLines(gc, x_transform, y_axis, data, trace.getWidth());
//                gc.setAlpha(255);
//                drawValueLines(gc, x_transform, y_axis, data, trace.getWidth());
//                break;
//            case SINGLE_LINE:
//                drawValueStaircase(gc, x_transform, y_axis, data, trace.getWidth());
//                break;
//            case SINGLE_LINE_DIRECT:
//                drawValueLines(gc, x_transform, y_axis, data, trace.getWidth());
//                break;
            default:
                drawValueStaircase(gc, x_transform, y_axis, data, trace.getWidth());
            }

            final PointType point_type = trace.getPointType();
            if (point_type != PointType.NONE)
                drawPoints(gc, x_transform, y_axis, data, point_type, trace.getPointSize());
        }
        finally
        {
            data.getLock().unlock();
        }
        gc.setStroke(old_width);
        gc.setBackground(old_bg);
        gc.setColor(old_color);
    }

    /** Draw values of data as staircase line
     *  @param gc GC
     *  @param x_transform Horizontal axis
     *  @param y_axis Value axis
     *  @param data Data
     *  @param line_width
     */
    final private void drawValueStaircase(final Graphics2D gc,
            final ScreenTransform<XTYPE> x_transform, final YAxisImpl<XTYPE> y_axis,
            final PlotDataProvider<XTYPE> data, final int line_width)
    {
        final IntList poly_x = new IntList(INITIAL_ARRAY_SIZE);
        final IntList poly_y = new IntList(INITIAL_ARRAY_SIZE);
        final int N = data.size();
        int last_x = -1, last_y = -1;
        gc.setStroke(new BasicStroke(line_width));
        for (int i=0; i<N; ++i)
        {
            final PlotDataItem<XTYPE> item = data.get(i);
            final int x = clipX(Math.round(x_transform.transform(item.getPosition())));
            final double value = item.getValue();
            if (poly_x.size() > 0  && x != last_x)
            {   // Staircase from last 'y'..
                poly_x.add(x);
                poly_y.add(last_y);
                last_x = x;
            }
            if (Double.isNaN(value))
            {
                flushPolyLine(gc, poly_x, poly_y, line_width);
                last_x = last_y = -1;
            }
            else
            {
                final int y = clipY(y_axis.getScreenCoord(value));
                if (last_x == x  &&  last_y == y)
                    continue;
                poly_x.add(x);
                poly_y.add(y);
                last_y = y;
            }
        }
        flushPolyLine(gc, poly_x, poly_y, line_width);
    }

    /** Draw values of data as direct line
     *  @param gc GC
     *  @param x_transform Horizontal axis
     *  @param y_axis Value axis
     *  @param data Data
     *  @param line_width
     */
//    final private void drawValueLines(final GC gc,
//            final ScreenTransform<XTYPE> x_transform, final YAxisImpl<XTYPE> y_axis,
//            final PlotDataProvider<XTYPE> data, final int line_width)
//    {
//        final IntList value_poly = new IntList(INITIAL_ARRAY_SIZE);
//        final int N = data.size();
//        gc.setLineWidth(line_width);
//        int last_x = -1, last_y = -1;
//        for (int i=0; i<N; ++i)
//        {
//            final PlotDataItem<XTYPE> item = data.get(i);
//            final int x = clipX(Math.round(x_transform.transform(item.getPosition())));
//            final double value = item.getValue();
//            if (Double.isNaN(value))
//                flushPolyLine(gc, value_poly, line_width);
//            else
//            {
//                final int y = clipY(y_axis.getScreenCoord(value));
//                if (x == last_x  &&  y == last_y)
//                    continue;
//                value_poly.add(x);
//                value_poly.add(y);
//                last_x = x;
//                last_y = y;
//            }
//        }
//        flushPolyLine(gc, value_poly, line_width);
//    }

    /** Draw min/max outline
     *  @param gc GC
     *  @param x_transform Horizontal axis
     *  @param y_axis Value axis
     *  @param data Data
     */
//    final private void drawMinMaxArea(final Graphics2D gc,
//            final ScreenTransform<XTYPE> x_transform, final YAxisImpl<XTYPE> y_axis,
//            final PlotDataProvider<XTYPE> data)
//    {
//        final IntList pos = new IntList(INITIAL_ARRAY_SIZE);
//        final IntList min = new IntList(INITIAL_ARRAY_SIZE);
//        final IntList max = new IntList(INITIAL_ARRAY_SIZE);
//
//        final int N = data.size();
//        for (int i = 0;  i < N;  ++i)
//        {
//            final PlotDataItem<XTYPE> item = data.get(i);
//            double ymin = item.getMin();
//            double ymax = item.getMax();
//            if (Double.isNaN(ymin)  ||  Double.isNaN(ymax))
//                flushPolyFill(gc, pos, min, max);
//            else
//            {
//                final int x1 = clipX(x_transform.transform(item.getPosition()));
//                final int y1min = clipY(y_axis.getScreenCoord(ymin));
//                final int y1max = clipY(y_axis.getScreenCoord(ymax));
//                pos.add(x1);
//                min.add(y1min);
//                max.add(y1max);
//            }
//        }
//        flushPolyFill(gc, pos, min, max);
//    }

    /** Draw min/max outline
     *  @param gc GC
     *  @param x_transform Horizontal axis
     *  @param y_axis Value axis
     *  @param data Data
     */
//    final private void drawMinMaxLines(final Graphics2D gc,
//            final ScreenTransform<XTYPE> x_transform, final YAxisImpl<XTYPE> y_axis,
//            final PlotDataProvider<XTYPE> data, final int line_width)
//    {
//        final IntList min = new IntList(INITIAL_ARRAY_SIZE);
//        final IntList max = new IntList(INITIAL_ARRAY_SIZE);
//
//        final int N = data.size();
//        for (int i = 0;  i < N;  ++i)
//        {
//            final PlotDataItem<XTYPE> item = data.get(i);
//            double ymin = item.getMin();
//            double ymax = item.getMax();
//            if (Double.isNaN(ymin)  ||  Double.isNaN(ymax))
//            {
//                flushPolyLine(gc, min, line_width);
//                flushPolyLine(gc, max, line_width);
//            }
//            else
//            {
//                final int x1 = clipX(x_transform.transform(item.getPosition()));
//                final int y1min = clipY(y_axis.getScreenCoord(ymin));
//                final int y1max = clipY(y_axis.getScreenCoord(ymax));
//                min.add(x1);   min.add(y1min);
//                max.add(x1);   max.add(y1max);
//            }
//        }
//        flushPolyLine(gc, min, line_width);
//        flushPolyLine(gc, max, line_width);
//    }

    /** Draw std. deviation outline
     *  @param gc GC
     *  @param x_transform Horizontal axis
     *  @param y_axis Value axis
     *  @param data Data
     *  @param line_width
     */
//    final private void drawStdDevLines(final Graphics2D gc, final ScreenTransform<XTYPE> x_transform, final YAxisImpl<XTYPE> y_axis,
//            final PlotDataProvider<XTYPE> data, final int line_width)
//    {
//        final IntList lower_poly = new IntList(INITIAL_ARRAY_SIZE);
//        final IntList upper_poly = new IntList(INITIAL_ARRAY_SIZE);
//
//        final int N = data.size();
//        for (int i = 0;  i < N;  ++i)
//        {
//            final PlotDataItem<XTYPE> item = data.get(i);
//            double value = item.getValue();
//            double dev = item.getStdDev();
//            if (Double.isNaN(value) ||  ! (dev > 0))
//            {
//                flushPolyLine(gc, lower_poly, line_width);
//                flushPolyLine(gc, upper_poly, line_width);
//            }
//            else
//            {
//                final int x = clipX(x_transform.transform(item.getPosition()));
//                final int low_y = clipY(y_axis.getScreenCoord(value - dev));
//                final int upp_y = clipY(y_axis.getScreenCoord(value + dev));
//                lower_poly.add(x);  lower_poly.add(low_y);
//                upper_poly.add(x);  upper_poly.add(upp_y);
//            }
//        }
//        flushPolyLine(gc, lower_poly, line_width);
//        flushPolyLine(gc, upper_poly, line_width);
//    }

    /** @param gc GC
     *  @param poly Points of poly line, will be cleared
     *  @param line_width
     */
    final private void flushPolyLine(final Graphics2D gc, final IntList poly_x, final IntList poly_y, final int line_width)
    {
        final int N = poly_x.size();
        if (N == 1)
            drawPoint(gc, poly_x.get(0), poly_y.get(0), line_width);
        else if (N > 1)
            gc.drawPolyline(poly_x.getArray(), poly_y.getArray(), N);
        poly_x.clear();
        poly_y.clear();
    }

    /** Draw values of data as direct line
     *  @param gc GC
     *  @param x_transform Horizontal axis
     *  @param y_axis Value axis
     *  @param data Data
     *  @param point_type
     *  @param size
     */
    final private void drawPoints(final Graphics2D gc,
            final ScreenTransform<XTYPE> x_transform, final YAxisImpl<XTYPE> y_axis,
            final PlotDataProvider<XTYPE> data, PointType point_type, final int size)
    {
        final int N = data.size();
        int last_x = -1, last_y = -1;
        for (int i=0; i<N; ++i)
        {
            final PlotDataItem<XTYPE> item = data.get(i);
            final int x = clipX(Math.round(x_transform.transform(item.getPosition())));
            final double value = item.getValue();
            if (!Double.isNaN(value))
            {
                final int y = clipY(y_axis.getScreenCoord(value));
                if (x == last_x  &&  y == last_y)
                    continue;
                switch (point_type)
                {
                case SQUARES:
                    gc.fillRect(x-size/2, y-size/2, size, size);
                    break;
                case DIAMONDS:
                    gc.fillPolygon(new int[] { x,        x+size/2, x,        x-size/2     },
                                   new int[] { y-size/2, y,        y+size/2, y            }, 4);
                    break;
                case XMARKS:
                    gc.drawLine(x-size/2, y-size/2, x+size/2, y+size/2);
                    gc.drawLine(x-size/2, y+size/2, x+size/2, y-size/2);
                    break;
                case TRIANGLES:
                    gc.fillPolygon(new int[] { x,        x+size/2, x-size/2 },
                                   new int[] { y-size/2, y+size/2, y+size/2 }, 3);
                    break;
                case CIRCLES:
                default:
                    drawPoint(gc, x, y, size);
                }
                last_x = x;
                last_y = y;
            }
        }
    }

    /** @param gc GC
     *  @param x Coordinate
     *  @param y .. of point on screen
     *  @param size
     */
    final private void drawPoint(final Graphics2D gc, final int x, final int y, final int size)
    {
        gc.fillOval(x-size/2, y-size/2, size, size);
    }

    /** Fill area. All lists will be cleared.
     *  @param gc GC
     *  @param pos Horizontal screen positions
     *  @param min Minimum 'y' values in screen coords
     *  @param max .. maximum
     */
//    @SuppressWarnings("unused")
//    final private void flushPolyFill(final Graphics2D gc, final IntList pos, final IntList min, final IntList max)
//    {
//        final int N = pos.size();
//        if (N <= 0)
//            return;
//
//        if (true)
//        {
//            // 'direct' outline, point-to-point
//            // Turn pos/min/max into array required by fillPolygon:
//            // pos[0], min[0], pos[1], min[1], ..., pos[N-1], max[N-1], pos[N], max[N]
//            final int N4 = N * 4;
//            final int points[] = new int[N4];
//            int head = 0, tail = N4;
//            for (int i=0; i<N; ++i)
//            {
//                points[head++] = pos.get(i);
//                points[head++] = min.get(i);
//                points[--tail] = max.get(i);
//                points[--tail] = pos.get(i);
//            }
//            gc.fillPolygon(points);
//        }
//        else
//        {
//            // 'staircase' outline
//            final int points[] = new int[8*N-4];
//
//            int p = 0;
//            points[p++] = pos.get(0);
//            int ly = points[p++] = min.get(0);
//            for (int i=1; i<N; ++i)
//            {
//                points[p++] = pos.get(i);
//                points[p++] = ly;
//                points[p++] = pos.get(i);
//                ly = points[p++] = min.get(i);
//            }
//
//            int lx = points[p++] = pos.get(N-1);
//            points[p++] = max.get(N-1);
//            for (int i=N-2; i>=0; --i)
//            {
//                points[p++] = lx;
//                points[p++] = max.get(i);
//                lx = points[p++] = pos.get(i);
//                points[p++] = max.get(i);
//            }
//            gc.fillPolygon(points);
//        }
//
//        pos.clear();
//        min.clear();
//        max.clear();
//    }
}
