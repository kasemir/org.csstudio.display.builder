/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.test;

import org.csstudio.display.builder.representation.javafx.sandbox.DragDropDemo;
import org.csstudio.javafx.swt.JFX_SWT_Wrapper;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/** Demo of {@link DragDropDemo} hosted in SWT
 *
 *  <p>To start in IDE, see notes in JFX_SWT_Wrapper
 *  on adding JRE/lib/jfxswt.jar to the classpath.
 *
 *  <p>When first started, this will only receive [text/plain]
 *  from a separately running {@link DragDropDemo}.
 *
 *  Once the custom data type is dragged from within this
 *  demo, the FXCanvas knows about the type and will now
 *  see it when dragged in from the plain {@link DragDropDemo}.
 *  .. but it will not always de-serialize the data,
 *  and it might now _only_ see the custom type, no longer
 *  recognizing the [text/plain].
 *
 *  @author Kay Kasemir
 */
public class DragDropDemoJFXinSWT
{
    @SuppressWarnings("nls")
    public static void main(String[] args) throws Exception
    {
        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("Drag Drop Demo");
        shell.setLayout(new FillLayout());

        final JFX_SWT_Wrapper wrapper = new JFX_SWT_Wrapper(shell, () -> DragDropDemo.createScene());
        shell.open();

        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}