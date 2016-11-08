/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.bobwidget;

import org.csstudio.display.builder.model.widgets.plots.XYPlotWidget;
import org.csstudio.display.builder.representation.javafx.widgets.RegionBaseRepresentation;
import org.csstudio.trends.databrowser3.ui.ModelBasedPlot;
import org.eclipse.swt.widgets.Shell;

import javafx.scene.layout.Pane;

/** OPI Figure that displays data browser plot on screen,
 *  holds a Data Browser Plot
 *
 *  @author Kay Kasemir
 */
public class DataBrowserWidgetJFX extends RegionBaseRepresentation<Pane, XYPlotWidget>
{
    /** Data Browser plot */
    private ModelBasedPlot plot;

    @Override
    public Pane createJFXNode() throws Exception
    {
        // Plot is only active in runtime mode, not edit mode
        //plot = new RTValuePlot(! toolkit.isEditMode());
        plot = new ModelBasedPlot(new Shell());
        //plot.showToolbar(false);
        //plot.showCrosshair(false);
        return plot.getPlot();
    }

    /** @return Data Browser Plot */
    public ModelBasedPlot getDataBrowserPlot()
    {
        return plot;
    }

    /** @return Tool bar visibility */
    public boolean isToolbarVisible()
    {
        return plot.getPlot().isToolbarVisible();
    }

    /** @param visible New tool bar visibility */
    public void setToolbarVisible(final boolean visible)
    {
        plot.getPlot().showToolbar(visible);
    }

    /** @return Legend visibility */
    public boolean isLegendVisible()
    {
        return plot.getPlot().isLegendVisible();
    }

    /** @param visible Legend visibility */
    public void setLegendVisible(final boolean visible)
    {
        plot.getPlot().showLegend(visible);
    }

    /** @param showValueLabels <code>true</code> if values should be visible */
    public void setShowValueLabels(final boolean showValueLabels)
    {
        plot.getPlot().showCrosshair(showValueLabels);
    }
}
