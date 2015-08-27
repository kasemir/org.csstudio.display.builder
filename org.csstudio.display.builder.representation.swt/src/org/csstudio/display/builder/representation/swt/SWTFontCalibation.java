/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.representation.FontCalibration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/** SWT Font Calibration
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class SWTFontCalibation implements FontCalibration
{
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public double getCalibrationFactor() throws Exception
    {
        final Display display = Display.getCurrent();

        final Font font = new Font(display, FontCalibration.FONT, FontCalibration.SIZE, SWT.NORMAL);
        final String name = font.getFontData()[0].getName();
        if (! name.startsWith(FontCalibration.FONT))
            throw new Exception("Requested " + FontCalibration.FONT + " but got " + name);

        final GC gc = new GC(display);
        gc.setFont(font);
        final Point measure = gc.stringExtent(TEXT);
        gc.dispose();

        logger.log(Level.CONFIG,
                "Font calibration measure: " + measure.x + " x " + measure.y);

        return FontCalibration.PIXEL_WIDTH / measure.x;
    }

    public static void main(final String[] args) throws Exception
    {
        final Display display = new Display();

        final double factor = new SWTFontCalibation().getCalibrationFactor();

        final int font_size = (int) (FontCalibration.SIZE * factor + 0.5);

        final Shell shell = new Shell(display);
        shell.setLayout(new RowLayout());
        shell.setText("SWT: Calibration factor " + factor);

        Label label = new Label(shell, SWT.NONE);
        final Font font = new Font(display, FontCalibration.FONT, font_size, SWT.NORMAL);
        label.setFont(font);
        label.setText(FontCalibration.TEXT);

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