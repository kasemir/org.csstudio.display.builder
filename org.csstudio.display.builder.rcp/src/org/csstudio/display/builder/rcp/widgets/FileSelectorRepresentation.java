/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.widgets;

import java.io.File;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.rcp.Messages;
import org.csstudio.display.builder.representation.javafx.widgets.JFXBaseRepresentation;
import org.csstudio.ui.util.dialogs.ResourceSelectionDialog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/** JFX Representation for {@link FileSelectorWidget}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class FileSelectorRepresentation extends JFXBaseRepresentation<Button, FileSelectorWidget>
{
    private final DirtyFlag dirty_size = new DirtyFlag();

    @Override
    protected Button createJFXNode() throws Exception
    {
        final Button button = new Button();
        button.setGraphic(new ImageView(new Image(FileSelectorWidget.WIDGET_DESCRIPTOR.getIconStream())));

        if (! toolkit.isEditMode())
            button.setOnAction(event -> selectFile());

        return button;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addPropertyListener(this::sizeChanged);
        model_widget.positionHeight().addPropertyListener(this::sizeChanged);
    }

    private void sizeChanged(final WidgetProperty<Integer> property, final Integer old_value, final Integer new_value)
    {
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }


    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
            jfx_node.setPrefSize(model_widget.positionWidth().getValue(),
                                 model_widget.positionHeight().getValue());
    }

    private void selectFile()
    {
        FileSelectorWidget.FileComponent component = model_widget.behaviorComponent().getValue();

        String path = VTypeUtil.getValueString(model_widget.runtimeValue().getValue(), false);

        // This is a JFX representation, but using RCP/SWT dialogs
        // which are aware of the workspace and match other load/save dialogs
        final Shell shell = Display.getCurrent().getActiveShell();
        if (model_widget.behaviorFilespace().getValue() == FileSelectorWidget.Filespace.WORKSPACE)
        {
            final ResourceSelectionDialog dialog = new ResourceSelectionDialog(shell,
                    Messages.SelectWorkspaceFile,
                    component == FileSelectorWidget.FileComponent.DIRECTORY ? null : new String[] { "*.*" });
            dialog.setSelectedResource(new Path(path));

            if (dialog.open() != Window.OK)
                return;
            final IPath resource = dialog.getSelectedResource();
            if (resource == null)
                return;

            switch (model_widget.behaviorComponent().getValue())
            {
            case FULL:
            case DIRECTORY:
                // ResourceSelectionDialog filter already
                // handles full path vs. directory
                path = resource.toPortableString();
                break;
            case FULLNAME:
                path = resource.lastSegment();
                break;
            case BASENAME:
                path = resource.removeFileExtension().lastSegment();
                break;
            }
        }
        else
        {
            if (component == FileSelectorWidget.FileComponent.DIRECTORY)
            {
                final DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setFilterPath(path);
                path = dialog.open();
                if (path == null)
                    return;
            }
            else
            {
                final FileDialog dialog = new FileDialog(shell, SWT.NONE);
                dialog.setFileName(path);
                path = dialog.open();
                if (path == null)
                    return;
            }
            File file = new File(path);
            switch (model_widget.behaviorComponent().getValue())
            {
            case FULL:
            case DIRECTORY:
                // DirectoryDialog vs. FileDialog already handle this
                break;
            case FULLNAME:
                path = file.getName();
                break;
            case BASENAME:
                path = file.getName();
                final int sep = path.lastIndexOf('.');
                if (sep >= 0)
                    path = path.substring(0, sep);
                break;
            }
        }

        toolkit.fireWrite(model_widget, path);
    }
}
