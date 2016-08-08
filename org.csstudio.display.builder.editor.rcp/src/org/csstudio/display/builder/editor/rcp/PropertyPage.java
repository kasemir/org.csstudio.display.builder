/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.EditorUtil;
import org.csstudio.display.builder.editor.properties.PropertyPanel;
import org.csstudio.display.builder.representation.javafx.AutocompleteMenu;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.properties.IPropertySheetPage;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

/** RCP property page wrapper of the editor's PropertyPanel
 *  @author Kay Kasemir
 */
public class PropertyPage extends Page implements IPropertySheetPage, IAdaptable
{
    /** ISaveablePart that's clean, no need to save.
     *
     *  Eclipse adapts the property page to its editor.
     *  If editor is 'dirty', the property page also appears 'dirty'
     *  and cannot be closed.
     *
     *  https://bugs.eclipse.org/bugs/show_bug.cgi?id=372799,
     *  https://github.com/ControlSystemStudio/cs-studio/issues/1619
     *
     *  Workaround:
     *  Force adaptation of property page to clean_saveable.
     */
    private final static ISaveablePart clean_saveable = new ISaveablePart()
    {
        @Override
        public boolean isSaveOnCloseNeeded()
        {
            return false;
        }

        @Override
        public boolean isSaveAsAllowed()
        {
            return false;
        }

        @Override
        public boolean isDirty()
        {
            return false;
        }

        @Override
        public void doSaveAs()
        {
            // NOP
        }

        @Override
        public void doSave(IProgressMonitor monitor)
        {
            // NOP
        }
    };
    private final PropertyPanel property_panel;

    private FXCanvas canvas;

    public PropertyPage(final DisplayEditor editor)
    {
        property_panel = new PropertyPanel(editor);
        final AutocompleteMenu menu = property_panel.getAutocompleteMenu();
        menu.setUpdater(new AutoCompleteUpdater(menu));
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object getAdapter(final Class adapter)
    {
        if (adapter == ISaveablePart.class)
            return clean_saveable;
        return null;
    }
}
