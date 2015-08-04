/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.display.builder.editor.undo;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.ContainerWidget;
import org.csstudio.display.builder.model.Widget;

/** Action to add widget
 *  @author Kay Kasemir
 */
public class AddWidgetAction extends UndoableAction
{
    private final ContainerWidget container;
    private final Widget widget;

    public AddWidgetAction(final ContainerWidget container, final Widget widget)
    {
        super(Messages.AddWidget);
        this.container = container;
        this.widget = widget;
    }

    @Override
    public void run()
    {
        container.addChild(widget);
    }

    @Override
    public void undo()
    {
        container.removeChild(widget);
    }
}
