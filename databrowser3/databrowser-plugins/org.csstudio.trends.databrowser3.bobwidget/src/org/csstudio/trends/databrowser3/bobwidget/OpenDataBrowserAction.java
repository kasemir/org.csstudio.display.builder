/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.bobwidget;

import static org.csstudio.trends.databrowser3.Activator.logger;

import java.util.logging.Level;

import org.csstudio.display.builder.runtime.RuntimeAction;
import org.csstudio.trends.databrowser3.editor.DataBrowserEditor;
import org.csstudio.trends.databrowser3.editor.DataBrowserModelEditorInput;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.csstudio.utility.singlesource.PathEditorInput;
import org.csstudio.utility.singlesource.SingleSourcePlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/** Action for context menu object contribution that opens
 *  the full Data Browser for the model in the Data Browser widget
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class OpenDataBrowserAction extends RuntimeAction
{
    private final DataBrowserWidget widget;

    public OpenDataBrowserAction(final DataBrowserWidget widget)
    {
        super(Messages.OpenDataBrowser, "platform:/plugin/org.csstudio.trends.databrowser3/icons/databrowser.png");
        this.widget = widget;
    }

    @Override
    public void run()
    {
        IPath filename;
        try
        {
            filename = SingleSourcePlugin.getResourceHelper().newPath(widget.getExpandedFilename());
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot determine path", ex);
            return;
        }
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try
        {
            final IEditorInput input = new  PathEditorInput(filename);
            final DataBrowserModelEditorInput model_input =
                    new DataBrowserModelEditorInput(input, widget.cloneModel());
            page.openEditor(model_input, DataBrowserEditor.ID, true);
        }
        catch (Exception ex)
        {
            ExceptionDetailsErrorDialog.openError(page.getActivePart().getSite().getShell(),
                    Messages.Error,
                    NLS.bind(Messages.OpenDataBrowserErrorFmt, filename.toString()),
                    ex);
        }
    }
}
