/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/** SWT Font Demo
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class FontDemoSWT
{
    public static void main(final String[] args) throws Exception
    {
        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setLayout(new RowLayout());

        Label label = new Label(shell, SWT.NONE);
        final Font font = new Font(display, "Liberation Mono", 40, SWT.NORMAL);
        label.setFont(font);
        label.setText("Example Test XOXO pq__ 1234567890 (SWT)");

        final GC gc = new GC(display);
        gc.setFont(font);
        System.out.println(font.getFontData()[0]);

        final FontMetrics metrics = gc.getFontMetrics();
        System.out.println("Height : " + metrics.getHeight());
        System.out.println("Ascent : " + metrics.getAscent());
        System.out.println("Descent: " + metrics.getDescent());
        System.out.println("leading: " + metrics.getLeading());

        shell.pack();
        shell.open();

        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}