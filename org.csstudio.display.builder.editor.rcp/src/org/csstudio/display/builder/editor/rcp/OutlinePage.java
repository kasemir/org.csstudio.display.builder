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
import org.csstudio.display.builder.editor.tree.WidgetTree;
import org.csstudio.display.builder.model.DisplayModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

/** Outline view
 *
 *  <p>Displays tree of widgets
 *  @author Kay Kasemir
 */
public class OutlinePage extends Page implements IContentOutlinePage
{
    private final WidgetTree tree;

    private FXCanvas canvas;

    public OutlinePage(final WidgetSelectionHandler widget_selection)
    {
        tree = new WidgetTree(widget_selection);
    }

    public void setModel(final DisplayModel model)
    {
        tree.setModel(model);
    }

    @Override
    public void createControl(final Composite parent)
    {
        canvas = new FXCanvas(parent, SWT.NONE);
        // StackPane w/ tree as single child to 'fill' the available space.
        final StackPane root = new StackPane(tree.create());
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
    public void addSelectionChangedListener(ISelectionChangedListener listener)
    {
        // TODO Pass selection from WidgetTree on to SWT/RCP
    }

    @Override
    public void removeSelectionChangedListener(
            ISelectionChangedListener listener)
    {
    }

    @Override
    public ISelection getSelection()
    {
        return null;
    }

    @Override
    public void setSelection(ISelection selection)
    {
    }
}
