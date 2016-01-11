/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.actions;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.editor.tracker.SelectedWidgetUITracker;

/** Enable/disable grid
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EnableGridAction extends ActionDescription
{
    private final SelectedWidgetUITracker tracker;

    public EnableGridAction(final SelectedWidgetUITracker tracker)
    {
        super("platform:/plugin/org.csstudio.display.builder.editor/icons/grid.png",
              Messages.Grid);
        this.tracker = tracker;
    }

    @Override
    public void run(final boolean selected)
    {
        tracker.enableGrid(selected);
    }
}
