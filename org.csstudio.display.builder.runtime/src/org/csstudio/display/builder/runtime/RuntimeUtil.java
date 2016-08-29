/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime;

import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.model.util.NamedDaemonPool;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.TabsWidget;
import org.csstudio.display.builder.model.widgets.TabsWidget.TabItemProperty;
import org.csstudio.display.builder.representation.ToolkitListener;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.runtime.script.internal.ScriptSupport;

/** Runtime Helper
 *
 *  <p>Model is unaware of representation and runtime,
 *  but runtime needs to attach certain pieces of information
 *  to the model.
 *  This is done via the 'user data' support of the {@link Widget}.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RuntimeUtil
{
    private static final ExecutorService executor = NamedDaemonPool.createThreadPool("DisplayRuntime");

    private static final ToolkitListener toolkit_listener = new ToolkitListener()
    {
        @Override
        public void handleAction(final Widget widget, final ActionInfo action)
        {
            ActionUtil.handleAction(widget, action);
        }

        @Override
        public void handleWrite(final Widget widget, final Object value)
        {
            final WidgetRuntime<Widget> runtime = getRuntime(widget);
            if (runtime == null)
                logger.log(Level.WARNING, "Widget " + widget + " has no runtime for writing " + value);
            else
                runtime.writePrimaryPV(value);
        }
    };

    /** Connect runtime listener to toolkit
     *  @param toolkit Toolkit that runtime needs to monitor
     */
    public static void hookRepresentationListener(final ToolkitRepresentation<?,?> toolkit)
    {
        // For representation in an RCP view, a "new" display
        // may actually just bring an existing display back to the front.
        // In that case, prevent double-subscription by first trying to
        // remove the listener.
        toolkit.removeListener(toolkit_listener);
        toolkit.addListener(toolkit_listener);
    }

    /** @return {@link ExecutorService} that should be used for runtime-related background tasks
     */
    public static ExecutorService getExecutor()
    {
        return executor;
    }

    /** Load model
     *
     *  @param parent_display Path to a 'parent' file, may be <code>null</code>
     *  @param display_file Model file
     *  @return {@link DisplayModel}
     *  @throws Exception on error
     */
    public static DisplayModel loadModel(final String parent_display, final String display_file) throws Exception
    {
        final String resolved_name = ModelResourceUtil.resolveResource(parent_display, display_file);
        final ModelReader reader = new ModelReader(ModelResourceUtil.openResourceStream(resolved_name));
        final DisplayModel model = reader.readModel();
        model.setUserData(DisplayModel.USER_DATA_INPUT_FILE, resolved_name);
        return model;
    }

    /** Locate top display model.
     *
     *  <p>For embedded displays, <code>getDisplayModel</code>
     *  only provides the embedded model.
     *  This method traverse up via the {@link EmbeddedDisplayWidget}
     *  to the top-level display model.
     *
     *  @param widget Widget within model
     *  @return Top-level {@link DisplayModel} for widget
     *  @throws Exception if widget is not part of a model
     */
    public static DisplayModel getTopDisplayModel(final Widget widget) throws Exception
    {
        DisplayModel model = widget.getDisplayModel();
        while (true)
        {
            final EmbeddedDisplayWidget embedder = model.getUserData(DisplayModel.USER_DATA_EMBEDDING_WIDGET);
            if (embedder == null)
                return model;
            model = getTopDisplayModel(embedder);
        }
    }

    /** Obtain script support
     *
     *  <p>Script support is associated with the top-level display model
     *  and initialized on first access, i.e. each display has its own
     *  script support. Embedded displays use the script support of
     *  their parent display.
     *
     *  @param widget Widget
     *  @return {@link ScriptSupport} for the widget's top-level display model
     *  @throws Exception on error
     */
    public static ScriptSupport getScriptSupport(final Widget widget) throws Exception
    {
        final DisplayModel model = getTopDisplayModel(widget);
        // During display startup, several widgets will concurrently request script support.
        // Assert that only one ScriptSupport is created.
        // Synchronizing on the model seems straight forward because this is about script support
        // for this specific model, but don't want to conflict with other code that may eventually
        // need to lock the model for other reasons.
        // So sync'ing on the ScriptSupport class
        synchronized (ScriptSupport.class)
        {
            ScriptSupport scripting = model.getUserData(Widget.USER_DATA_SCRIPT_SUPPORT);
            if (scripting == null)
            {
                // This takes about 3 seconds
                final long start = System.currentTimeMillis();
                scripting = new ScriptSupport();
                final long elapsed = System.currentTimeMillis() - start;
                logger.log(Level.FINE, "ScriptSupport created for {0} by {1} in {2} ms", new Object[] { model, widget, elapsed });
                model.setUserData(Widget.USER_DATA_SCRIPT_SUPPORT, scripting);
            }
            return scripting;
        }
    }

    /** @param widget Widget
     *  @return {@link WidgetRuntime} of the widget or <code>null</code>
     */
    public static <MW extends Widget> WidgetRuntime<MW> getRuntime(final MW widget)
    {
        return widget.getUserData(Widget.USER_DATA_RUNTIME);
    }

    /** Create and start runtime for all widgets in the model
     *  @param model {@link DisplayModel}
     */
    public static void startRuntime(final DisplayModel model)
    {
        startRuntimeRecursively(model);
    }

    // Actually start runtimes from this widget down
    private static void startRuntimeRecursively(final Widget widget)
    {
        try
        {
            final WidgetRuntime<Widget> runtime = WidgetRuntimeFactory.INSTANCE.createRuntime(widget);
            runtime.start();
        }
        catch (final Exception ex)
        {
            logger.log(Level.SEVERE, "Cannot start widget runtime", ex);
        }

        if (widget instanceof TabsWidget)
            for (TabItemProperty tab : ((TabsWidget)widget).displayTabs().getValue())
                for (final Widget child : tab.children().getValue())
                    startRuntimeRecursively(child);

        // Recurse into child widgets
        final ChildrenProperty children = ChildrenProperty.getChildren(widget);
        if (children != null)
            for (final Widget child : children.getValue())
                startRuntimeRecursively(child);
    }

    /** Stop runtime for all widgets in the model
     *  @param model {@link DisplayModel}
     */
    public static void stopRuntime(final DisplayModel model)
    {
        stopRuntimeRecursively(model);
    }

    // Actually stop runtimes from this widget down
    private static void stopRuntimeRecursively(final Widget widget)
    {
        // Mirror-image of startRuntimeRecursively:
        // First recurse into child widgets, ..
        final ChildrenProperty children = ChildrenProperty.getChildren(widget);
        if (children != null)
            for (final Widget child : children.getValue())
                stopRuntimeRecursively(child);

        if (widget instanceof TabsWidget)
            for (TabItemProperty tab : ((TabsWidget)widget).displayTabs().getValue())
                for (final Widget child : tab.children().getValue())
                    stopRuntimeRecursively(child);

        // .. then stop this runtime
        final WidgetRuntime<?> runtime = RuntimeUtil.getRuntime(widget);
        if (runtime != null)
            runtime.stop();
    }
}
