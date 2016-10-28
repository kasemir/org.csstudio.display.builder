/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.editor;

import org.csstudio.javafx.rtplot.Activator;
import org.csstudio.javafx.rtplot.Messages;
import org.csstudio.javafx.rtplot.RTPlot;
import org.csstudio.javafx.rtplot.RTPlotListener;
/** Action that shows/hides the toolbar
 *  @author Kay Kasemir
 */
import org.eclipse.jface.action.Action;

@SuppressWarnings("nls")
public class ToggleToolbarAction extends Action
{
    // TODO Implement for TBD context menu

    final private RTPlot<?> plot;

    public ToggleToolbarAction(final RTPlot<?> plot, final boolean is_visible)
    {
        super(is_visible ? Messages.Toolbar_Hide : Messages.Toolbar_Show,
                Activator.getIconID("toolbar"));
        this.plot = plot;

        plot.addListener(new RTPlotListener()
        {
            @Override public void changedToolbar(boolean visible)
            {
                updateText();
            }
        });

        updateText();
    }
    //
    public void updateText()
    {
        setText(plot.isToolbarVisible() ? Messages.Toolbar_Hide : Messages.Toolbar_Show);
    }
    //
    @Override
    public void run()
    {
        plot.showToolbar(! plot.isToolbarVisible());
    }
}
