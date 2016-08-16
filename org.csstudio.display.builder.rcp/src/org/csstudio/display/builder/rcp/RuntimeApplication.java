/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.representation.javafx.JFXStageRepresentation;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.csstudio.vtype.pv.PV;
import org.csstudio.vtype.pv.PVPool;
import org.csstudio.vtype.pv.RefCountMap;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Parent;
import javafx.stage.Stage;

/** RCP Application for display runtime
 *
 *  <p>"Standalone" runtime
 *
 *  May need command-line options
 *  -vmargs -Dosgi.requiredJavaVersion=1.8 -Xms256m -Xmx1024m
 *  -Dorg.osgi.framework.bundle.parent=ext -Dosgi.framework.extensions=org.eclipse.fx.osgi
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RuntimeApplication implements IApplication
{
    private final static Logger logger = Logger.getLogger(RuntimeApplication.class.getName());
    private Display display;
    private JFXStageRepresentation toolkit;

    public void usage()
    {
        System.out.println("USAGE: DisplayRuntime [options] /path/to/display.bob");
        System.out.println("Options:");
        System.out.println("    -help   Display command line options");
    }

    @Override
    public Object start(final IApplicationContext context) throws Exception
    {
        logger.log(Level.INFO, "Display Builder Runtime");

        final String[] argv = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        logger.log(Level.CONFIG, "Args: " + Arrays.toString(argv));

        if (argv.length != 1)
        {
            logger.log(Level.SEVERE, "Missing *.bob file name");
            usage();
            return Integer.valueOf(-1);
        }
        if (argv[0].startsWith("-h"))
        {
            usage();
            return Integer.valueOf(0);
        }

        final String display_path = argv[0];

        final Stage stage = initializeUI();

        // Load model in background
        RuntimeUtil.getExecutor().execute(() -> loadModel(stage, display_path));

        while (!display.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }

        int refs = 0;
        for (final RefCountMap.ReferencedEntry<PV> ref : PVPool.getPVReferences())
        {
            refs += ref.getReferences();
            logger.log(Level.SEVERE, "PV {0} left with {1} references", new Object[] { ref.getEntry().getName(), ref.getReferences() });
        }
        if (refs == 0)
            logger.log(Level.FINE, "All PV references were released, good job, get a cookie!");

        return Integer.valueOf(0);
    }

    private Stage initializeUI()
    {
        // TODO Load named color, font configs

        // Creating an FXCanvas results in a combined
        // SWT and JavaFX setup with common UI thread.
        // Shell that's created as a parent for the FXCanvas is never shown.
        display = Display.getDefault();
        final Shell temp_shell = new Shell(display);
        new FXCanvas(temp_shell, SWT.NONE);
        temp_shell.close();

        final Stage stage = new Stage();
        stage.setTitle("Display Runtime");
        stage.setWidth(600);
        stage.setHeight(400);
        stage.show();

        toolkit = new JFXStageRepresentation();
        RuntimeUtil.hookRepresentationListener(toolkit);
        return stage;
    }

    private void loadModel(final Stage stage, final String display_path)
    {
        try
        {
            final DisplayModel model = RuntimeUtil.loadModel(null, display_path);

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
            final Parent parent = toolkit.configureStage(stage, model, this::handleClose);
            toolkit.representModel(parent, model);
        }
        catch (final Exception ex)
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
        display.dispose();
        return true;
    }

    @Override
    public void stop()
    {
        System.exit(-1);
    }
}
