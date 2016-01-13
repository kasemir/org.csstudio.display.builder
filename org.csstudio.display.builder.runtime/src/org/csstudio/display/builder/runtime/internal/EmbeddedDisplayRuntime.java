/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.csstudio.display.builder.runtime.WidgetRuntime;

/** Runtime for the {@link EmbeddedDisplayWidget}
 *
 *  <p>Loads, represents and runs the content of the embedded widget
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EmbeddedDisplayRuntime extends WidgetRuntime<EmbeddedDisplayWidget>
{
    /** Model for the embedded content */
    private volatile DisplayModel content_model;

    /** Start: Connect to PVs, ...
     *  @throws Exception on error
     */
    @Override
    public void start() throws Exception
    {
        super.start();

        // Load initial content, then register for changes.
        // Loading the content reads the displayFile property,
        // which resolves macros and could trigger a property change.
        loadContent();
        // Registering for changes after the initial load
        // prevents double-loading based on the change triggered in
        // the initial load
        widget.displayFile().addPropertyListener((p, o, n) -> loadContent());
    }

    private void loadContent()
    {
        try
        {
            // Load model for displayFile, allowing lookup relative to this widget's model
            final DisplayModel model = widget.getDisplayModel();
            final String parent_display = model.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
            final String display_file = widget.displayFile().getValue();
            content_model = RuntimeUtil.loadModel(parent_display, display_file);
            // Adjust model name to reflect source file
            content_model.widgetName().setValue("EmbeddedDisplay " + display_file);

            // Attach toolkit to embedded model
            final ToolkitRepresentation<Object, ?> toolkit = RuntimeUtil.getToolkit(model);

            // Tell embedded model that it is held by this widget
            content_model.setUserData(DisplayModel.USER_DATA_EMBEDDING_WIDGET, widget);

            // Represent on UI thread
            toolkit.execute(() -> representContent(toolkit));
        }
        catch (final Exception ex)
        {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    "Failed to load embedded display", ex);
        }
    }

    private void representContent(final ToolkitRepresentation<Object, ?> toolkit)
    {
        try
        {
            final Object parent = widget.getUserData(EmbeddedDisplayWidget.USER_DATA_EMBEDDED_DISPLAY_CONTAINER);
            toolkit.representModel(parent, content_model);

            // Start runtimes of child widgets off the UI thread
            RuntimeUtil.getExecutor().execute(() -> RuntimeUtil.startRuntime(content_model));
        }
        catch (final Exception ex)
        {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    "Failed to represent embedded display", ex);
        }
    }

    @Override
    public void stop()
    {
        RuntimeUtil.stopRuntime(content_model);
        super.stop();
    }
}
