/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import static org.csstudio.logbook.AttachmentBuilder.attachment;

import java.io.FileInputStream;

import org.csstudio.display.builder.rcp.Messages;
import org.csstudio.javafx.Screenshot;
import org.csstudio.logbook.LogEntryBuilder;
import org.csstudio.logbook.ui.LogEntryBuilderDialog;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import javafx.scene.Scene;

/** Action for logging snapshot of display
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class SendLogbookAction extends Action
{
    private final Shell shell;
    private final Scene scene;

    public SendLogbookAction(final Shell shell, final Scene scene)
    {
        super(Messages.SendToLogbook,
              AbstractUIPlugin.imageDescriptorFromPlugin("org.csstudio.logbook.ui", "icons/exportLog.png"));
        this.shell = shell;
        this.scene = scene;
    }

    @Override
    public void run()
    {
        String image_file = null;
        try
        {
            final Screenshot screenshot = new Screenshot(scene, "display");
            image_file = screenshot.getFilename();
        }
        catch (Exception ex)
        {
            ExceptionDetailsErrorDialog.openError(shell, "Cannot obtain screenshot", ex);
            image_file = null;
        }

        try
        {
            final LogEntryBuilder entry = LogEntryBuilder.withText(Messages.DefaultLogbookText);
            if (image_file != null)
                entry.attach(attachment(image_file).inputStream(new FileInputStream(image_file)));
            final LogEntryBuilderDialog dialog = new LogEntryBuilderDialog(shell, entry);
            dialog.setBlockOnOpen(true);
            dialog.open();
        }
        catch (Exception ex)
        {
            ExceptionDetailsErrorDialog.openError(shell, "Cannot create log entry", ex);
        }
    }
}
