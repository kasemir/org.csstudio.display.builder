/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/** Creates SWT item for model widget
 *  @author Kay Kasemir
 */
public class GroupRepresentation extends SWTBaseRepresentation<Group, GroupWidget>
{
    private final DirtyFlag dirty_border = new DirtyFlag();

    @Override
    protected Group createSWTControl(final Composite parent) throws Exception
    {
        final Group group = new Group(parent, SWT.NO_FOCUS);
        return group;
    }

    @Override
    protected Composite getChildParent(final Composite parent)
    {
        return control;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.widgetName().addUntypedPropertyListener(this::borderChanged);
        model_widget.positionWidth().addUntypedPropertyListener(this::borderChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::borderChanged);
    }

    private void borderChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_border.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_border.checkAndClear())
        {
            control.setText(model_widget.widgetName().getValue());
        }
    }
}
