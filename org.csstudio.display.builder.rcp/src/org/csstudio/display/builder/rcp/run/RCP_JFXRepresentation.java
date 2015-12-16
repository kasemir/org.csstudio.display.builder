/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.util.function.Consumer;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.display.builder.runtime.RuntimeUtil;

import javafx.scene.Group;

/** Represent display builder in JFX inside RCP Views
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RCP_JFXRepresentation extends JFXRepresentation
{
    // Similar to JFXRepresentation, but using RuntimeViewPart as 'Window'

    public RCP_JFXRepresentation()
    {
        RuntimeUtil.hookRepresentationListener(this);
    }

    @Override
    public Group openNewWindow(final DisplayModel model,
                               final Consumer<DisplayModel> close_handler) throws Exception
    {
        final RuntimeViewPart part = RuntimeViewPart.open(close_handler);
        return part.getRoot();
    }

    @Override
    public void representModel(final Group parent, final DisplayModel model)
            throws Exception
    {
        // Top-level Group of the part's Scene has pointer to RuntimeViewPart.
        // For EmbeddedDisplayWidget, the parent is inside the EmbeddedDisplayWidget.
        final RuntimeViewPart part = (RuntimeViewPart) parent.getProperties().get(RuntimeViewPart.ROOT_RUNTIME_VIEW_PART);
        if (part != null)
            part.trackCurrentModel(model);

        super.representModel(parent, model);
    }
}
