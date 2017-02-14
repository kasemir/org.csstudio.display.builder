/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.csstudio.display.builder.runtime.WidgetRuntime;

/** Runtime for the {@link EmbeddedDisplayWidget}
 *
 *  <p>The EmbeddedDisplayRepresentation loads the
 *  embedded display model to allow showing it in the editor.
 *  The runtime tarts/stops the model of the embedded widget.
 *
 *  @author Kay Kasemir
 */
public class EmbeddedDisplayRuntime extends WidgetRuntime<EmbeddedDisplayWidget>
{
    /** Start: Connect to PVs, ..., then monitor the embedded model to start/stop it
     *  @throws Exception on error
     */
    @Override
    public void start() throws Exception
    {
        super.start();
        widget.runtimePropEmbeddedModel().addPropertyListener(this::embeddedModelChanged);
        embeddedModelChanged(null, null, widget.runtimePropEmbeddedModel().getValue());
    }

    private void embeddedModelChanged(final WidgetProperty<DisplayModel> property, final DisplayModel old_model, final DisplayModel new_model)
    {
        // Dispose old model
        if (old_model != null)
            RuntimeUtil.stopRuntime(old_model);
        if (new_model != null)
            // Back off UI thread, start runtimes of child widgets
            RuntimeUtil.startRuntime(new_model);
    }

    @Override
    public void stop()
    {
        final DisplayModel old_model = widget.runtimePropEmbeddedModel().getValue();
        if (old_model != null)
            RuntimeUtil.stopRuntime(old_model);
        super.stop();
    }
}
