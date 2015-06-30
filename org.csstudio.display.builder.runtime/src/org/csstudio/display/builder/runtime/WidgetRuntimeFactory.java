/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime;

import java.util.HashMap;
import java.util.Map;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.widgets.ActionButtonWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.runtime.internal.ActionButtonRuntime;
import org.csstudio.display.builder.runtime.internal.DisplayRuntime;
import org.csstudio.display.builder.runtime.internal.EmbeddedDisplayRuntime;

/** Factory for runtimes
 *
 *  @author Kay Kasemir
 */
public class WidgetRuntimeFactory
{
    public static final WidgetRuntimeFactory INSTANCE = new WidgetRuntimeFactory();

    private final Map<Class<? extends Widget>,
                      Class<? extends WidgetRuntime<? extends Widget>>> runtimes = new HashMap<>();

    private WidgetRuntimeFactory()
    {
        // TODO Locate runtimes in registry
        register(DisplayModel.class, DisplayRuntime.class);

        register(ActionButtonWidget.class, ActionButtonRuntime.class);
        register(EmbeddedDisplayWidget.class, EmbeddedDisplayRuntime.class);
    }

    protected void register(final Class<? extends Widget> widget_class,
                            final Class<? extends WidgetRuntime<? extends Widget>> runtime_class)
    {
        runtimes.put(widget_class, runtime_class);
    }

    @SuppressWarnings("unchecked")
    public <MW extends Widget> WidgetRuntime<MW> createRuntime(final MW model_widget) throws Exception
    {
        // Locate registered Runtime, or use default
        final Class<? extends WidgetRuntime<?>> runtime_class = runtimes.get(model_widget.getClass());
        if (runtime_class == null)
            return new WidgetRuntime<MW>(model_widget);
        // return new ThatRuntime(widget);
        return (WidgetRuntime<MW>)
                runtime_class.getDeclaredConstructor(model_widget.getClass()).newInstance(model_widget);
    }
}
