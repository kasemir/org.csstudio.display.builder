/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import static org.csstudio.display.builder.rcp.Plugin.logger;

import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.ModelLoader;
import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.Messages;
import org.csstudio.display.builder.rcp.Plugin;
import org.csstudio.display.builder.rcp.RuntimeViewPart;
import org.csstudio.display.builder.representation.javafx.JFXStageRepresentation;
import org.csstudio.display.builder.runtime.ActionUtil;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import javafx.scene.Parent;
import javafx.stage.Stage;

/** Action to run display in standalone Stage
 *
 *  <p>Executes display in JFX Stage without menu bar,
 *  toolbar, perspectives, context menu etc.
 *
 *  <p>Action is not reversible.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class StandaloneAction extends Action
{
    private final RuntimeViewPart view;
    private JFXStageRepresentation toolkit;
    private Stage stage;

    public StandaloneAction(final RuntimeViewPart view)
    {
        super(Messages.OpenStandalone,
              AbstractUIPlugin.imageDescriptorFromPlugin(Plugin.ID, "icons/standalone.png"));
        this.view = view;
    }

    @Override
    public void run()
    {
        final DisplayInfo display_info = view.getDisplayInfo();
        if (display_info == null)
        {
            logger.log(Level.WARNING, "No display info for " + view);
            return;
        }

        stage = new Stage();
        stage.setTitle(view.getPartName());
        stage.setWidth(600);
        stage.setHeight(400);
        stage.show();

        toolkit = new JFXStageRepresentation(stage);
        RuntimeUtil.hookRepresentationListener(toolkit);

        // Close view
        view.getSite().getPage().hideView(view);

        // Initiate loading model into standalone stage
        RuntimeUtil.getExecutor().execute(() -> loadModel(display_info));
    }

    private void loadModel(final DisplayInfo display_info)
    {
        try
        {
            final DisplayModel model = ModelLoader.loadModel(null, display_info.getPath());
            // Macros: Start with display info, add those of loaded model
            final Macros combined_macros = Macros.merge(display_info.getMacros(), model.propMacros().getValue());
            model.propMacros().setValue(combined_macros);
            // Representation needs to be created in UI thread
            toolkit.execute(() -> representModel(model));
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "Cannot load " + display_info.getPath(), ex);
        }
    }

    private void representModel(final DisplayModel model)
    {
        try
        {   // Create representation for model items
            final Parent parent = toolkit.configureStage(model, this::handleClose);
            toolkit.representModel(parent, model);
        }
        catch (final Exception ex)
        {
            logger.log(Level.SEVERE, "Cannot represent model", ex);
        }

        // Start runtimes in background
        RuntimeUtil.getExecutor().execute(() -> RuntimeUtil.startRuntime(model));
    }

    private boolean handleClose(final DisplayModel model)
    {
        ActionUtil.handleClose(model);
        stage.close();
        return true;
    }
}
