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

import org.csstudio.display.builder.model.ModelPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/** RCP Application for display runtime
 *
 *  <p>"Standalone" runtime
 *
 *  @author Kay Kasemir
 */
public class RuntimeApplication implements IApplication
{
    private final static Logger logger = Logger.getLogger(RuntimeApplication.class.getName());

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

        String display_path = argv[0];

        // TODO Remove preference check
        final IPreferencesService prefs = Platform.getPreferencesService();
        final String macros = prefs.getString(ModelPlugin.ID, "macros", "", null);
        System.out.println(macros);

        // TODO Load named color, font configs

        // Creating an FXCanvas results in a combined
        // SWT and JavaFX setup with common UI thread.
        final Display display = Display.getDefault();
        final Shell shell = new Shell(display);
        shell.setText("Display Runtime");
        shell.setLayout(new FillLayout());

        final FXCanvas fxcanvas = new FXCanvas(shell, SWT.NONE);

        final Label label = new Label("JFX In SWT");
        final BorderPane root = new BorderPane(label);
        fxcanvas.setScene(new Scene(root));
        shell.setVisible(true);
        shell.setBounds(20, 10, 400, 600);
        shell.addDisposeListener(event -> display.dispose());

        final Stage stage = new Stage();
        stage.setTitle("Display Runtime");
        stage.setWidth(400);
        stage.setHeight(600);
        stage.setX(440);
        stage.setY(10);
        stage.setScene(new Scene(new BorderPane(new Label("JavaFX"))));
        stage.show();

        while (!display.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }

        System.out.println("Done.");

        return Integer.valueOf(0);
    }

    @Override
    public void stop()
    {
        System.exit(-1);
    }
}
