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
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.XYPlotWidget;
import org.csstudio.display.builder.runtime.internal.DisplayRuntime;
import org.csstudio.display.builder.runtime.internal.EmbeddedDisplayRuntime;
import org.csstudio.display.builder.runtime.internal.XYPlotWidgetRuntime;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

/** Factory for runtimes
 *
 *  @author Kay Kasemir
 */
public class WidgetRuntimeFactory
{
    public static final WidgetRuntimeFactory INSTANCE = new WidgetRuntimeFactory();

    /** Supplier of a WidgetRuntime, may throw Exception */
    private static interface RuntimeSupplier
    {
        WidgetRuntime<? extends Widget> get() throws Exception;
    };

    /** Map widget type IDs to RuntimeSuppliers */
    private final Map<String, RuntimeSupplier> runtimes = new HashMap<>();

    /** Initialize available runtimes */
    private WidgetRuntimeFactory()
    {
        final IExtensionRegistry registry = RegistryFactory.getRegistry();
        if (registry == null)
        {   // Fall back to hardcoded runtimes
            runtimes.put(DisplayModel.WIDGET_TYPE, () -> new DisplayRuntime());
            runtimes.put(EmbeddedDisplayWidget.WIDGET_DESCRIPTOR.getType(), () -> new EmbeddedDisplayRuntime());
            runtimes.put(XYPlotWidget.WIDGET_DESCRIPTOR.getType(), () -> new XYPlotWidgetRuntime());
        }
        else
        {   // Locate runtimes in registry
            for (IConfigurationElement config : registry.getConfigurationElementsFor(WidgetRuntime.EXTENSION_POINT))
            {
                final String type = config.getAttribute("type");
                runtimes.put(type, createSupplier(config));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private RuntimeSupplier createSupplier(final IConfigurationElement config)
    {
        return () -> (WidgetRuntime<? extends Widget>) config.createExecutableExtension("class");
    }

    /** Create a runtime and initialize for widget
     *  @param model_widget
     *  @return {@link WidgetRuntime}
     *  @throws Exception on error
     */
    @SuppressWarnings("unchecked")
    public <MW extends Widget> WidgetRuntime<MW> createRuntime(final MW model_widget) throws Exception
    {
        // Locate registered Runtime, or use default
        final String type = model_widget.getType();
        final RuntimeSupplier runtime_class = runtimes.get(type);
        final WidgetRuntime<MW> runtime;
        if (runtime_class == null)
            // Use default runtime
            runtime = new WidgetRuntime<MW>();
        else
            // Use widget-specific runtime
            runtime = (WidgetRuntime<MW>) runtime_class.get();
        runtime.initialize(model_widget);
        return runtime;
    }
}
