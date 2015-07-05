/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import org.csstudio.display.builder.model.ContainerWidget;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/** Constraint on the movement of the Tracker that snaps to other widgets
 *  @author Kay Kasemir
 */
public class TrackerSnapConstraint implements TrackerConstraint
{
    /** If number of widgets to check exceeds this threshold,
     *  the search is parallelized is into 2 sub-tasks
     */
    public static final int PARALLEL_THRESHOLD = 2;

    private final DisplayModel model;
    private final List<Widget>selected_widgets;
    private final double snap_distance = 10;

    /** Horizontal and/or vertical position to which we 'snapped' */
    private static class SnapResult
    {
        final static double INVALID = Double.NEGATIVE_INFINITY;

        /** Horizontal coordinate to which to 'snap', or INVALID */
        double horiz = INVALID;

        /** Distance at which the horizontal snap was found */
        double horiz_distance = Double.MAX_VALUE;

        /** Vertical coordinate to which to 'snap', or INVALID */
        double vert = INVALID;

        /** Distance at which the vertical snap was found */
        double vert_distance = Double.MAX_VALUE;

        /** @param other Other result from which to use horiz or vert if they're closer */
        public void updateFrom(final SnapResult other)
        {
            if (other.horiz_distance < horiz_distance)
            {
                horiz = other.horiz;
                horiz_distance = other.horiz_distance;
            }
            if (other.vert_distance < vert_distance)
            {
                vert = other.vert;
                vert_distance = other.vert_distance;
            }
        }
    }

    /** Parallel search for snap points in list of widgets */
    private class SnapSearch extends RecursiveTask<SnapResult>
    {
        private static final long serialVersionUID = 7120422764377430462L;
        private final List<Widget> widgets;
        private final double x;
        private final double y;

        /** @param widgets Widgets to search
         *  @param x Requested X position
         *  @param y Requested Y position
         */
        SnapSearch(final List<Widget> widgets, final double x, final double y)
        {
            this.widgets = widgets;
            this.x = x;
            this.y = y;
        }

        @Override
        protected SnapResult compute()
        {
            return checkWidgets(widgets);
        }

        private SnapResult checkWidgets(final List<Widget> widgets)
        {
            final SnapResult result;
            final int N = widgets.size();
            if (N > PARALLEL_THRESHOLD)
            {
                final int split = N / 2;
                final SnapSearch sub1 = new SnapSearch(widgets.subList(0, split), x, y);
                final SnapSearch sub2 = new SnapSearch(widgets.subList(split, N), x, y);
                // Spawn sub1, handle sub2 in this thread, then combine results
                sub1.fork();
                result = sub2.compute();
                result.updateFrom(sub1.join());
            }
            else
            {
                result = new SnapResult();
                for (final Widget child : widgets)
                {
                    final SnapResult sub_result = checkWidget(x, y, child);
                    result.updateFrom(sub_result);
                }
            }
            return result;
        }

        /** @param x Requested X position
         *  @param y Requested Y position
         *  @param widget Widget where corners are checked as snap candidates
         *  @return {@link SnapResult}
         */
        private SnapResult checkWidget(final double x, final double y, final Widget widget)
        {
            // System.out.println("Checking " + widget.getClass().getSimpleName() + " in " + Thread.currentThread().getName());
            final SnapResult result = new SnapResult();

            // Do _not_ snap to one of the active widgets,
            // because that would lock their coordinates.
            if (selected_widgets.contains(widget))
                return result;

            // Check all widget corners
            final Rectangle2D bounds = GeometryTools.getDisplayBounds(widget);
            updateSnapResult(result, x, y, bounds.getMinX(), bounds.getMinY());
            updateSnapResult(result, x, y, bounds.getMaxX(), bounds.getMinY());
            updateSnapResult(result, x, y, bounds.getMaxX(), bounds.getMaxY());
            updateSnapResult(result, x, y, bounds.getMinX(), bounds.getMaxY());

            if (widget instanceof ContainerWidget)
                result.updateFrom(checkWidgets(((ContainerWidget)widget).getChildren()));
            return result;
        }

        /** @param result Result to update if this test point is closer
         *  @param x Requested X position
         *  @param y Requested Y position
         *  @param corner_x X coord of a widget corner
         *  @param corner_y Y coord of a widget corner
         */
        private void updateSnapResult(final SnapResult result,
                                      final double x, final double y,
                                      final double corner_x, final double corner_y)
        {
            // Determine distance of corner from requested point
            final double dx = Math.abs(corner_x - x);
            final double dy = Math.abs(corner_y - y);
            final double distance = dx*dx + dy*dy;

            // Horizontal snap, closer to what's been found before?
            if (dx < snap_distance  &&  distance < result.horiz_distance)
            {
                result.horiz = corner_x;
                result.horiz_distance = distance;
            }

            // Vertical snap, closer to what's been found before?
            if (dy < snap_distance  &&  distance < result.vert_distance)
            {
                result.vert = corner_y;
                result.vert_distance = distance;
            }
        }
    }

    /** @param model Model that provides widgets to snap to
     *  @param selected_widgets Currently selected widgets
     */
    public TrackerSnapConstraint(final DisplayModel model, final List<Widget> selected_widgets)
    {
        this.model = model;
        this.selected_widgets = selected_widgets;
    }

    @Override
    public Point2D constrain(double x, double y)
    {
        final SnapSearch task = new SnapSearch(Arrays.asList(model), x, y);
        final SnapResult result = task.compute();

        if (result.horiz != SnapResult.INVALID)
            x = result.horiz;
        if (result.vert != SnapResult.INVALID)
            y = result.vert;

        // TODO Show alignment guides

        return new Point2D(x, y);
    }
}
