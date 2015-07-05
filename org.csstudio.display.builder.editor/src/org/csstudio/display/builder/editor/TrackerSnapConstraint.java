/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.util.List;

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
    private final DisplayModel model;
    private final List<Widget>selected_widgets;
    private final double snap_distance = 5;

    private static class SnapResult
    {
        private double distance;
        private double pos;

        public synchronized void clear()
        {
            pos = Double.NaN;
        }

        public synchronized void update(final double pos, final double distance)
        {   // Already have a better match?
            if (Double.isFinite(this.pos)  &&  this.distance < distance)
                return;
            this.pos = pos;
        }

        public synchronized double get()
        {
            return pos;
        }
    }

    private final SnapResult horizontal_snap = new SnapResult();
    private final SnapResult vertical_snap = new SnapResult();

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
        // Clear search settings
        horizontal_snap.clear();
        vertical_snap.clear();

        checkWidget(x, y, model);

        // TODO Show alignment guides
        double pos = horizontal_snap.get();
        if (Double.isFinite(pos))
            x = pos;
        pos = vertical_snap.get();
        if (Double.isFinite(pos))
            y = pos;
        return new Point2D(x, y);
    }

    private void checkWidget(final double x, final double y, final Widget widget)
    {
        // Do _not_ snap to one of the active widgets,
        // because that would lock their coordinates.
        if (selected_widgets.contains(widget))
            return;
        // Check all widget corners
        final Rectangle2D bounds = GeometryTools.getDisplayBounds(widget);
        checkBounds(x, y, bounds.getMinX(), bounds.getMinY());
        checkBounds(x, y, bounds.getMaxX(), bounds.getMinY());
        checkBounds(x, y, bounds.getMaxX(), bounds.getMaxY());
        checkBounds(x, y, bounds.getMinX(), bounds.getMaxY());

        if (widget instanceof ContainerWidget)
        {
            final List<Widget> children = ((ContainerWidget)widget).getChildren();
            // TODO Handle children in parallel
            for (final Widget child : children)
                checkWidget(x, y, child);
        }
    }

    private void checkBounds(final double x, final double y, final double bounds_x, final double bounds_y)
    {
        // Determine distance of corner from test point
        final double dx = Math.abs(bounds_x - x);
        final double dy = Math.abs(bounds_y - y);
        if (dx > snap_distance  &&  dy > snap_distance)
            return;

        // Found widget corner to which to snap
        final boolean snap_horizontal = dx < dy;

        // Is it closer than the so far best snap?
        final double distance = dx*dx + dy*dy;

        if (snap_horizontal)
            horizontal_snap.update(bounds_x, distance);
        else
            vertical_snap.update(bounds_y, distance);
    }
}
