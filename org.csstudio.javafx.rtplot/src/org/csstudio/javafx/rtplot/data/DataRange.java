/*******************************************************************************
 * Copyright (c) 2014-2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot.data;

import org.csstudio.javafx.rtplot.AxisRange;

/** Range for 'position' (X) and 'value' (Y) of plot data
 *  @param <XTYPE> Data type used for the {@link PlotDataItem}
 *  @author Kay Kasemir
 */
public class DataRange<XTYPE extends Comparable<XTYPE>>
{
	private final AxisRange<XTYPE> positions;
	private final ValueRange values;

    public DataRange(final XTYPE start, final XTYPE end, final double low, final double high)
    {
    	positions = new AxisRange<>(start, end);
    	values = new ValueRange(low, high);
    }

	public AxisRange<XTYPE> getPositions()
	{
		return positions;
	}

	public ValueRange getValues()
	{
		return values;
	}
	
    /** @return Returns low end of position axis. */
    final public XTYPE getStart()
    {
        return positions.getLow();
    }

    /** @return Returns high end of position axis. */
    final public XTYPE getEnd()
    {
        return positions.getHigh();
    }
	
    /** @return Returns low end of value range. */
    final public Double getLow()
    {
        return values.getLow();
    }

    /** @return Returns high end of value range. */
    final public Double getHigh()
    {
        return values.getHigh();
    }
}
