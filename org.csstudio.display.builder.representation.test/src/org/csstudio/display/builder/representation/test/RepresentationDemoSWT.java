/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.test;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propHeight;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propName;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.swt.SWTRepresentation;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/** SWT Demo
 *  @author Kay Kasemir
 */
public class RepresentationDemoSWT
{
    public static void main(final String[] args) throws Exception
    {
        //final DisplayModel model = ExampleModels.getModel(1);
        final DisplayModel model = ExampleModels.createModel();

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText(model.getPropertyValue(propName));

        shell.setSize(model.getPropertyValue(propWidth),
                      model.getPropertyValue(propHeight));

        final ToolkitRepresentation<Composite, Control> toolkit =
                new SWTRepresentation(display);
        toolkit.representModel(shell, model);

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