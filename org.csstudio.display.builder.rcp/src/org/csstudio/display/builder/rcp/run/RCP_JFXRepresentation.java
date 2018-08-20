/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.commons.lang3.SystemUtils;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.Messages;
import org.csstudio.display.builder.rcp.RuntimePerspective;
import org.csstudio.display.builder.rcp.RuntimeViewPart;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.ui.util.dialogs.ResourceSelectionDialog;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.ide.IDE;

import javafx.scene.Node;
import javafx.scene.Parent;

/** Represent display builder in JFX inside RCP Views
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RCP_JFXRepresentation extends JFXRepresentation
{
    private final RuntimeViewPart part;

    // Similar to JFXRepresentation, but using RuntimeViewPart as 'Window'

    public RCP_JFXRepresentation(final RuntimeViewPart part)
    {
        super(false);
        this.part = part;
    }

    @Override
    public ToolkitRepresentation<Parent, Node> openPanel(final DisplayModel model,
                                                         final Consumer<DisplayModel> close_handler) throws Exception
    {
        return openDisplayOnPage(null, model, close_handler);
    }

    @Override
    public ToolkitRepresentation<Parent, Node> openNewWindow(final DisplayModel model,
                                                             final Consumer<DisplayModel> close_handler) throws Exception
    {
        // Create new workbench page
        final IWorkbenchWindow window = PlatformUI.getWorkbench().openWorkbenchWindow(RuntimePerspective.ID, null);
        final IWorkbenchPage page = window.getActivePage();
        final Shell shell = window.getShell();
        shell.forceActive();
        shell.forceFocus();
        shell.moveAbove(null);
        return openDisplayOnPage(page, model, close_handler);
    }

    private ToolkitRepresentation<Parent, Node> openDisplayOnPage(final IWorkbenchPage page,
                                                                  final DisplayModel model,
                                                                  final Consumer<DisplayModel> close_handler) throws Exception
    {
        final DisplayInfo info = DisplayInfo.forModel(model);
        final RuntimeViewPart part = RuntimeViewPart.open(page, close_handler, info);
        final RCP_JFXRepresentation new_representation = part.getRepresentation();
        new_representation.representModel(part.getRoot(), model);
        return new_representation;
    }

    // 'Standalone' creates a detached RCP part
    @Override
    public ToolkitRepresentation<Parent, Node> openStandaloneWindow(final DisplayModel model,
                                                                    final Consumer<DisplayModel> close_handler) throws Exception
    {
        final DisplayInfo info = DisplayInfo.forModel(model);
        final RuntimeViewPart part = RuntimeViewPart.open(null, close_handler, info);

        // See http://tomsondev.bestsolution.at/2012/07/13/so-you-used-internal-api/
        final EModelService model_service = part.getSite().getService(EModelService.class);
        MPartSashContainerElement p = part.getSite().getService(MPart.class);
        // Part may be shared by several perspectives, get the shared instance
        if (p.getCurSharedRef() != null)
            p = p.getCurSharedRef();

        int extra_width = 0;
        int extra_height = 0;

        if ( SystemUtils.IS_OS_MAC ) {
            //  Tested on MacOS X 10.13.6
            extra_width = 8;
            extra_height = 55;
        } else if ( SystemUtils.IS_OS_LINUX ) {
            //  Tested on CentOS 7.5
            extra_width = 8;
            extra_height = 83;
        } else if ( SystemUtils.IS_OS_WINDOWS) {
            //  Tested on Windows 7 Professional
            extra_width = 24;
            extra_height = 71;
        }

        // Position as configured in display model
        model_service.detach(p,
                model.propX().getValue(),
                model.propY().getValue(),
                model.propWidth().getValue() + extra_width,
                model.propHeight().getValue() + extra_height);

        final RCP_JFXRepresentation new_representation = part.getRepresentation();
        new_representation.representModel(part.getRoot(), model);
        return new_representation;
    }

    @Override
    public void representModel(final Parent parent, final DisplayModel model)
            throws Exception
    {
        // Top-level Group of the part's Scene has pointer to RuntimeViewPart.
        // For EmbeddedDisplayWidget, the parent is inside the EmbeddedDisplayWidget,
        // and has no reference to the RuntimeViewPart.
        // Only track the top-level model, not embedded models.
        if (parent.getProperties().get(RuntimeViewPart.ROOT_RUNTIME_VIEW_PART) == part)
            part.trackCurrentModel(model);
        super.representModel(parent, model);
    }

    @Override
    public String showSaveAsDialog(final Widget widget, String initial_value)
    {
        final AtomicReference<String> result = new AtomicReference<String>();
        final Display display = Display.getDefault();

        // Unless initial value already contains a path,
        // suggest the first project
        if (initial_value.length() > 0  &&  ! initial_value.contains("/"))
        {
            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            for (IProject project : root.getProjects())
                if (project.isOpen())
                {
                    initial_value = "/" + project.getName() + "/" + initial_value;
                    break;
                }
        }

        final Path initial_path = new Path(initial_value);

        final Runnable doit = () ->
        {
            final ResourceSelectionDialog dialog = new ResourceSelectionDialog(display.getActiveShell(),
                    Messages.SelectWorkspaceFile, new String[] { "*.*" });
            dialog.setSelectedResource(initial_path);
            if (dialog.open() != Window.OK)
                return;
            final IPath resource = dialog.getSelectedResource();
            if (resource == null)
                return;
            result.set(resource.toPortableString());
        };

        if (Display.getCurrent() != null)
            doit.run();
        else
            display.syncExec(doit);

        return result.get();
    }

    @Override
    public void closeWindow(final DisplayModel model) throws Exception
    {
        final IWorkbenchPage page = part.getSite().getPage();
        final Display display = page.getWorkbenchWindow().getShell().getDisplay();
        display.asyncExec(() -> page.hideView(part));
    }

    @Override
    public void openFile(final String path) throws Exception
    {
        final IPath rcp_path = new Path(path);
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final IFile file = root.getFile(rcp_path);
        if (file.exists())
        {   // Open workspace file.
            // Clear the last-used-editor info to always get the default editor,
            // the one configurable via Preferences, General, Editors, File Associations,
            // and not whatever one user may have used last via Navigator's "Open With..".
            // Other cases below use a new, local file that won't have last-used-editor info, yet
            file.setPersistentProperty(IDE.EDITOR_KEY, null);
            IDE.openEditor(page, file, true);
        }
        else
        {   // Open file that is outside of workspace
            final IFileStore localFile = EFS.getLocalFileSystem().getStore(rcp_path);
            IDE.openEditorOnFileStore(page, localFile);
        }
    }

    @Override
    public void openWebBrowser(final String url) throws Exception
    {
        final IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport()
                  .createBrowser(IWorkbenchBrowserSupport.NAVIGATION_BAR | IWorkbenchBrowserSupport.LOCATION_BAR,
                                 "CSS", null, null);
        browser.openURL(new URL(url));
    }
}
