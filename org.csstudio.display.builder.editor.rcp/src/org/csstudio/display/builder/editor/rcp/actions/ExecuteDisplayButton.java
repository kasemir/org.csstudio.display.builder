/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import org.csstudio.display.builder.editor.rcp.DisplayEditorPart;
import org.csstudio.display.builder.editor.rcp.Messages;
import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.OpenDisplayAction;
import org.eclipse.core.runtime.NullProgressMonitor;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/** Execute currently edited display
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ExecuteDisplayButton extends Button
{
    /** Progress monitor that opens display when 'done' */
    private class MonitorSaveThenOpenDisplay extends NullProgressMonitor
    {
        @Override
        public void done()
        {
            if (isCanceled())
                return;
            final DisplayInfo info = edit_part.getDisplayInfo();
            new OpenDisplayAction(info).run();
        }
    }

    private final DisplayEditorPart edit_part;

    public ExecuteDisplayButton(final DisplayEditorPart edit_part)
    {
        setGraphic(new ImageView(new Image("platform:/plugin/org.csstudio.display.builder.editor.rcp/icons/execute.png")));
        setTooltip(new Tooltip(Messages.ExecuteDisplay));
        this.edit_part = edit_part;

        setOnAction(event -> edit_part.doSave(new MonitorSaveThenOpenDisplay()));
    }
}
