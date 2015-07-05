/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetName;

import java.beans.PropertyChangeEvent;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/** Creates SWT item for model widget
 *  @author Kay Kasemir
 */
public class GroupRepresentation extends SWTBaseRepresentation<Group, GroupWidget>
{
    private final DirtyFlag dirty_border = new DirtyFlag();

    public GroupRepresentation(final ToolkitRepresentation<Composite, Control> toolkit,
	                           final GroupWidget model_widget)
    {
        super(toolkit, model_widget);
    }

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
        model_widget.widgetName().addPropertyListener(this::borderChanged);
        model_widget.positionWidth().addPropertyListener(this::borderChanged);
        model_widget.positionHeight().addPropertyListener(this::borderChanged);
    }

    private void borderChanged(final PropertyChangeEvent event)
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
            control.setText(model_widget.getPropertyValue(widgetName));
        }
    }
}
