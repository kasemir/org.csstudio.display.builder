/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetMacros;

import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo;
import org.csstudio.display.builder.representation.ToolkitRepresentation;

/** Action Helper
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ActionUtil
{
    private static final Logger logger = Logger.getLogger(ActionUtil.class.getName());

    /** Handle an action
     *  @param source_widget Widget from which the action is invoked.
     *  @param action Information about the action to perform
     */
    public static void handleAction(final Widget source_widget, final ActionInfo action)
    {
        if (action instanceof OpenDisplayActionInfo)
        {   // Move off the UI Thread
            RuntimeUtil.getExecutor().execute(() -> openDisplay(source_widget, (OpenDisplayActionInfo) action));
        }
        else
            logger.log(Level.WARNING, "Cannot handle unknown " + action);
    }

    /** Open a display.
     *
     *  <p>Depending on the target of the action,
     *  this will open a new display or replace
     *  an existing display
     *
     * @param source_widget Widget from which the action is invoked.
     *                      Used to resolve the potentially relative path of the
     *                      display specified in the action.
     * @param action        Information on which display to open and how.
     */
    private static void openDisplay(final Widget source_widget,
                                    final OpenDisplayActionInfo action)
    {
        try
        {
            // Load new model. If that fails, no reason to continue.
            final DisplayModel widget_model = RuntimeUtil.getDisplayModel(source_widget);
            // Resolve new display path relative to the source widget, not the 'top'!
            final String parent_path = widget_model.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
            final String expanded_path =
                    MacroHandler.replace(source_widget.getEffectiveMacros(), action.getFile());
            final DisplayModel new_model = RuntimeUtil.loadModel(parent_path, expanded_path);

            // Model is standalone; The source_widget (Action button, ..) is _not_ the parent,
            // but it does provide set initial macros.
            new_model.setPropertyValue(widgetMacros, source_widget.getEffectiveMacros());

            // On UI thread...
            final DisplayModel top_model = RuntimeUtil.getTopDisplayModel(source_widget);
            final ToolkitRepresentation<Object, Object> toolkit = RuntimeUtil.getToolkit(top_model);
            if (action.getTarget() == OpenDisplayActionInfo.Target.REPLACE)
            {   // Replace the 'top'. Stop old runtime.
                RuntimeUtil.stopRuntime(top_model);
                final Future<Object> wait_for_ui = toolkit.submit(() ->
                {   // Close old representation
                    final Object parent = toolkit.disposeRepresentation(top_model);
                    // Replace top model with new content
                    top_model.replaceWith(new_model);
                    // Represent it
                    toolkit.representModel(parent, top_model);
                    return null;
                });
                // Back in background thread, create new runtime
                wait_for_ui.get();
                RuntimeUtil.startRuntime(top_model);
            }
            else
            {
                final Future<Object> wait_for_ui = toolkit.submit(() ->
                {   // Create new top-level
                    // TODO Distinguish 'window'/'tab'
                    final Object parent = toolkit.openNewWindow(new_model, ActionUtil::handleClose);
                    // Represent it
                    toolkit.representModel(parent, new_model);
                    return null;
                });
                // Back in background thread, create new runtime
                wait_for_ui.get();
                RuntimeUtil.startRuntime(new_model);
            }
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Error handling " + action, ex);
        }
    }

    private static boolean handleClose(final DisplayModel model)
    {
        final ToolkitRepresentation<Object, Object> toolkit = RuntimeUtil.getToolkit(model);

        RuntimeUtil.stopRuntime(model);
        toolkit.disposeRepresentation(model);
        return true;
    }
}
