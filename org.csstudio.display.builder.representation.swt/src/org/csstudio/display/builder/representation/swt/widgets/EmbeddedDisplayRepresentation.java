/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt.widgets;

import static org.csstudio.display.builder.representation.EmbeddedDisplayRepresentationUtil.checkCompletion;
import static org.csstudio.display.builder.representation.EmbeddedDisplayRepresentationUtil.loadDisplayModel;
import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.ModelThreadPool;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.representation.EmbeddedDisplayRepresentationUtil.DisplayAndGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/** Creates SWT item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EmbeddedDisplayRepresentation extends SWTBaseRepresentation<Composite, EmbeddedDisplayWidget>
{
    // No border, just a container for the embedded display

    /** Inner composite that holds child widgets */
    private Composite inner;

    /** The display file (and optional group inside that display) to load */
    private final AtomicReference<DisplayAndGroup> pending_display_and_group = new AtomicReference<>();

    /** Track active model in a thread-safe way
     *  to assert that each one is represented and removed
     */
    private final AtomicReference<DisplayModel> active_content_model = new AtomicReference<>();

    @Override
    protected Composite createSWTControl(final Composite parent) throws Exception
    {
        inner = new Composite(parent, SWT.NO_FOCUS);
        return inner;
    }

    @Override
    protected Composite getChildParent(final Composite parent)
    {
        return inner;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.propFile().addUntypedPropertyListener(this::fileChanged);
        model_widget.propGroupName().addUntypedPropertyListener(this::fileChanged);
        fileChanged(null, null, null);
    }

    private void fileChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        final DisplayAndGroup file_and_group =
            new DisplayAndGroup(model_widget.propFile().getValue(), model_widget.propGroupName().getValue());
        final DisplayAndGroup skipped = pending_display_and_group.getAndSet(file_and_group);
        if (skipped != null)
            logger.log(Level.FINE, "Skipped: {0}", skipped);
        // Load embedded display in background thread
        ModelThreadPool.getExecutor().execute(this::updatePendingDisplay);
    }

    /** Update to the next pending display
    *
    *  <p>Synchronized to serialize the background threads.
    *
    *  <p>Example: Displays A, B, C are requested in quick succession.
    *
    *  <p>pending_display_and_group=A is submitted to executor thread A.
    *
    *  <p>While handling A, pending_display_and_group=B is submitted to executor thread B.
    *  Thread B will be blocked in synchronized method.
    *
    *  <p>Then pending_display_and_group=C is submitted to executor thread C.
    *  As thread A finishes, thread B finds pending_display_and_group==C.
    *  As thread C finally continues, it finds pending_display_and_group empty.
    *  --> Showing A, then C, skipping B.
    */
   private synchronized void updatePendingDisplay()
   {
       final DisplayAndGroup handle = pending_display_and_group.getAndSet(null);
       if (handle == null)
           return;
        try
        {   // Load new model (potentially slow)
            final DisplayModel new_model = loadDisplayModel(model_widget, handle);

            // Atomically update the 'active' model
            final DisplayModel old_model = active_content_model.getAndSet(new_model);

            if (old_model != null)
            {   // Dispose old model
                final Future<Object> completion = toolkit.submit(() ->
                {
                    toolkit.disposeRepresentation(old_model);
                    return null;
                });
                checkCompletion(model_widget, completion, "timeout disposing old representation");
            }
            // Represent new model on UI thread
            final Future<Object> completion = toolkit.submit(() ->
            {
                representContent(new_model);
                return null;
            });
            checkCompletion(model_widget, completion, "timeout representing new content");
            model_widget.runtimePropEmbeddedModel().setValue(new_model);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Failed to handle embedded display " + handle, ex);
        }
    }

    /** @param content_model Model to represent */
    private void representContent(final DisplayModel content_model)
    {
        try
        {
            toolkit.representModel(inner, content_model);
        }
        catch (final Exception ex)
        {
            logger.log(Level.WARNING, "Failed to represent embedded display", ex);
        }
    }
}
