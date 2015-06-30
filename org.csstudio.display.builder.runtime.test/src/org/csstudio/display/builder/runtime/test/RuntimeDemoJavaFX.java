/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.csstudio.vtype.pv.PV;
import org.csstudio.vtype.pv.PVPool;
import org.csstudio.vtype.pv.RefCountMap;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.stage.Stage;

/** Runtime demo for JavaFX
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RuntimeDemoJavaFX extends Application
{
    private final Logger logger = Logger.getLogger(getClass().getName());
    private ToolkitRepresentation<Group, Node> toolkit;

    /** JavaFX main
     *  @throws Exception
     */
    public static void main(final String[] args) throws Exception
    {
        Settings.setup();
        launch(args);
    }

    /** JavaFX Start */
    @Override
    public void start(final Stage stage)
    {
        toolkit = new JFXRepresentation(stage);
        // Load model in background
        RuntimeUtil.getExecutor().execute(() -> loadModel(stage));
    }

    private void loadModel(final Stage stage)
    {
        try
        {
            final DisplayModel model = RuntimeUtil.loadModel("examples/dummy.opi", Settings.example_name);

            // Representation needs to be created in UI thread
            toolkit.execute(() -> representModel(stage, model));
        }
        catch (final Exception ex)
        {
            logger.log(Level.SEVERE, "Cannot start", ex);
        }
    }

    private void representModel(final Stage stage, final DisplayModel model)
    {
        // Create representation for model items
        try
        {
            final Group parent = toolkit.openNewWindow(model, this::handleClose);
            toolkit.representModel(parent, model);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        // Start runtimes in background
        RuntimeUtil.getExecutor().execute(() -> RuntimeUtil.startRuntime(model));
    }

    private boolean handleClose(final DisplayModel model)
    {
        RuntimeUtil.stopRuntime(model);
        toolkit.disposeRepresentation(model);

        int refs = 0;
        for (RefCountMap.ReferencedEntry<PV> ref : PVPool.getPVReferences())
        {
            refs += ref.getReferences();
            logger.log(Level.SEVERE, "PV {0} left with {1} references", new Object[] { ref.getEntry().getName(), ref.getReferences() });
        }
        if (refs == 0)
        {
            logger.log(Level.FINE, "All PV references were released, good job, get a cookie!");
            // JCA Context remains running, so need to exit() to really quit
            System.exit(0);
        }

        return true;
    }
}
