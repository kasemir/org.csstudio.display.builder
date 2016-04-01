/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.util.function.Consumer;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/** Represent model items in JavaFX toolkit based on standalone Stage
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class JFXStageRepresentation extends JFXRepresentation
{
    /** Configure an existing Stage
     *  @param stage Stage to configure
     *  @param model Model that provides stage size
     *  @param close_request_handler Close request handler that will be hooked to stage's close handler
     *  @return Top-level Parent
     */
    public Parent configureStage(final Stage stage, final DisplayModel model, final Consumer<DisplayModel> close_request_handler)
    {
        stage.setTitle(model.widgetName().getValue());
        stage.setWidth(model.positionWidth().getValue());
        stage.setHeight(model.positionHeight().getValue());
        stage.setX(model.positionX().getValue());
        stage.setY(model.positionY().getValue());

        final Scene scene = new Scene(createModelRoot());
        setSceneStyle(scene);
        stage.setScene(scene);
        stage.setOnCloseRequest((WindowEvent event) -> handleCloseRequest(scene, close_request_handler));
        stage.show();

        // If ScenicView.jar is added to classpath, open it here
        // ScenicView.show(scene);

        return getModelParent();
    }

    @Override
    public Parent openNewWindow(final DisplayModel model, final Consumer<DisplayModel> close_request_handler) throws Exception
    {
        final Stage stage = new Stage();
        return configureStage(stage, model, close_request_handler);
    }

    private void handleCloseRequest(final Scene scene,
                                    final Consumer<DisplayModel> close_request_handler)
    {
        final Parent root = getModelParent();
        final DisplayModel model = (DisplayModel) root.getProperties().get(ACTIVE_MODEL);

        try
        {
            if (model != null)
                close_request_handler.accept(model);
        }
        catch (final Exception ex)
        {
            logger.log(Level.WARNING, "Close request handler failed", ex);
        }
    }
}
