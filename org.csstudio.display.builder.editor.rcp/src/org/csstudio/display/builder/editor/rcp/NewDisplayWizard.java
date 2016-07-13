/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/** Wizard for creating a new (empty) display file.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class NewDisplayWizard extends Wizard implements INewWizard
{
    /** Wizard ID registered in plugin.xml */
    public static final String ID = "org.csstudio.display.builder.editor.rcp.NewDisplayWizard";

    private NewDisplayWizardPage page;
    private ISelection selection;

    public NewDisplayWizard()
    {
        setNeedsProgressMonitor(true);
    }

    // If activated from Navigator, selection may contain parent folder
    @Override
    public void init(final IWorkbench workbench, final IStructuredSelection selection)
    {
        this.selection = selection;
    }

    @Override
    public void addPages()
    {
        page = new NewDisplayWizardPage(selection);
        addPage(page);
    }

    @Override
    public boolean performFinish()
    {
        final String containerName = page.getContainerName();
        final String fileName = page.getFileName();
        try
        {
            getContainer().run(true, false, monitor ->
            {
                try
                {
                    doFinish(containerName, fileName, monitor);
                }
                catch (Exception ex)
                {
                    throw new InvocationTargetException(ex);
                }
                finally
                {
                    monitor.done();
                }
            });
        }
        catch (InterruptedException ex)
        {
            return false;
        }
        catch (InvocationTargetException ex)
        {
            MessageDialog.openError(getShell(), Messages.NewDisplay_Error,
                    ex.getTargetException().getMessage());
            return false;
        }
        return true;
    }

    private void doFinish(final String containerName, final String fileName,
                          final IProgressMonitor monitor) throws Exception
    {
        // create a sample file
        monitor.beginTask("Creating " + fileName, 2);
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final IResource resource = root.findMember(new Path(containerName));
        if (!resource.exists() || !(resource instanceof IContainer))
            throw new Exception("Container \"" + containerName + "\" does not exist.");
        final IContainer container = (IContainer) resource;
        final IFile file = container.getFile(new Path(fileName));
        try
        {
            final InputStream stream = getInitialContent();
            if (file.exists())
            {
                file.setContents(stream, true, true, monitor);
            }
            else
            {
                file.create(stream, true, monitor);
            }
            stream.close();
        }
        catch (IOException e)
        {
        }
        monitor.worked(1);
        monitor.setTaskName("Opening file for editing...");
        getShell().getDisplay().asyncExec(() ->
        {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            try
            {
                IDE.openEditor(page, file, true);
            }
            catch (PartInitException e)
            {
            }
        });
        monitor.worked(1);
    }

    private InputStream getInitialContent()
    {
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
"<display version=\"1.0.0\">" +
"  <name>Display</name>" +
"  <widget type=\"label\" version=\"2.0.0\">" +
"    <name>Label</name>" +
"    <width>281</width>" +
"    <height>31</height>" +
"    <text>My Display</text>" +
"    <font>" +
"      <font name=\"Header 1\" family=\"Liberation Sans\" style=\"BOLD\" size=\"22.0\">" +
"      </font>" +
"    </font>" +
"  </widget>" +
"</display>";
        return new ByteArrayInputStream(xml.getBytes());
    }
}