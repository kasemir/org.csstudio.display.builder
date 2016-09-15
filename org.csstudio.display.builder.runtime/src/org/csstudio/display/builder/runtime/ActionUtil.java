/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime;

import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.util.concurrent.Future;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.properties.ExecuteScriptActionInfo;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo;
import org.csstudio.display.builder.model.properties.WritePVActionInfo;
import org.csstudio.display.builder.representation.ToolkitRepresentation;

/** Action Helper
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ActionUtil
{
    /** Handle an action
     *  @param source_widget Widget from which the action is invoked.
     *  @param action Information about the action to perform
     */
    public static void handleAction(final Widget source_widget, final ActionInfo action)
    {
        // Move off the UI Thread
        if (action instanceof OpenDisplayActionInfo)
            RuntimeUtil.getExecutor().execute(() -> openDisplay(source_widget, (OpenDisplayActionInfo) action));
        else if (action instanceof WritePVActionInfo)
            RuntimeUtil.getExecutor().execute(() -> writePV(source_widget, (WritePVActionInfo) action));
        else if (action instanceof ExecuteScriptActionInfo)
            RuntimeUtil.getExecutor().execute(() -> executeScript(source_widget, (ExecuteScriptActionInfo) action));
        else
            logger.log(Level.WARNING, "Cannot handle unknown " + action);
    }

    /** Open a display
     *
     *  <p>Depending on the target of the action,
     *  this will open a new display or replace
     *  an existing display
     *
     *  @param source_widget Widget from which the action is invoked
     *                       Used to resolve the potentially relative path of the
     *                       display specified in the action
     *  @param action        Information on which display to open and how
     */
    private static void openDisplay(final Widget source_widget,
                                    final OpenDisplayActionInfo action)
    {
        if (action.getFile().isEmpty())
        {
            logger.log(Level.WARNING, "Action without file: {0}", action);
            return;
        }
        try
        {
            // Path to resolve, after expanding macros of source widget and action
            final Macros macros = Macros.merge(source_widget.getEffectiveMacros(), action.getMacros());
            final String expanded_path = MacroHandler.replace(macros, action.getFile());
            logger.log(Level.FINER, "{0}, effective macros {1} ({2})", new Object[] { action, macros, expanded_path });

            // Resolve new display file relative to the source widget model (not 'top'!)
            final DisplayModel widget_model = source_widget.getDisplayModel();
            final String parent_file = widget_model.getUserData(DisplayModel.USER_DATA_INPUT_FILE);

            // Load new model. If that fails, no reason to continue.
            final DisplayModel new_model = RuntimeUtil.loadModel(parent_file, expanded_path);

            // Model is standalone; source_widget (Action button, ..) is _not_ the parent,
            // but it does add macros to those already defined in the display file.
            final Macros combined_macros = Macros.merge(macros, new_model.propMacros().getValue());
            new_model.propMacros().setValue(combined_macros);

            // On UI thread...
            final DisplayModel top_model = RuntimeUtil.getTopDisplayModel(source_widget);
            final ToolkitRepresentation<Object, Object> toolkit = ToolkitRepresentation.getToolkit(top_model);
            if (action.getTarget() == OpenDisplayActionInfo.Target.REPLACE)
            {   // Replace the 'top'. Stop old runtime.
                RuntimeUtil.stopRuntime(top_model);
                final Future<Object> wait_for_ui = toolkit.submit(() ->
                {   // Close old representation
                    final Object parent = toolkit.disposeRepresentation(top_model);
                    // Tell toolkit about new model to represent
                    toolkit.representModel(parent, new_model);
                    return null;
                });
                // Back in background thread, create new runtime
                wait_for_ui.get();
                RuntimeUtil.startRuntime(new_model);
            }
            else
            {
                final Future<Object> wait_for_ui = toolkit.submit(() ->
                {   // Create new top-level window
                    // TODO Distinguish 'window'/'tab'
                    final ToolkitRepresentation<Object, Object> new_toolkit =
                        toolkit.openNewWindow(new_model, ActionUtil::handleClose);
                    if (new_toolkit != toolkit)
                        RuntimeUtil.hookRepresentationListener(new_toolkit);
                    return null;
                });
                // Back in background thread, create new runtime
                wait_for_ui.get();
                RuntimeUtil.startRuntime(new_model);
            }
        }
        catch (final Exception ex)
        {
            logger.log(Level.WARNING, "Error handling " + action, ex);
        }
    }

    /** Passed to newly opened windows to handle runtime shutdown
     *  when window is closed
     *  @param model Model for which runtime needs to be closed
     */
    public static void handleClose(final DisplayModel model)
    {
        final ToolkitRepresentation<Object, Object> toolkit = ToolkitRepresentation.getToolkit(model);
        RuntimeUtil.stopRuntime(model);
        toolkit.disposeRepresentation(model);
    }

    /** Write a PV
     *  @param source_widget Widget from which the action is invoked
     *  @param action        What to write to which PV
     */
    private static void writePV(final Widget source_widget, final WritePVActionInfo action)
    {
        final WidgetRuntime<Widget> runtime = RuntimeUtil.getRuntime(source_widget);
        try
        {
            runtime.writePV(action.getPV(), action.getValue());
        }
        catch (final Exception ex)
        {
            logger.log(Level.WARNING, action + " failed", ex);
        }
    }

    /** Execute script
     *  @param source_widget Widget from which the action is invoked
     *  @param action        Script action to execute
     */
    private static void executeScript(final Widget source_widget, final ExecuteScriptActionInfo action)
    {
        final WidgetRuntime<Widget> runtime = RuntimeUtil.getRuntime(source_widget);
        try
        {
            runtime.executeScriptAction(action);
        }
        catch (final Throwable ex)
        {
            logger.log(Level.WARNING, action + " failed", ex);
        }
    }
}
