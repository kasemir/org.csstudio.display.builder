/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import static org.csstudio.display.builder.rcp.Plugin.logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

import org.csstudio.display.builder.rcp.Messages;
import org.csstudio.javafx.Screenshot;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import javafx.scene.Scene;

/** Action for printing snapshot of display
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PrintAction extends Action
{
    private final Shell shell;
    private final Scene scene;

    public PrintAction(final Shell shell, final Scene scene)
    {
        super(Messages.Print,
              PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_PRINT_EDIT));
        this.shell = shell;
        this.scene = scene;

        // Skip printer check on GTK because of hangups:
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=153936,
        // -Dorg.eclipse.swt.internal.gtk.disablePrinting if there are no printers,
        // https://github.com/ControlSystemStudio/cs-studio/issues/83
        if (! SWT.getPlatform().equals("gtk"))
        {
            // Only enable if printing is supported.
            final PrinterData[] printers = Printer.getPrinterList();
            if (printers != null)
            {
                logger.fine("Available printers:");
                for (PrinterData p : printers)
                    logger.fine("Printer: " + p.name + " (" + p.driver + ")");
                setEnabled(printers.length > 0);
            }
            else
            {
                logger.fine("No available printers");
                setEnabled(false);
            }
        }
    }

    @Override
    public void run()
    {
        // Not optimal:
        // Creates screenshot file for JFX scene,
        // then loads that file as SWT image..
        final Image snapshot;
        try
        {
            final Screenshot screenshot = new Screenshot(scene);
            final File image_file = screenshot.writeToTempfile("display");
            final ImageLoader loader = new ImageLoader();
            final ImageData[] data = loader.load(new FileInputStream(image_file));
            snapshot = new Image(shell.getDisplay(), data[0]);
        }
        catch (Exception ex)
        {
            ExceptionDetailsErrorDialog.openError(shell, "Cannot obtain screenshot", ex);
            return;
        }

        // SWT Printer GUI
        final PrintDialog dlg = new PrintDialog(shell);
        PrinterData data = dlg.open();
        if (data == null)
        {
            logger.fine("Cannot obtain printer");
            return;
        }
        // Access to SWT Printer must be on UI thread.
        // Printing in other thread can deadlock with UI thread.
        final Printer printer = new Printer(data);
        try
        {
            if (!printer.startJob("Display Builder"))
            {
                Logger.getLogger(getClass().getName()).fine("Cannot start print job");
                return;
            }
            try
            {   // Printer page info
                final Rectangle area = printer.getClientArea();
                final Rectangle trim = printer.computeTrim(0, 0, 0, 0);
                final Point dpi = printer.getDPI();

                // Compute layout
                final Rectangle image_rect = snapshot.getBounds();
                // Leave one inch on each border.
                // (copied the computeTrim stuff from an SWT example.
                //  Really no clue...)
                final int left_right = dpi.x + trim.x;
                final int top_bottom = dpi.y + trim.y;
                final int printed_width = area.width - 2*left_right;
                // Try to scale height according to on-screen aspect ratio.
                final int max_height = area.height - 2*top_bottom;
                final int printed_height = Math.min(max_height,
                   image_rect.height * printed_width / image_rect.width);

                // Print one page
                printer.startPage();
                final GC gc = new GC(printer);
                gc.drawImage(snapshot, 0, 0, image_rect.width, image_rect.height,
                             left_right, top_bottom, printed_width, printed_height);
                printer.endPage();
            }
            finally
            {
                printer.endJob();
            }
        }
        finally
        {
            printer.dispose();
        }
        // Image used by printer must only be disposed after the printer that used it.
        // Otherwise crash, https://github.com/ControlSystemStudio/cs-studio/issues/1937
        snapshot.dispose();
    }
}
