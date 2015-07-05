/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayText;

import java.beans.PropertyChangeEvent;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/** Creates SWT item for model widget
 *  @author Kay Kasemir
 */
public class LabelRepresentation extends SWTBaseRepresentation<Label, LabelWidget>
{
    private final DirtyFlag dirty_content = new DirtyFlag();

    public LabelRepresentation(final ToolkitRepresentation<Composite, Control> toolkit,
                               final LabelWidget model_widget)
    {
        super(toolkit, model_widget);
    }

    @Override
    protected Label createSWTControl(final Composite parent) throws Exception
    {
        return new Label(parent, SWT.NONE);
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.displayText().addPropertyListener(this::contentChanged);
    }

    private void contentChanged(final PropertyChangeEvent event)
    {
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_content.checkAndClear())
            control.setText(model_widget.getPropertyValue(displayText));
    }
}
