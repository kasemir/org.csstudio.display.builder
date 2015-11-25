/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.util.function.Predicate;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.display.builder.runtime.RuntimeUtil;

import javafx.scene.Group;
import javafx.stage.Stage;

/** Represent display builder in JFX inside RCP Views
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RCP_JFXRepresentation extends JFXRepresentation
{
    // Similar to JFXRepresentation, but using RuntimeViewPart as 'Window'
    private final static RCP_JFXRepresentation instance = new RCP_JFXRepresentation();

    /** @return Singleton instance */
    public static RCP_JFXRepresentation getInstance()
    {
        return instance;
    }

    /** Prevent multiple instances */
    private RCP_JFXRepresentation()
    {
        // TODO Replace with hook that allows view part to track prev/next
        RuntimeUtil.hookListener(this);
    }

    @Override
    public Group configureStage(final Stage stage, final DisplayModel model,
            final Predicate<DisplayModel> close_request_handler)
    {
        throw new IllegalStateException("RCP-based representation should not use standalone Stage");
    }

    @Override
    public Group openNewWindow(final DisplayModel model,
            final Predicate<DisplayModel> close_request_handler) throws Exception
    {
        // TODO Review: Check ActionUtil.openDisplay() which calls this to open a new window
        final RuntimeViewPart part = RuntimeViewPart.open();
        model.setUserData(RuntimeViewPart.USER_DATA_VIEW_PART, part);
        return part.getRoot();
    }

    @Override
    public void representModel(final Group parent, final DisplayModel model)
            throws Exception
    {
        final RuntimeViewPart part = model.getUserData(RuntimeViewPart.USER_DATA_VIEW_PART);
        if (part == null)
            System.err.println("\n\n*** Missing RuntimeViewPart ***");
        else
            part.setPartName(model.getName());

        super.representModel(parent, model);
    }
}
