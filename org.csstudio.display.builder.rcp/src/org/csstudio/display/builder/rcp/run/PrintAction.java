/*******************************************************************************
 * Copyright (c) 2015-2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.util.Objects;

import org.csstudio.display.builder.rcp.Messages;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Scale;

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
    }

    @Override
    public void run()
    {
        try
        {
            // Select printer
            final PrinterJob job = Objects.requireNonNull(PrinterJob.createPrinterJob(), "Cannot create printer job");

            if (! job.showPrintDialog(scene.getWindow()))
                return;

            // Get Screenshot
            final WritableImage screenshot = scene.getRoot().snapshot(null, null);

            // Scale image to full page
            final Printer printer = job.getPrinter();
            final Paper paper = job.getJobSettings().getPageLayout().getPaper();
            final PageLayout pageLayout = printer.createPageLayout(paper,
                                                                   PageOrientation.LANDSCAPE,
                                                                   Printer.MarginType.DEFAULT);
            final double scaleX = pageLayout.getPrintableWidth() / screenshot.getWidth();
            final double scaleY = pageLayout.getPrintableHeight() / screenshot.getHeight();
            final double scale = Math.min(scaleX, scaleY);
            final ImageView print_node = new ImageView(screenshot);
            print_node.getTransforms().add(new Scale(scale, scale));

            // Print off the UI thread
            Job.create(Messages.Print, monitor ->
            {
                if (job.printPage(print_node))
                    job.endJob();
            }).schedule();

        }
        catch (Exception ex)
        {
            ExceptionDetailsErrorDialog.openError(shell, "Cannot obtain screenshot", ex);
            return;
        }
    }
}
