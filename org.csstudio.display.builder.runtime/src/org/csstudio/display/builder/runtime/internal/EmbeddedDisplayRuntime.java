/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget.Resize;
import org.csstudio.display.builder.model.widgets.LabelWidget;
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
    /** Future for the embedded model.
     *
     *  Complete task chain for a display file:
     *  1) Load content_model for display file (Runtime executor)
     *  2) Represent content_model in toolkit (Toolkit executor)
     *  3) Start runtime for content_model (Runtime executor)
     *
     *  A Future is created for the complete task chain,
     *  returning the content_model on completion.
     *
     *  In principle, the future can be cancelled to prevent
     *  a scheduled but not yet executing task chain from starting,
     *  but interruption of an executing task chain is not permitted
     *  because it is unclear how for example a partially constructed
     *  representation would then be cleaned up.
     *  A task chain thus must run to completion.
     */
    private final AtomicReference<Future<DisplayModel>> active_content_model = new AtomicReference<>();

    /** Start: Connect to PVs, ..., then start the task chain for the display file
     *  @throws Exception on error
     */
    @Override
    public void start() throws Exception
    {
        super.start();

        // Reading the displayFile property resolves macros and could trigger a property change.
        // -> Resolve name & load initial content, _then_ register for changes.
        final String display_file = widget.displayFile().getValue();
        startDisplayUpdate(display_file);

        // Registering for changes after the initial load
        // prevents double-loading based on the change triggered in
        // the initial load.
        widget.displayFile().addPropertyListener((p, o, n) ->
        {
            // Runtime changes to the displayFile are expected to set the value,
            // not the macroized specification, so this getValue() call is very
            // unlikely to trigger a nested property update
            startDisplayUpdate(widget.displayFile().getValue());
        });
    }

    /** Start task chain for display update
     *  @param display_file Path to display
     */
    private void startDisplayUpdate(final String display_file)
    {
        // Create task chain for new file, which will wait for disposal of old content
        final CountDownLatch wait_for_old_content_disposal = new CountDownLatch(1);
        final Future<DisplayModel> new_chain = display_file.isEmpty()
            ? null
            : RuntimeUtil.getExecutor().submit(() ->
            {
                wait_for_old_content_disposal.await();
                return loadRepresentRun(display_file);
            });

        // Atomically get future of existing content and submit task chain for new display_file
        final Future<DisplayModel> old = active_content_model.getAndSet(new_chain);

        if (old != null)
        {   // Dispose old content
            final DisplayModel content_model = stopActiveContentRuntime(old);
            if (content_model != null)
            {
                try
                {
                    final DisplayModel model = widget.getDisplayModel();
                    final ToolkitRepresentation<Object, ?> toolkit = RuntimeUtil.getToolkit(model);
                    // TODO Time out
                    toolkit.submit(() -> { toolkit.disposeRepresentation(content_model); return null; }).get();
                }
                catch (Exception ex)
                {
                    Logger.getLogger(getClass().getName())
                          .log(Level.WARNING, "Failed to dispose representation for " + widget, ex);
                }
            }
        }

        // Allow submitted task chain to proceed
        // after potential old content has been disposed
        wait_for_old_content_disposal.countDown();
    }

    /** Load display file, schedule representation (on toolkit), start runtimes
     *  @param display_file Path to display
     *  @return DisplayModel that was handled or null on error
     *  @throws Exception on error
     */
    private DisplayModel loadRepresentRun(final String display_file) throws Exception
    {
        final DisplayModel model = widget.getDisplayModel();
        DisplayModel embedded_model;
        try
        {   // Load model for displayFile, allowing lookup relative to this widget's model
            final String parent_display = model.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
            embedded_model = RuntimeUtil.loadModel(parent_display, display_file);
            // Adjust model name to reflect source file
            embedded_model.widgetName().setValue("EmbeddedDisplay " + display_file);
            widget.runtimeConnected().setValue(true);
        }
        catch (final Throwable ex)
        {   // Log error and show message in pseudo model
            final String message = "Failed to load embedded display " + display_file;
            Logger.getLogger(getClass().getName()).log(Level.WARNING, message, ex);
            final LabelWidget info = new LabelWidget();
            info.displayText().setValue(message);
            info.displayForegroundColor().setValue(WidgetColorService.getColor(NamedWidgetColors.ALARM_DISCONNECTED));
            final int wid = widget.positionWidth().getValue()-2;
            final int hei = widget.positionHeight().getValue()-2;
            info.positionWidth().setValue(wid);
            info.positionHeight().setValue(hei);
            embedded_model = new DisplayModel();
            embedded_model.positionWidth().setValue(wid);
            embedded_model.positionHeight().setValue(hei);
            embedded_model.addChild(info);
            widget.runtimeConnected().setValue(false);
        }
        final DisplayModel content_model = embedded_model;

        try
        {
            // Attach toolkit to embedded model
            final ToolkitRepresentation<Object, ?> toolkit = RuntimeUtil.getToolkit(model);

            // Tell embedded model that it is held by this widget
            content_model.setUserData(DisplayModel.USER_DATA_EMBEDDING_WIDGET, widget);

            // Represent on UI thread
            final Future<Object> represented = toolkit.submit(() ->
            {
                representContent(toolkit, content_model);
                return null;
            });
            // Wait for completion
            // TODO Time out
            represented.get();

            // Back in runtime pool thread, start runtimes of child widgets
            RuntimeUtil.startRuntime(content_model);

            return content_model;
        }
        catch (final Throwable ex)
        {
            Logger.getLogger(getClass().getName())
                  .log(Level.WARNING, "Failed to start embedded display " + display_file, ex);
        }
        return null;
    }

    /** @param toolkit Toolkit to use for representation
     *  @param content_model Model to represent
     */
    private void representContent(final ToolkitRepresentation<Object, ?> toolkit,
                                  final DisplayModel content_model)
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
        }
        catch (final Exception ex)
        {
            Logger.getLogger(getClass().getName())
                  .log(Level.WARNING, "Failed to represent embedded display", ex);
        }
    }

    /** @param content_future Future that may hold model to stop
     *  @return Model that was stopped or <code>null</code>
     */
    private DisplayModel stopActiveContentRuntime(final Future<DisplayModel> content_future)
    {
        try
        {
            if (content_future != null)
            {   // TODO Hung here
                final DisplayModel content_model = content_future.get();
                if (content_model != null)
                {
                    RuntimeUtil.stopRuntime(content_model);
                    return content_model;
                }
            }
        }
        catch (Exception ex)
        {
            Logger.getLogger(getClass().getName())
                  .log(Level.WARNING, "Failed to stop embedded display runtime", ex);
        }
        return null;
    }

    @Override
    public void stop()
    {
        stopActiveContentRuntime(active_content_model.getAndSet(null));
        super.stop();
    }
}
