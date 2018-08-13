/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.editor;

import org.csstudio.javafx.rtplot.Messages;
import org.csstudio.javafx.rtplot.RTPlot;
import org.csstudio.javafx.rtplot.RTPlotListener;
import org.csstudio.javafx.rtplot.data.PlotDataItem;
import org.csstudio.trends.databrowser3.Activator;
import org.eclipse.jface.action.Action;

/** Action to hide/show legend.
 *
 *  @param <XTYPE> Data type used for the {@link PlotDataItem}
 *  @author Kunal Shroff - Original author
 *  @author Kay Kasemir
 *  @uathor Megan Grodowitz
 */
@SuppressWarnings("nls")
public class ToggleLegendAction extends Action
{
    final private RTPlot<?> plot;
    //
    public ToggleLegendAction(final RTPlot<?> plot)
    {
        super(Messages.Legend_Show, Action.AS_CHECK_BOX);
        setImageDescriptor(Activator.getRTPlotIconID("legend"));
        this.plot = plot;
        setChecked(plot.isLegendVisible());

        plot.addListener(new RTPlotListener()
        {
            @Override public void changedLegend(boolean visible)
            {
                update();
            }
        });
    }
    //
    public void update()
    {
        setChecked(plot.isLegendVisible());
    }
    //
    @Override
    public void run()
    {
        plot.showLegend(! plot.isLegendVisible());
    }
}
