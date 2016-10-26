/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot.internal;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.javafx.Screenshot;
import org.csstudio.javafx.rtplot.Activator;
import org.csstudio.javafx.rtplot.Messages;
import org.csstudio.javafx.rtplot.RTPlot;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/** Action that saves a snapshot of the current plot
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class SnapshotAction<XTYPE extends Comparable<XTYPE>> extends Action
{

    final private RTPlot<XTYPE> plot;
    private Shell shell;
    //
    public SnapshotAction(final RTPlot<XTYPE> plot)
    {
        super(Messages.Snapshot, Activator.getIconID("camera"));
        this.plot = plot;
        this.shell = null;
    }

    public void setShell(Shell shell)
    {
        this.shell = shell;
    }

    //
    @Override
    public void run()
    {
        if (shell == null)
        {
            Logger.getLogger("SnapshotAction").log(Level.SEVERE, "Cannot make snapshot. Shell not set"); //$NON-NLS-1$
            return;
        }

        final FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setOverwrite(true);
        dialog.setFilterNames(new String[] { "PNG Files", "All Files (*.*)" });
        dialog.setFilterExtensions(new String[] { "*.png", "*.*" });
        final String path = dialog.open();
        if (path == null)
            return;

        final Screenshot screenshot;
        try
        {
            screenshot = new Screenshot(plot.getScene());
        }
        catch (Exception ex)
        {
            ExceptionDetailsErrorDialog.openError(shell, "Cannot obtain screenshot", ex);
            return;
        }

        new Job("Save Image")
        {
            @Override
            protected IStatus run(final IProgressMonitor monitor)
            {
                try
                {
                    screenshot.writeToFile(new File(path));
                }
                catch (Exception ex)
                {
                    shell.getDisplay().asyncExec(() ->
                    ExceptionDetailsErrorDialog.openError(shell, "Cannot write screenshot", ex));
                }
                return Status.OK_STATUS;
            }

        }.schedule();;
    }

    //{
    //        final Shell shell = plot.getShell();
    //        final AtomicReference<String> path = new AtomicReference<String>();
    //
    //        // Use background thread because of potentially lengthy file I/O
    //        final Job job = new Job(Messages.Snapshot)
    //        {
    //            @Override
    //            protected IStatus run(final IProgressMonitor progress)
    //            {
    //                progress.beginTask(Messages.Snapshot, IProgressMonitor.UNKNOWN);
    //                while (true) // Repeat until success or cancel
    //                {
    //                    shell.getDisplay().syncExec(() ->
    //                    {    // Prompt for file name, in display thread
    //                        final FileDialog dialog = new FileDialog(shell, SWT.SAVE);
    //                        if (path.get() != null)
    //                        {    // Re-open with previously entered directory and filename
    //                            final File file = new File(path.get());
    //                            dialog.setFilterPath(file.getParent());
    //                            dialog.setFileName(file.getName());
    //                        }
    //                        dialog.setOverwrite(true);
    //                        dialog.setFilterNames(new String[] { "PNG Files", "All Files (*.*)" });
    //                        dialog.setFilterExtensions(new String[] { "*.png", "*.*" }); // For Windows
    //                        path.set(dialog.open());
    //                    });
    //                    if (path.get() == null) // cancelled?
    //                        break;
    //
    //                    try
    //                    {    // Create snapshot, save to file
    //                        final ImageLoader loader = new ImageLoader();
    //                        final Image image = plot.getImage();
    //                        loader.data = new ImageData[] { image.getImageData() };
    //                        image.dispose();
    //                        loader.save(path.get(), SWT.IMAGE_PNG);
    //                        // Done!
    //                        break;
    //                    }
    //                    catch (Exception ex)
    //                    {    // Error dialog needs to be in display thread
    //                        shell.getDisplay().syncExec(() ->
    //                        {
    //                            MessageDialog.openError(shell, Messages.Snapshot,
    //                                    NLS.bind("Cannot save snapshot as {0}.\n\nDetail:\n{1}",
    //                                             path.get(), ex.getMessage()) );
    //                        });
    //                    }
    //                    // Prompt for different file name
    //                }
    //                progress.done();
    //                return Status.OK_STATUS;
    //            }
    //        };
    //        job.schedule();
    //}
}
