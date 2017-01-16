/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.editor;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.javafx.Screenshot;
import org.csstudio.javafx.rtplot.Messages;
import org.csstudio.javafx.rtplot.RTPlot;
import org.csstudio.trends.databrowser3.Activator;
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
public class SnapshotAction extends Action
{
    final private RTPlot<?> plot;
    private Shell shell;
    //
    public SnapshotAction(final RTPlot<?> plot, Shell shell)
    {
        super(Messages.Snapshot, Activator.getRTPlotIconID("camera"));
        this.plot = plot;
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
            screenshot = new Screenshot(plot.getPlotNode());
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
}
