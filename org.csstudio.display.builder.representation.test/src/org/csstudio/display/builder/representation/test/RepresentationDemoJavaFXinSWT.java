/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.test;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propName;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.javafx.swt.JFX_SWT_Wrapper;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import javafx.scene.Parent;
import javafx.scene.Scene;

/** JavaFX inside SWT Demo
 *  @author Kay Kasemir
 */
public class RepresentationDemoJavaFXinSWT
{
    private static JFXRepresentation toolkit;

    public static void main(final String[] args) throws Exception
    {
        //final DisplayModel model = ExampleModels.getModel(1);
        final DisplayModel model = ExampleModels.createModel();

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText(model.getPropertyValue(propName));
        shell.setLayout(new FillLayout());

        final JFX_SWT_Wrapper wrapper = new JFX_SWT_Wrapper(shell, () ->
        {
            toolkit = new JFXRepresentation(false);
            return new Scene(toolkit.createModelRoot());
        });

        final Scene scene = wrapper.getScene();
        JFXRepresentation.setSceneStyle(scene);
        final Parent parent = toolkit.getModelParent();
        toolkit.representModel(parent, model);

        final DummyRuntime runtime = new DummyRuntime(model);

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