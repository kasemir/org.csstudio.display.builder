/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import java.io.FileInputStream;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.EditorUtil;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Parent;
import javafx.scene.Scene;

/** RCP 'Editor' for the display builder
 *  @author Kay Kasemir
 */
public class DisplayEditorPart extends EditorPart
{
    private final static JFXRepresentation toolkit = new JFXRepresentation();

    private final Logger logger = Logger.getLogger(getClass().getName());

    private FXCanvas fx_canvas;

    private final DisplayEditor editor = new DisplayEditor(toolkit);

    public DisplayEditorPart()
    {
        // TODO Properties
        // TODO Outline
    }

    @Override
    public void init(final IEditorSite site, final IEditorInput input)
            throws PartInitException
    {
        setSite(site);
        // TODO setInput(new NoResourceEditorInput(input));
        setInput(input);
    }

    @Override
    public void createPartControl(final Composite parent)
    {
        parent.setLayout(new FillLayout());

        // Must first create FXCanvas, because that initializes JFX.
        // When creating FXCanvas later, there will be JFX errors
        // like "Not on FX application thread", "Toolkit not initialized"
        fx_canvas = new FXCanvas(parent, SWT.NONE);

        final Parent root = editor.create();
        final Scene scene = new Scene(root);
        EditorUtil.setSceneStyle(scene);

        fx_canvas.setScene(scene);

        createToolbar();

        final IEditorInput input = getEditorInput();
        final IFile file = input.getAdapter(IFile.class);
        if (file != null)
            loadModel(file);
    }

    private void loadModel(final IFile file)
    {
        // Load model in background thread, then set it
        CompletableFuture.supplyAsync(() -> doLoadModel(file), EditorUtil.getExecutor())
                         .thenAccept(this::setModel);
    }

    private DisplayModel doLoadModel(final IFile file)
    {
        try
        {
            final ModelReader reader = new ModelReader(new FileInputStream(file.getLocation().toFile()));
            return reader.readModel();
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot load display " + file, ex);
            return null;
        }
    }

    private void setModel(final DisplayModel model)
    {
        // TODO handle model == null
        // In UI thread..
        toolkit.execute(() ->
        {
            setPartName(model.getName());
            editor.setModel(model);
        });
    }

    private void createToolbar()
    {
        // TODO Toolbar..
        final IToolBarManager toolbar = getEditorSite().getActionBars().getToolBarManager();
        toolbar.add(new Action("Test")
        {
        });
    }

    @Override
    public void setFocus()
    {
        fx_canvas.setFocus();
    }

    @Override
    public boolean isDirty()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSaveAsAllowed()
    {
        return true;
    }

    @Override
    public void doSave(final IProgressMonitor monitor)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void doSaveAs()
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void dispose()
    {
        editor.dispose();
        super.dispose();
    }
}
