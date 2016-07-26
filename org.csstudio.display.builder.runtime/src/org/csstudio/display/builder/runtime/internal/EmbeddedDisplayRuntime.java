/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget.Resize;
import org.csstudio.display.builder.model.widgets.GroupWidget;
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
    /** Timeout used to await UI thread operations to prevent deadlock */
    private static final long TIMEOUT_MS = 5000;

    /** Complete task chain for a new display file:
     *  1) Load model for display file (Runtime executor)
     *  2) Represent model in toolkit (Toolkit executor)
     *  3) Start runtime for model (Runtime executor)
     *
     *  The active model is between steps 1 & 2,
     *  where the old model is then disposed before
     *  representing and starting the new one.
     */
    private final AtomicReference<DisplayModel> active_content_model = new AtomicReference<>();

    /** Start: Connect to PVs, ..., then start the task chain for the display file
     *  @throws Exception on error
     */
    @Override
    public void start() throws Exception
    {
        super.start();

        // Reading the displayFile property resolves macros and could trigger a property change.
        // -> Resolve name, _then_ register for changes.
        final String display_file = widget.displayFile().getValue();

        // Load embedded display in runtime thread
        // so that start() returns quickly and allows the next widget to start up
        RuntimeUtil.getExecutor().execute(() -> startDisplayUpdate(display_file));

        // Even though display is still loading, file name has been resolved
        // so can now register for changes
        widget.displayFile().addPropertyListener((p, o, n) ->
        {
            // Runtime changes to the displayFile are expected to set the value,
            // not the macroized specification, so this getValue() call is very
            // unlikely to trigger a nested property update

            // Handle update of embedded display in the thread that triggered it
            startDisplayUpdate(widget.displayFile().getValue());
        });
    }

    /** Start task chain for display update
     *  @param display_file Path to display
     */
    private void startDisplayUpdate(final String display_file)
    {
        try
        {
            final DisplayModel display = widget.getDisplayModel();
            final ToolkitRepresentation<Object, ?> toolkit = ToolkitRepresentation.getToolkit(display);
            // Load new model (potentially slow)
            final DisplayModel new_model = loadDisplayModel(display_file);

            // If group name property is set, remove widgets that aren't groups with matching name
            if (! widget.displayGroupName().isDefaultValue())
            {
                final String group_name = widget.displayGroupName().getValue();
                final List<Widget> children = new_model.runtimeChildren().getValue();
                int index = 0;
                while (!children.isEmpty() && index < children.size())
                {
                    final Widget child = children.get(index);
                    if (child.getType().equals(GroupWidget.WIDGET_DESCRIPTOR.getType()) &&
                            child.getName().equals(group_name))
                    {
                        index++;
                    }
                    else
                        new_model.runtimeChildren().removeChild(child);
                }

                if (new_model.runtimeChildren().getValue().isEmpty())
                    logger.log(Level.WARNING, "Cannot locate group named '" + group_name + "' in " + display_file);
            }

            // Atomically update the 'active' model
            final DisplayModel old_model = active_content_model.getAndSet(new_model);

            if (old_model != null)
            {   // Dispose old model
                stopActiveContentRuntime(old_model);
                final Future<Object> completion = toolkit.submit(() ->
                {
                    toolkit.disposeRepresentation(old_model);
                    return null;
                });
                checkCompletion(completion, "timeout disposing old representation");
            }

            // Represent new model on UI thread
            final Future<Object> completion = toolkit.submit(() ->
            {
                representContent(toolkit, new_model);
                return null;
            });
            checkCompletion(completion, "timeout representing new content");

            // Back off UI thread, start runtimes of child widgets
            RuntimeUtil.startRuntime(new_model);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Failed to handle embedded display " + display_file, ex);
        }
    }

    /** Wait for future to complete
     *
     *  .. with timeout in case the UI thread cannot execute the submitted task right now.
     *
     *  <p>Intermediate versions of the embedded widget code
     *  experienced a deadlock when the UI was shut down, i.e. UI tried to dispose content,
     *  while at the same time a script was updating the content, also using the UI thread
     *  to create the new representation.
     *  The deadlock resulted from each were waiting on each other.
     *  Using a timeout, then moving on without waiting for the submitted UI thread,
     *  would resolve that deadlock.
     *
     *  @param completion
     *  @param message
     *  @throws Exception
     */
    private void checkCompletion(final Future<Object> completion, final String message) throws Exception
    {
        try
        {
            completion.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException timeout)
        {
            logger.log(Level.WARNING, message + " for " + widget);
        }
    }

    /** Load display file
     *  @param display_file Path to display
     *  @return DisplayModel that was loaded
     *  @throws Exception on error
     */
    private DisplayModel loadDisplayModel(final String display_file) throws Exception
    {
        DisplayModel embedded_model;
        if (display_file.isEmpty())
        {   // Empty model for empty file name
            embedded_model = new DisplayModel();
            widget.runtimeConnected().setValue(true);
        }
        else
        {
            try
            {   // Load model for displayFile, allowing lookup relative to this widget's model
                final DisplayModel display = widget.getDisplayModel();
                final String parent_display = display.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
                embedded_model = RuntimeUtil.loadModel(parent_display, display_file);
                // Adjust model name to reflect source file
                embedded_model.widgetName().setValue("EmbeddedDisplay " + display_file);
                widget.runtimeConnected().setValue(true);
            }
            catch (final Throwable ex)
            {   // Log error and show message in pseudo model
                final String message = "Failed to load embedded display '" + display_file + "'";
                logger.log(Level.WARNING, message, ex);
                embedded_model = createErrorModel(message);
                widget.runtimeConnected().setValue(false);
            }
        }
        // Tell embedded model that it is held by this widget
        embedded_model.setUserData(DisplayModel.USER_DATA_EMBEDDING_WIDGET, widget);
        return embedded_model;
    }

    /** @param message Error message
     *  @return DisplayModel that shows the message
     */
    private DisplayModel createErrorModel(final String message)
    {
        final LabelWidget info = new LabelWidget();
        info.displayText().setValue(message);
        info.displayForegroundColor().setValue(WidgetColorService.getColor(NamedWidgetColors.ALARM_DISCONNECTED));
        // Size a little smaller than the widget to fill but not require scrollbars
        final int wid = widget.positionWidth().getValue()-2;
        final int hei = widget.positionHeight().getValue()-2;
        info.positionWidth().setValue(wid);
        info.positionHeight().setValue(hei);
        final DisplayModel error_model = new DisplayModel();
        error_model.positionWidth().setValue(wid);
        error_model.positionHeight().setValue(hei);
        error_model.runtimeChildren().addChild(info);
        return error_model;
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
            logger.log(Level.WARNING, "Failed to represent embedded display", ex);
        }
    }

    /** @param content_model Model where runtime needs to be stopped. May be <code>null</code> */
    private void stopActiveContentRuntime(final DisplayModel content_model)
    {
        if (content_model == null)
            return;
        try
        {
            RuntimeUtil.stopRuntime(content_model);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Failed to stop embedded display runtime", ex);
        }
    }

    @Override
    public void stop()
    {
        stopActiveContentRuntime(active_content_model.getAndSet(null));
        super.stop();
    }
}
