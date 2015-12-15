/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import org.csstudio.display.builder.editor.EditorUtil;
import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.editor.properties.PropertyPanel;
import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.properties.IPropertySheetPage;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class PropertyPage extends Page implements IPropertySheetPage
{
    private final PropertyPanel property_panel;

    private FXCanvas canvas;

    public PropertyPage(final WidgetSelectionHandler widget_selection,
                               final UndoableActionManager undo)
    {
        property_panel = new PropertyPanel(widget_selection, undo);
    }

    @Override
    public void createControl(final Composite parent)
    {
        canvas = new FXCanvas(parent, SWT.NONE);
        // StackPane w/ panel as single child to 'fill' the available space.
        final StackPane root = new StackPane(property_panel);
        final Scene scene = new Scene(root, 200.0, 400.0);
        EditorUtil.setSceneStyle(scene);
        canvas.setScene(scene);
    }

    @Override
    public Control getControl()
    {
        return canvas;
    }

    @Override
    public void setFocus()
    {
        canvas.setFocus();
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection)
    {
        // NOP

    }

}
