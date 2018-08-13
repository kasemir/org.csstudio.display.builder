/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.editor;

import org.csstudio.javafx.rtplot.Messages;
import org.csstudio.javafx.rtplot.RTPlot;
import org.csstudio.javafx.rtplot.RTPlotListener;
import org.csstudio.trends.databrowser3.Activator;
/** Action that shows/hides the toolbar
 *  @author Kay Kasemir
 */
import org.eclipse.jface.action.Action;

@SuppressWarnings("nls")
public class ToggleToolbarAction extends Action
{
    final private RTPlot<?> plot;

    public ToggleToolbarAction(final RTPlot<?> plot)
    {
        super(Messages.Toolbar_Show, Action.AS_CHECK_BOX);
        setImageDescriptor(Activator.getRTPlotIconID("toolbar"));
        this.plot = plot;
        setChecked(plot.isToolbarVisible());

        plot.addListener(new RTPlotListener()
        {
            @Override public void changedToolbar(boolean visible)
            {
                update();
            }
        });
    }

    public void update()
    {
        setChecked(plot.isToolbarVisible());
    }

    @Override
    public void run()
    {
        plot.showToolbar(! plot.isToolbarVisible());
    }
}
