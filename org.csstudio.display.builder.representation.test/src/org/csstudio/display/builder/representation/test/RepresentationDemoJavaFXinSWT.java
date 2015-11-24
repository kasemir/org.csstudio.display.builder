/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.test;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetName;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Group;
import javafx.scene.Scene;

/** JavaFX inside SWT Demo
 *  @author Kay Kasemir
 */
public class RepresentationDemoJavaFXinSWT
{
    public static void main(final String[] args) throws Exception
    {
        //final DisplayModel model = ExampleModels.getModel(1);
        final DisplayModel model = ExampleModels.createModel();

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText(model.getPropertyValue(widgetName));
        shell.setLayout(new RowLayout());

        // Requires defining classpath variable JFXSWT as
        // ${JVM}/jre/lib/jfxswt.jar
        // TODO On older Ubuntu Linux, crashes in here. GTK2/3 conflict?
        // OK on RedHat 6
        final FXCanvas fx_canvas = new FXCanvas(shell, SWT.NONE)
        {
            @Override
            public Point computeSize(int wHint, int hHint, boolean changed)
            {
                getScene().getWindow().sizeToScene();
                final int width = (int) getScene().getWidth();
                final int height = (int) getScene().getHeight();
                return new Point(width, height);
            }
        };

        final JFXRepresentation toolkit = new JFXRepresentation();
        final Scene scene = toolkit.createScene();
        final Group parent = toolkit.getSceneRoot(scene);
        toolkit.representModel(parent, model);

        final DummyRuntime runtime = new DummyRuntime(model);

        fx_canvas.setScene(scene);
        shell.open();

        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        runtime.shutdown();
        toolkit.shutdown();
        display.dispose();
    }
}