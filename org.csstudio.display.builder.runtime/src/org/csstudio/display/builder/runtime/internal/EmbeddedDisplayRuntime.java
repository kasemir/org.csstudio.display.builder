/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget.Resize;
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
    private volatile Future<?> active_task;

    /** Start: Connect to PVs, ...
     *  @throws Exception on error
     */
    @Override
    public void start() throws Exception
    {
        super.start();

        // Loading the content reads the displayFile property,
        // which resolves macros and could trigger a property change.
        // -> Resolve name & load initial content, _then_ register for changes.
        final String display_file = widget.displayFile().getValue();
        startDisplayUpdate(display_file);

        // Registering for changes after the initial load
        // prevents double-loading based on the change triggered in
        // the initial load
        widget.displayFile().addPropertyListener((p, o, n) -> startDisplayUpdate(widget.displayFile().getValue()));
    }

    /** Start background task for display update
     *  @param display_file Path to display
     */
    private void startDisplayUpdate(final String display_file)
    {
        // If there is already an ongoing load that has _not_ actually started,
        // cancel it. Don't interrupt one that's being executed because that could
        // result in partially created content.
        // In case many updates arrive in a burst, the initial, ongoing update will run to completion.
        // The intermediate updates will all cancel each other, and the final update will remain
        // as the "active_task" that's actually executed in the end.
        final Future<?> ongoing = active_task;
        if (ongoing != null)
            ongoing.cancel(false);
        active_task = RuntimeUtil.getExecutor().submit(() -> loadDisplayFile(display_file));
    }

    /** Load display file and schedule representation
     *  @param display_file Path to display
     */
    private void loadDisplayFile(final String display_file)
    {
        System.out.println("Loading " + display_file + " on " + Thread.currentThread().getName());
        // TODO Handle changed embedded display file:
        //      Stop runtime, dispose representation before loading/starting new one
        try
        {
            // Load model for displayFile, allowing lookup relative to this widget's model
            final DisplayModel model = widget.getDisplayModel();
            final String parent_display = model.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
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
        catch (final Throwable ex)
        {
            // TODO Show "Failed to load embedded display" + display_file in representation
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    "Failed to load embedded display " + display_file, ex);
        }
    }

    private void representContent(final ToolkitRepresentation<Object, ?> toolkit)
    {
        try
        {
            final Object parent = widget.getUserData(EmbeddedDisplayWidget.USER_DATA_EMBEDDED_DISPLAY_CONTAINER);

            final Resize resize = widget.displayResize().getValue();
            final int content_width = content_model.positionWidth().getValue();
            final int content_height = content_model.positionHeight().getValue();
            if (resize == Resize.ResizeContent)
            {
                final double zoom_x = content_width  > 0 ? (double)widget.positionWidth().getValue()  / content_width : 1.0;
                final double zoom_y = content_height > 0 ? (double)widget.positionHeight().getValue() / content_height : 1.0;
                final double zoom = Math.min(zoom_x, zoom_y);
                widget.runtimeScale().setValue(zoom);
            }
            else if (resize == Resize.SizeToContent)
            {
                if (content_width > 0)
                    widget.positionWidth().setValue(content_width);
                if (content_height > 0)
                    widget.positionHeight().setValue(content_height);
            }
            toolkit.representModel(parent, content_model);

            // Start runtimes of child widgets off the UI thread
            RuntimeUtil.getExecutor().execute(() -> RuntimeUtil.startRuntime(content_model));
        }
        catch (final Exception ex)
        {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    "Failed to represent embedded display", ex);
        }
        // Indicate that task completed
        active_task = null;
    }

    @Override
    public void stop()
    {
        RuntimeUtil.stopRuntime(content_model);
        super.stop();
    }
}
