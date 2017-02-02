/*******************************************************************************
 * Copyright (c) 2014-2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot.internal.undo;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.display.builder.util.undo.UndoableAction;
import org.csstudio.javafx.rtplot.Axis;
import org.csstudio.javafx.rtplot.AxisRange;
import org.csstudio.javafx.rtplot.internal.Plot;
import org.csstudio.javafx.rtplot.internal.YAxisImpl;

/** Un-doable action to modify value range of axes
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ChangeAxisRanges<XTYPE extends Comparable<XTYPE>> extends UndoableAction
{
    final private Plot<XTYPE> plot;
    final private Axis<XTYPE> x_axis;
    final private AxisRange<XTYPE> original_x_range, new_x_range;
    final private List<YAxisImpl<XTYPE>> yaxes;
    final private List<AxisRange<Double>> original_yranges, new_yranges;
    final private List<Boolean> original_autoscale, new_autoscale;

    /** Complete axes change
     *  @param plot Plot
     *  @param name Name of the action
     *  @param x_axis X Axis or <code>null</code>
     *  @param original_x_range Original ..
     *  @param new_x_range .. and new X range, or <code>null</code>
     *  @param y_axes Y Axes or <code>null</code>
     *  @param original_y_ranges Original
     *  @param new_y_ranges .. and new value ranges, or <code>null</code>
     *  @param original_autoscale Original autoscale or <code>null</code>. The 'new' autoscale will be all <code>false</code>.
     */
    public ChangeAxisRanges(final Plot<XTYPE> plot,
            final String name,
            final Axis<XTYPE> x_axis,
            final AxisRange<XTYPE> original_x_range,
            final AxisRange<XTYPE> new_x_range,
            final List<YAxisImpl<XTYPE>> y_axes,
            final List<AxisRange<Double>> original_y_ranges,
            final List<AxisRange<Double>> new_y_ranges,
            final List<Boolean> original_autoscale)
    {
        super(name);
        this.plot = plot;
        this.x_axis = x_axis;
        this.original_x_range = original_x_range;
        this.new_x_range = new_x_range;
        this.yaxes = y_axes;
        this.original_yranges = original_y_ranges;
        this.new_yranges = new_y_ranges;
        this.original_autoscale = original_autoscale;
        if (yaxes == null)
            new_autoscale = null;
        else
        {
            if (y_axes.size() != original_y_ranges.size())
                throw new IllegalArgumentException(y_axes.size() + " Y axes, but " + original_y_ranges.size() + " orig. ranges");
            if (y_axes.size() != new_y_ranges.size())
                throw new IllegalArgumentException(y_axes.size() + " Y axes, but " + new_y_ranges.size() + " new ranges");
            if (original_autoscale == null)
                new_autoscale = null;
            else
            {
                if (y_axes.size() != original_autoscale.size())
                    throw new IllegalArgumentException(y_axes.size() + " Y axes, but " + original_autoscale.size() + " original autoscale");
                new_autoscale = new ArrayList<>(original_autoscale.size());
                for (int i=0; i<y_axes.size(); ++i)
                    new_autoscale.add(false);
            }
        }
    }

    /** X Axis change
     *  @param plot Plot
     *  @param name Name of the action
     *  @param x_axis X Axis or <code>null</code>
     *  @param original_x_range Original ..
     *  @param new_x_range .. and new X range, or <code>null</code>
     */
    public ChangeAxisRanges(final Plot<XTYPE> plot, final String name,
            final Axis<XTYPE> x_axis,
            final AxisRange<XTYPE> original_x_range,
            final AxisRange<XTYPE> new_x_range)
    {
        this(plot, name, x_axis, original_x_range, new_x_range, null, null, null, null);
    }

    /** Y axes change
     *  @param plot Plot
     *  @param name Name of the action
     *  @param y_axes Y Axes or <code>null</code>
     *  @param original_y_ranges Original
     *  @param new_y_ranges .. and new value ranges, or <code>null</code>
     *  @param original_autoscale Original autoscale or <code>null</code>. The 'new' autoscale will be all <code>false</code>.
     */
    public ChangeAxisRanges(final Plot<XTYPE> plot, final String name,
            final List<YAxisImpl<XTYPE>> y_axes,
            final List<AxisRange<Double>> original_y_ranges,
            final List<AxisRange<Double>> new_y_ranges,
            final List<Boolean> original_autoscale)
    {
        this(plot, name, null, null, null, y_axes, original_y_ranges, new_y_ranges, original_autoscale);
    }


    @Override
    public void run()
    {
        if (x_axis != null)
            setXRange(new_x_range);
        if (yaxes != null)
            setRange(new_yranges, new_autoscale);
    }

    @Override
    public void undo()
    {
        if (x_axis != null)
            setXRange(original_x_range);
        if (yaxes != null)
            setRange(original_yranges, original_autoscale);
    }

    private void setXRange(AxisRange<XTYPE> range)
    {
        if (x_axis.setValueRange(range.getLow(), range.getHigh()))
        {
            if (x_axis.isAutoscale())
                x_axis.setAutoscale(false);
            plot.fireXAxisChange();
        }
    }

    private void setRange(final List<AxisRange<Double>> ranges,
                          final List<Boolean> autoscale)
    {

        if (autoscale != null)
            System.out.println("Items: " + autoscale.size());
        for (int i=0; i<yaxes.size(); ++i)
        {
            final AxisRange<Double> range = ranges.get(i);
            final YAxisImpl<XTYPE> axis = yaxes.get(i);
            if (autoscale != null  &&  axis.setAutoscale(autoscale.get(i)))
                plot.fireAutoScaleChange(axis);
            if (axis.setValueRange(range.getLow(), range.getHigh()))
                plot.fireYAxisChange(axis);
        }
    }
}
