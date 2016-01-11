/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
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
import org.csstudio.display.builder.representation.swt.SWTRepresentation;
import org.csstudio.display.builder.runtime.ActionUtil;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.csstudio.vtype.pv.PV;
import org.csstudio.vtype.pv.PVPool;
import org.csstudio.vtype.pv.RefCountMap;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/** Runtime demo for SWT
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RuntimeDemoSWT implements Runnable
{
    private final Logger logger = Logger.getLogger(getClass().getName());
    private ToolkitRepresentation<Composite, Control> toolkit;

    /** SWT main
     *  @throws Exception
     */
    public static void main(final String[] args) throws Exception
    {
        Settings.setup();
        if (args.length == 1)
            Settings.display_path = args[0];
        new RuntimeDemoSWT().run();
    }

    @Override
    public void run()
    {
        final Display display = new Display();
        toolkit = new SWTRepresentation(display);
        RuntimeUtil.hookRepresentationListener(toolkit);

        // Load model in background
        RuntimeUtil.getExecutor().execute(() -> loadModel(display));

        while (!display.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }

        toolkit.shutdown();
        display.dispose();

        int refs = 0;
        for (final RefCountMap.ReferencedEntry<PV> ref : PVPool.getPVReferences())
        {
            refs += ref.getReferences();
            logger.log(Level.SEVERE, "PV {0} left with {1} references", new Object[] { ref.getEntry().getName(), ref.getReferences() });
        }
        if (refs == 0)
            logger.log(Level.FINE, "All PV references were released, good job, get a cookie!");

        // JCA Context remains running, so need to exit() to really quit
        System.exit(0);
    }

    private void loadModel(final Display display)
    {
        try
        {
            final DisplayModel model = RuntimeUtil.loadModel("examples/dummy.opi", Settings.display_path);

            // Representation needs to be created in UI thread
            toolkit.execute(() -> representModel(display, model));
        }
        catch (final Exception ex)
        {
            logger.log(Level.SEVERE, "Cannot start", ex);
        }
    }

    private void representModel(final Display display, final DisplayModel model)
    {
        // Add model items
        final Composite parent;
        try
        {
            parent = toolkit.openNewWindow(model, this::handleClose);
            toolkit.representModel(parent, model);
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
            return;
        }

        // Start runtimes in background
        RuntimeUtil.getExecutor().execute(() -> RuntimeUtil.startRuntime(model));
    }

    private void handleClose(final DisplayModel model)
    {
        ActionUtil.handleClose(model);

        int refs = 0;
        for (final RefCountMap.ReferencedEntry<PV> ref : PVPool.getPVReferences())
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
    }
}
