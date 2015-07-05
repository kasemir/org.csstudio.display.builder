/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt.widgets;

import java.beans.PropertyChangeEvent;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.TextUpdateWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.epics.vtype.VType;

/** Creates SWT item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TextUpdateRepresentation extends SWTBaseRepresentation<Label, TextUpdateWidget>
{
    private final DirtyFlag dirty_content = new DirtyFlag();
    private volatile String value_text = "<?>";

    public TextUpdateRepresentation(final ToolkitRepresentation<Composite, Control> toolkit,
                                    final TextUpdateWidget model_widget)
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
        model_widget.runtimeValue().addPropertyListener(this::contentChanged);
    }

    private void contentChanged(final PropertyChangeEvent event)
    {
        value_text = VTypeUtil.getValueString((VType)event.getNewValue(), true);
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_content.checkAndClear())
            control.setText(value_text);
    }
}
