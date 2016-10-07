/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets.plots;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.csstudio.javafx.rtplot.data.PlotDataItem;
import org.csstudio.javafx.rtplot.data.PlotDataProvider;
import org.csstudio.javafx.rtplot.data.SimpleDataItem;
import org.diirt.util.array.ArrayDouble;
import org.diirt.util.array.ListNumber;

/** Data provider for RTPlot
 *
 *  <p>Adapts two waveforms received from PV
 *  into samples for a trace in the RTPlot.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class XYVTypeDataProvider implements PlotDataProvider<Double>
{
    public final static ListNumber EMPTY = new ArrayDouble(new double[0], true);

    final private ReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile ListNumber x_data, y_data, error_data;

    public void setData(final ListNumber x_data, final ListNumber y_data, final ListNumber error_data) throws Exception
    {
        lock.writeLock().lock();
        try
        {
            // If both X and Y are provided, their size must match
            if (x_data != null  &&   x_data.size() != y_data.size())
            {
                this.x_data = null;
                this.y_data = null;
                throw new Exception("X/Y data size difference: " + x_data.size() + " vs. " + y_data.size());
            }
            // In principle, error_data should have 1 element or same size as X and Y..
            this.x_data = x_data;
            this.y_data = y_data;
            this.error_data = error_data;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Lock getLock()
    {
        return lock.readLock();
    }

    @Override
    public int size()
    {
        return y_data == null ? 0 : y_data.size();
    }

    @Override
    public PlotDataItem<Double> get(final int index)
    {
        final double x = x_data == null ? index : x_data.getDouble(index);
        final double y = y_data.getDouble(index);

        final double min, max;
        if (error_data.size() <= 0)
            min = max = Double.NaN; // No error data
        else
        {   // Use corresponding array element, or [0] for scalar error info
            // (silently treating size(error) < size(Y) as a mix of error array and scalar)
            final double error = (error_data.size() > index) ? error_data.getDouble(index) : error_data.getDouble(0);
            min = y - error;
            max = y + error;
        }
        return new SimpleDataItem<Double>(x, y, Double.NaN, min, max, null);
    }
}
