/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.opiwidget;

import static org.csstudio.trends.databrowser3.Activator.logger;

import java.util.logging.Level;

import org.csstudio.javafx.swt.JFX_SWT_Wrapper;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.widgets.figures.AbstractSWTWidgetFigure;
import org.csstudio.trends.databrowser3.ui.ModelBasedPlot;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;

/** OPI Figure that displays data browser plot on screen,
 *  holds a Data Browser Plot
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DataBrowserWidgetFigure extends AbstractSWTWidgetFigure<Control>
{
    /** Data Browser plot */
    private ModelBasedPlot plot;

    /** Initialize
     *  @param filename Configuration file name
     */
    public DataBrowserWidgetFigure(final AbstractBaseEditPart editPart, final String selectionValuePv, final boolean showValueLabels)
    {
        super(editPart);
        // TODO selectionValuePv?
        setShowValueLabels(showValueLabels);
    }

    @Override
    protected Control createSWTWidget(final Composite parent, final int style)
    {
        final JFX_SWT_Wrapper wrapper = new JFX_SWT_Wrapper(parent, () ->
        {
            Parent root;
            try
            {
                plot = new ModelBasedPlot(editPart.getExecutionMode() == ExecutionMode.RUN_MODE);
                root = plot.getPlot();
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot create Data Browser OPI Widget's plot", ex);
                root = new Label("Cannot initialize Plot");
            }
            return new Scene(root);
        });
        return wrapper.getFXCanvas();
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
