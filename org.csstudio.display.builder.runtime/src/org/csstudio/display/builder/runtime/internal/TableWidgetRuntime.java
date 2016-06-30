/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.csstudio.display.builder.model.widgets.TableWidget;
import org.csstudio.display.builder.runtime.RuntimeAction;
import org.csstudio.display.builder.runtime.WidgetRuntime;

/** Runtime for the TableWidget
 *
 *  @author Kay Kasemir
 */
public class TableWidgetRuntime  extends WidgetRuntime<TableWidget>
{
    private final List<RuntimeAction> runtime_actions = new ArrayList<>(1);

    @Override
    public void initialize(final TableWidget widget)
    {
        super.initialize(widget);
        if (widget.behaviorEditable().getValue())
            runtime_actions.add(new ToggleToolbarAction(widget));
    }

    @Override
    public Collection<RuntimeAction> getRuntimeActions()
    {
        return runtime_actions;
    }

    @Override
    public void start() throws Exception
    {
        super.start();
    }

    @Override
    public void stop()
    {
        super.stop();
    }
}
