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

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
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
            try
            {
                final Widget widget = view.getActiveWidget();
                final DisplayModel model = widget.getDisplayModel();
                final String path;
                // Options:
                if (widget instanceof EmbeddedDisplayWidget)
                {   // c) Widget is an embedded widget.
                    //    -> User probably wanted to edit the _body_ of that embedded widget
                    final EmbeddedDisplayWidget embedded = (EmbeddedDisplayWidget) widget;
                    path = ModelResourceUtil.resolveResource(model, embedded.propFile().getValue());
                }
                else
                {   // b) Widget is one of the widgets in the body of an embedded widget:
                    //    -> Get the body display, _not_ the top-level display
                    // a) Widget is in the top-level display, or the display itself:
                    //    -> Use that that
                    path = model.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
                }
                try
                {
                    open(path);
                }
                catch (Exception ex)
                {
                    logger.log(Level.WARNING, "Cannot open in editor: " + path, ex);
                }
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot open display in editor", ex);
            }
        }
        return null;
    }

    /** Open editor for a display
     *
     *  <p>For remote files (http://..), it prompts for a download
     *  and then opens the local file.
     *
     *  @param display_path Path to the display file
     *  @throws Exception on error
     */
    public static void open(final String display_path) throws Exception
    {
        // Locate workspace file
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(display_path));
        if (! file.exists())
        {
            // If there is no file, try to open the stream for the web URL or external file
            final InputStream stream = ModelResourceUtil.openResourceStream(display_path);

            // If that succeeds, prompt for local file name
            final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            if (! MessageDialog.openQuestion(shell, Messages.DownloadTitle,
                    NLS.bind(Messages.DownloadPromptFMT, display_path)))
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
