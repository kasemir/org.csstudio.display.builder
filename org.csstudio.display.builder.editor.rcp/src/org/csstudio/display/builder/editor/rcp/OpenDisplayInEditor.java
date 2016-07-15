/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import static org.csstudio.display.builder.editor.rcp.Plugin.logger;

import java.util.logging.Level;

import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.run.RuntimeViewPart;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

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
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IWorkbenchPart part = page.getActivePart();
        if (part instanceof RuntimeViewPart)
        {
            final RuntimeViewPart view = (RuntimeViewPart) part;
            final DisplayInfo info = view.getDisplayInfo();

            try
            {
                final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(info.getPath()));
                if (! file.exists())
                {
                    // TODO Check for URL, download into file?
                    throw new Exception("Cannot locate " + info.getPath() + " in workspace");
                }

                final IEditorInput input = new FileEditorInput(file);
                page.openEditor(input, DisplayEditorPart.ID);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot open in editor: " + info, ex);
            }
        }
        else
            logger.log(Level.WARNING, "Cannot locate active display, got " + part);
        return null;
    }
}
