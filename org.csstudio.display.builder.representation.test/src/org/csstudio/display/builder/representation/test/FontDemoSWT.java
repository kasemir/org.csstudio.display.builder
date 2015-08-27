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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/** SWT Font Demo
 *
 *  Call with font size. Defaults to '40 points'.
 *  Argument "auto" will attempt to match the JFX example.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class FontDemoSWT
{
    private static final String TEXT = "'Example' Test \"XOXO\" pq__ 1234567890 (SWT)";

    public static void main(final String[] args) throws Exception
    {
        final Display display = new Display();

        // Determine size of text
        Font font = new Font(display, "Liberation Mono", 100, SWT.NORMAL);
        GC gc = new GC(display);
        gc.setFont(font);
        final Point measure = gc.stringExtent(TEXT);
        gc.dispose();

        System.out.println("'100 point' text uses " + measure.x + " x " + measure.y + " pixels");

        final int font_size;
        if (args.length == 1)
        {
            if ("auto".equalsIgnoreCase(args[0]))
            {   // JFX example is 1032.16 pixels wide
                font_size = 1032 * 100 / measure.x;
                System.out.println("Using font size " + font_size + " pt to get same width as JFX example");
                System.out.println("Font scaling factor: " + font_size/40.0);
            }
            else
            {
                font_size = Integer.parseInt(args[0]);
                System.out.println("Using font size " + font_size + " pt");
            }
        }
        else
        {
            font_size = 40;
            System.out.println("Using font size " + font_size + " pt");
        }

        final Shell shell = new Shell(display);
        shell.setLayout(new RowLayout());

        Label label = new Label(shell, SWT.NONE);
        font = new Font(display, "Liberation Mono", font_size, SWT.NORMAL);
        label.setFont(font);
        label.setText(TEXT);

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