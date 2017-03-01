/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import org.csstudio.display.builder.editor.EditorUtil;
import org.csstudio.display.builder.editor.properties.PropertyPanel;
import org.csstudio.display.builder.representation.javafx.AutocompleteMenu;
import org.csstudio.javafx.swt.JFX_SWT_Wrapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.properties.IPropertySheetPage;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

/** RCP property page wrapper of the editor's PropertyPanel
 *  @author Kay Kasemir
 */
public class PropertyPage extends Page implements IPropertySheetPage, ISaveablePart
{
    private final DisplayEditorPart editor_part;
    private final PropertyPanel property_panel;

    private Control canvas;

    public PropertyPage(final DisplayEditorPart editor_part)
    {
        this.editor_part = editor_part;
        property_panel = new PropertyPanel(editor_part.getDisplayEditor());
        final AutocompleteMenu menu = property_panel.getAutocompleteMenu();
        menu.setUpdater(new AutoCompleteUpdater(menu));
    }

    @Override
    public void createControl(final Composite parent)
    {
        final JFX_SWT_Wrapper wrapper = new JFX_SWT_Wrapper(parent, () ->
        {
            // StackPane w/ panel as single child to 'fill' the available space.
            final StackPane root = new StackPane(property_panel);
            final Scene scene = new Scene(root, 200.0, 400.0);
            EditorUtil.setSceneStyle(scene);
            return scene;
        });
        canvas = wrapper.getFXCanvas();
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

    // Implement ISaveablePart,
    // forwarding all operations to the editor_part,
    // to allow "Save" (Ctrl-S) from within the property view.
    @Override
    public void doSave(final IProgressMonitor monitor)
    {
        editor_part.doSave(monitor);

        // Bug:
        // 1) Close property view
        // 2) Make editor dirty
        // 3) Open property view -> It now has a '*' dirty marker
        // 4) File/Save or Ctrl-S while in property view
        // -> Editor saved, editor no longer dirty,
        //    but property view remains with '*'..
    }

    @Override
    public void doSaveAs()
    {
        editor_part.doSaveAs();
    }

    @Override
    public boolean isDirty()
    {
        return editor_part.isDirty();
    }

    @Override
    public boolean isSaveAsAllowed()
    {
        return editor_part.isSaveAsAllowed();
    }

    @Override
    public boolean isSaveOnCloseNeeded()
    {   // Editor may return true, but always OK to close the property view
        return false;
    }
}
