/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.EditorUtil;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Group;
import javafx.scene.Scene;

/** RCP 'Editor' for the display builder
 *  @author Kay Kasemir
 */
public class DisplayEditorPart extends EditorPart
{
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final JFXRepresentation toolkit = new JFXRepresentation();

    private DisplayModel model;

    private FXCanvas fx_canvas;

    private Group model_parent;

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

        // Must first create FXCanvas, then the scene, and set canvas' scene.
        // When instead creating scene, then FXCanvas, then setting canvas' scene,
        // there is an IllegalStateException: Not on FX application thread
        fx_canvas = new FXCanvas(parent, SWT.NONE);

        // TODO Palette
        model_parent = new Group();
        final Group root = new Group(model_parent);
        Scene scene = new Scene(root);

        fx_canvas.setScene(scene);

        final IEditorInput input = getEditorInput();
        final IFile file = input.getAdapter(IFile.class);
        if (file != null)
            EditorUtil.getExecutor().execute(() -> loadModel(file));
    }

    @Override
    public void setFocus()
    {
        fx_canvas.setFocus();
    }

    private void loadModel(final IFile file)
    {
        try
        {
            final ModelReader reader = new ModelReader(file.getContents());
            final DisplayModel model = reader.readModel();
            setModel(model);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot load " + file, ex);
        }
    }

    private void setModel(final DisplayModel model)
    {
        toolkit.execute(() ->
        {
            setPartName(model.getName());

            // TODO
            //            widget_naming.clear();
            //            selection.clear();
            //            tree.setModel(model);
            //            group_handler.setModel(model);

            final DisplayModel old_model = this.model;
            if (old_model != null)
                toolkit.disposeRepresentation(old_model);
            this.model = Objects.requireNonNull(model);

            // Create representation for model items
            try
            {
                toolkit.representModel(model_parent, model);
            }
            catch (final Exception ex)
            {
                logger.log(Level.SEVERE, "Error representing model", ex);
            }
        });
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
        // TODO Auto-generated method stub
        super.dispose();
    }
}
