/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.io.File;

import org.csstudio.display.builder.rcp.Messages;
import org.csstudio.javafx.Screenshot;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import javafx.scene.Scene;

/** Action for saving snapshot of display
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class SaveSnapshotAction extends Action
{
    private final Shell shell;
    private final Scene scene;

    public SaveSnapshotAction(final Shell shell, final Scene scene)
    {
        super(Messages.SaveSnapshot,
              PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
        this.shell = shell;
        this.scene = scene;
    }

    @Override
    public void run()
    {
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
            screenshot = new Screenshot(scene);
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
