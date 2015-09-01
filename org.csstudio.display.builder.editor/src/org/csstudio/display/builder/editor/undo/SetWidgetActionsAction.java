/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.display.builder.editor.undo;

import java.util.List;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.properties.ActionsWidgetProperty;

/** Action to update widget actions
 *  @author Kay Kasemir
 */
public class SetWidgetActionsAction extends UndoableAction
{
    private final ActionsWidgetProperty property;
    private final List<ActionInfo> orig_actions;
    private final List<ActionInfo> actions;

    public SetWidgetActionsAction(final ActionsWidgetProperty property,
                                 final List<ActionInfo> actions)
    {
        super(Messages.SetWidgetActions);
        this.property = property;
        this.orig_actions = property.getValue();
        this.actions = actions;
    }

    @Override
    public void run()
    {
        property.setValue(actions);
    }

    @Override
    public void undo()
    {
        property.setValue(orig_actions);
    }
}
