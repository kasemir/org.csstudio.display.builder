/*******************************************************************************
 * Copyright (c) 2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.opiwidget;

import org.csstudio.javafx.rtplot.RTTimePlot;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Action for context menu to show/hide toolbar
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ShowToolbarAction extends Action
{
    final private DataBrowserWidgedEditPart edit_part;

    public ShowToolbarAction(final DataBrowserWidgedEditPart edit_part)
    {
        super(Messages.ShowToolbar,
              AbstractUIPlugin.imageDescriptorFromPlugin("org.csstudio.javafx.rtplot", "icons/toolbar.png"));
        this.edit_part = edit_part;
        updateDescription();
    }

    private RTTimePlot getPlot()
    {
        final DataBrowserWidgetFigure gui = (DataBrowserWidgetFigure) edit_part.getFigure();
        return gui.getDataBrowserPlot().getPlot();
    }

    private void updateDescription()
    {
        final RTTimePlot plot = getPlot();
        setText(plot.isToolbarVisible() ? Messages.HideToolbar : Messages.ShowToolbar);
    }

    @Override
    public void run()
    {
        final RTTimePlot plot = getPlot();
        plot.showToolbar(! plot.isToolbarVisible());
        updateDescription();
    }
}
