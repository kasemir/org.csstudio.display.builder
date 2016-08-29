/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.Messages;
import org.csstudio.display.builder.rcp.RuntimeViewPart;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.ui.util.dialogs.ResourceSelectionDialog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;

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
    public ToolkitRepresentation<Parent, Node> openNewWindow(final DisplayModel model,
                                                             final Consumer<DisplayModel> close_handler) throws Exception
    {
        final DisplayInfo info = DisplayInfo.forModel(model);
        final RuntimeViewPart part = RuntimeViewPart.open(close_handler, info);
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
        final RuntimeViewPart part = (RuntimeViewPart) parent.getProperties().get(RuntimeViewPart.ROOT_RUNTIME_VIEW_PART);
        if (part != null)
            part.trackCurrentModel(model);

        super.representModel(parent, model);
    }

    @Override
    public String showSaveAsDialog(final Widget widget, final String initial_value)
    {
        final AtomicReference<String> result = new AtomicReference<String>();
        final Display display = Display.getDefault();

        final Runnable doit = () ->
        {
            final ResourceSelectionDialog dialog = new ResourceSelectionDialog(display.getActiveShell(),
                    Messages.SelectWorkspaceFile, new String[] { "*.*" });
            dialog.setSelectedResource(new Path(initial_value));
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
}
