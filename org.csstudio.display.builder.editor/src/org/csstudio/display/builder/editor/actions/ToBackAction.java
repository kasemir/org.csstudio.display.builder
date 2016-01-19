/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.actions;

import java.util.List;

import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.model.ContainerWidget;
import org.csstudio.display.builder.model.Widget;

/** Action to move widget to back
 *  @author Kay Kasemir
 */
public class ToBackAction extends ActionDescription
{
    private final WidgetSelectionHandler selection_handler;

    public ToBackAction(final WidgetSelectionHandler selection_handler)
    {
        super("platform:/plugin/org.csstudio.display.builder.editor/icons/toback.png", "Move widget to back");
        this.selection_handler = selection_handler;
    }

    @Override
    public void run(final boolean selected)
    {
        final List<Widget> widgets = selection_handler.getSelection();
        for (Widget widget : widgets)
        {
            final ContainerWidget parent = widget.getParent().orElse(null);
            if (parent == null)
                continue;

            // TODO Use undo
            parent.removeChild(widget);
            parent.addChild(0, widget);
        }
    }
}
