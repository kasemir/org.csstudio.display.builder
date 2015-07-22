/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.display.builder.editor.undo;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;

/** Action to add widget
 *  @author Kay Kasemir
 */
public class AddWidgetAction extends UndoableAction
{
    private final DisplayModel model;
    private final Widget widget;

    public AddWidgetAction(final DisplayModel model, final Widget widget)
    {
        super(Messages.AddWidget);
        this.model = model;
        this.widget = widget;
    }

    @Override
    public void run()
    {
        model.addChild(widget);
    }

    @Override
    public void undo()
    {
        model.removeChild(widget);
    }
}
