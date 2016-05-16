/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/** Content of the {@link NewDisplayWizard}
 *  @author Kay Kasemir
 */
public class NewDisplayWizardPage extends WizardPage
{
    private final ISelection selection;
    private Text containerText;
    private Text fileText;

    public NewDisplayWizardPage(final ISelection selection)
    {
        super("page1");
        this.selection = selection;
        setTitle(Messages.NewDisplay_Title);
        setDescription(Messages.NewDisplay_Description);
    }

    @Override
    public void createControl(final Composite parent)
    {
        final Composite container = new Composite(parent, SWT.NULL);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.verticalSpacing = 9;
        container.setLayout(layout);

        Label label = new Label(container, SWT.NULL);
        label.setText(Messages.NewDisplay_Container);

        containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
        containerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        final ModifyListener value_check = e -> checkValues();
        containerText.addModifyListener(value_check);

        final Button button = new Button(container, SWT.PUSH);
        button.setText(Messages.NewDisplay_Browse);
        button.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                handleBrowse();
            }
        });
        label = new Label(container, SWT.NULL);
        label.setText(Messages.NewDisplay_Filename);

        fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
        fileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fileText.addModifyListener(value_check);
        initialize();
        checkValues();
        setControl(container);
    }

    private void initialize()
    {
        // Try to determine container from selection
        if (selection instanceof IStructuredSelection  &&  !selection.isEmpty())
        {
            final IStructuredSelection ssel = (IStructuredSelection) selection;
            if (ssel.size() > 1)
                return;
            final Object obj = ssel.getFirstElement();
            if (obj instanceof IResource)
            {
                final IContainer container;
                if (obj instanceof IContainer)
                    container = (IContainer) obj;
                else
                    container = ((IResource) obj).getParent();
                containerText.setText(container.getFullPath().toString());
            }
        }
        fileText.setText(Messages.NewDisplay_InitialName);
    }

    private void handleBrowse()
    {
        final ContainerSelectionDialog dialog = new ContainerSelectionDialog(
            getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
            Messages.NewDisplay_BrowseTitle);
        if (dialog.open() != ContainerSelectionDialog.OK)
            return;
        final Object[] result = dialog.getResult();
        if (result.length == 1)
            containerText.setText(((Path) result[0]).toString());
    }

    private void checkValues()
    {
        final IResource container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(getContainerName()));
        final String fileName = getFileName();

        if (getContainerName().length() == 0)
        {
            updateStatus(Messages.NewDisplay_MissingContainer);
            return;
        }
        if (container == null || (container.getType()
                & (IResource.PROJECT | IResource.FOLDER)) == 0)
        {
            updateStatus(Messages.NewDisplay_ContainerNotFound);
            return;
        }
        if (!container.isAccessible())
        {
            updateStatus(Messages.NewDisplay_NotWriteable);
            return;
        }
        if (fileName.length() == 0)
        {
            updateStatus(Messages.NewDisplay_MissingFileName);
            return;
        }
        if (fileName.replace('\\', '/').indexOf('/', 1) > 0)
        {
            updateStatus(Messages.NewDisplay_InvalidFileName);
            return;
        }
        int dotLoc = fileName.lastIndexOf('.');
        if (dotLoc != -1)
        {
            final String ext = fileName.substring(dotLoc + 1);
            if (ext.equalsIgnoreCase(Messages.NewDisplay_Extension) == false)
            {
                updateStatus(Messages.NewDisplay_ExtensionError);
                return;
            }
        }
        updateStatus(null);
    }

    private void updateStatus(final String message)
    {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public String getContainerName()
    {
        return containerText.getText();
    }

    public String getFileName()
    {
        return fileText.getText();
    }
}