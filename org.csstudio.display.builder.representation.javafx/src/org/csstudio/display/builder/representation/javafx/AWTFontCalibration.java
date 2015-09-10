/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.representation.FontCalibration;

/** Java AWT calibration
 *
 *  <p>Can also be executed as demo
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class AWTFontCalibration implements Runnable, FontCalibration
{
    private final Logger logger = Logger.getLogger(getClass().getName());
    final Font font = new Font(FontCalibration.FONT, Font.PLAIN, FontCalibration.SIZE);

    @Override
    public double getCalibrationFactor()
    {
        final BufferedImage buf = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        final Graphics2D gc = buf.createGraphics();
        gc.setFont(font);
        final Rectangle2D measure = gc.getFontMetrics().getStringBounds(FontCalibration.TEXT, gc);
        gc.dispose();

        logger.log(Level.FINE,
                   "Font calibration measure: " + measure.getWidth() + " x " + measure.getHeight());
        final double factor = FontCalibration.PIXEL_WIDTH / measure.getWidth();
        logger.log(Level.CONFIG, "JFX font calibration factor: {0}", factor);
        return factor;
    }

    @Override
    public void run()
    {
        Logger.getLogger("").setLevel(Level.CONFIG);
        for (Handler handler : Logger.getLogger("").getHandlers())
            handler.setLevel(Level.CONFIG);

        final double factor = getCalibrationFactor();

        final Frame frame = new Frame("Java AWT: Calibration factor " + factor);
        frame.setLayout(new BorderLayout());
        final TextField text = new TextField(FontCalibration.TEXT);
        text.setFont(font);
        text.setEditable(false);
        frame.add(text, BorderLayout.CENTER);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent windowEvent)
            {
                System.exit(0);
            }
        });

        frame.pack();
        frame.setVisible(true);

        if (Math.abs(factor - 1.0) > 0.01)
            System.err.println("Calibration is not 1.0 but " + factor);
    }

    public static void main(final String[] args)
    {
        new AWTFontCalibration().run();
    }
}