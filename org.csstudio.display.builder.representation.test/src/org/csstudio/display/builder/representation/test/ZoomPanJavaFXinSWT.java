/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.test;

import org.csstudio.display.builder.representation.javafx.sandbox.ZoomPan;
import org.csstudio.javafx.swt.JFX_SWT_Wrapper;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/** Demo of {@link ZoomPan} hosted in SWT
 *
 *  <p>To start in IDE, see notes in JFX_SWT_Wrapper
 *  on adding JRE/lib/jfxswt.jar to the classpath.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings({ "nls", "unused" })
public class ZoomPanJavaFXinSWT
{
    public static void main(String[] args) throws Exception
    {
        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("Zoom Pan Demo");
        shell.setLayout(new FillLayout());

        final JFX_SWT_Wrapper wrapper = new JFX_SWT_Wrapper(shell, () -> ZoomPan.createScene());
        shell.open();

        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}