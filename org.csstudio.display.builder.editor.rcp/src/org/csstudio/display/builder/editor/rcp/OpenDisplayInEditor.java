/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import static org.csstudio.display.builder.editor.rcp.Plugin.logger;

import java.io.InputStream;
import java.util.logging.Level;

import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.RuntimeViewPart;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/** Open display of currently active view in editor
 *
 *  <p>plugin.xml adds this to a context menu defined
 *  by ContextMenuSupport in org.csstudio.display.builder.rcp plugin.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class OpenDisplayInEditor extends AbstractHandler implements IHandler
{
    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException
    {
        final RuntimeViewPart view = RuntimeViewPart.getActiveDisplay();
        if (view != null)
        {
            final DisplayInfo info = view.getDisplayInfo();
            try
            {
                openDisplay(info);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot open in editor: " + info, ex);
            }
        }
        return null;
    }

    private void openDisplay(final DisplayInfo info) throws Exception
    {
        // Locate workspace file
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(info.getPath()));
        if (! file.exists())
        {
            // If there is no file, try to open the stream for the web URL or external file
            final InputStream stream = ModelResourceUtil.openResourceStream(info.getPath());

            // If that succeeds, prompt for local file name
            final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            if (! MessageDialog.openQuestion(shell, Messages.DownloadTitle,
                    NLS.bind(Messages.DownloadPromptFMT, info.getPath())))
            {
                stream.close();
                return;
            }
            file = DisplayEditorPart.promptForFile(shell, null);
            if (file == null)
                return;

            // Write local copy, then proceed as if we had a workspace file to begin with
            if (file.exists())
                file.setContents(stream, IResource.FORCE, new NullProgressMonitor());
            else
                file.create(stream, IResource.FORCE, new NullProgressMonitor());
        }

        DisplayEditorPart.openDisplayFile(file);
    }
}
