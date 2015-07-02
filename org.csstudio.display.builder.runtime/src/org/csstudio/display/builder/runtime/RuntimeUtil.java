/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.ContainerWidget;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.runtime.script.ScriptSupport;

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
    private static final Logger logger = Logger.getLogger(RuntimeUtil.class.getName());

    /** @return Executor that should be used for runtime-related background tasks
     */
    public static Executor getExecutor()
    {
        return ForkJoinPool.commonPool();
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
        final String resolved_name = ResourceUtil.resolveDisplay(parent_display, display_file);
        final ModelReader reader = new ModelReader(ResourceUtil.openInputStream(resolved_name));
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
        DisplayModel model = getDisplayModel(widget);
        while (true)
        {
            final EmbeddedDisplayWidget embedder = model.getUserData(DisplayModel.USER_DATA_EMBEDDING_WIDGET);
            if (embedder == null)
                return model;
            model = getTopDisplayModel(embedder);
        }
    }

    /** Locate display model, i.e. root of widget tree
     *  @param widget Widget within model
     *  @return {@link DisplayModel} for widget
     *  @throws Exception if widget is not part of a model
     */
    public static DisplayModel getDisplayModel(final Widget widget) throws Exception
    {
        Widget candidate = widget;
        while (candidate.getParent().isPresent())
            candidate = candidate.getParent().get();
        if (candidate instanceof DisplayModel)
            return (DisplayModel) candidate;
        throw new Exception("Missing DisplayModel for " + widget);
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

    /** Obtain the toolkit used to represent widgets
     *
     *  @param model {@link DisplayModel}
     *  @return
     *  @throws NullPointerException if toolkit not set
     */
    public static <TWP, TW> ToolkitRepresentation<TWP, TW> getToolkit(final DisplayModel model) throws NullPointerException
    {
        final ToolkitRepresentation<TWP, TW> toolkit = model.getUserData(DisplayModel.USER_DATA_TOOLKIT);
        return Objects.requireNonNull(toolkit, "Toolkit not set");
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

        // Recurse into child widgets
        if (widget instanceof ContainerWidget)
            for (final Widget child : ((ContainerWidget) widget).getChildren())
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
        final WidgetRuntime<?> runtime = RuntimeUtil.getRuntime(widget);
        if (runtime != null)
            runtime.stop();

        // Recurse into child widgets
        if (widget instanceof ContainerWidget)
            for (final Widget child : ((ContainerWidget) widget).getChildren())
                stopRuntimeRecursively(child);
    }
}
