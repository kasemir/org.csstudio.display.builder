/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/** Creates SWT item for model widget
 *  @author Kay Kasemir
 */
public class LabelRepresentation extends SWTBaseRepresentation<Label, LabelWidget>
{
    private final DirtyFlag dirty_content = new DirtyFlag();

    @Override
    protected Label createSWTControl(final Composite parent) throws Exception
    {
        return new Label(parent, SWT.NONE);
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.displayText().addUntypedPropertyListener(this::contentChanged);
    }

    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_content.checkAndClear())
            control.setText(model_widget.displayText().getValue());
    }
}
