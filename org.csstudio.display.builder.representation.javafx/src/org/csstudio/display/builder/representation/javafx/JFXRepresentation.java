/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.widgets.ActionButtonWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.model.widgets.LEDWidget;
import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.csstudio.display.builder.model.widgets.ProgressBarWidget;
import org.csstudio.display.builder.model.widgets.RectangleWidget;
import org.csstudio.display.builder.model.widgets.TextUpdateWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ActionButtonRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.EmbeddedDisplayRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.GroupRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.LEDRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.LabelRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ProgressBarRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.RectangleRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.TextUpdateRepresentation;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/** Represent model items in JavaFX toolkit
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class JFXRepresentation extends ToolkitRepresentation<Group, Node>
{
    /** JavaFX 'Application' for standalone demo already provides
     *  a Stage, this one.
     *  First call to openNewWindow() uses that initial stage,
     *  while subsequent calls will create a new stage.
     */
    private Stage initial_stage;

    /** @param stage Initial Stage from JavaFX 'Application' */
    public JFXRepresentation(final Stage stage)
    {
        initial_stage = stage;

        // TODO Load available widget representation from registry
        register(ActionButtonWidget.class, ActionButtonRepresentation.class);
        register(EmbeddedDisplayWidget.class, EmbeddedDisplayRepresentation.class);
        register(GroupWidget.class, GroupRepresentation.class);
        register(LabelWidget.class, LabelRepresentation.class);
        register(LEDWidget.class, LEDRepresentation.class);
        register(ProgressBarWidget.class, ProgressBarRepresentation.class);
        register(RectangleWidget.class, RectangleRepresentation.class);
        register(TextUpdateWidget.class, TextUpdateRepresentation.class);
    }

    @Override
    public Group openNewWindow(final DisplayModel model, final Predicate<DisplayModel> close_request_handler)
    {
        // Use initial stage, or create new one if that's already used
        Stage stage = initial_stage;
        initial_stage = null;
        if (stage == null)
            stage = new Stage();

        stage.setTitle(model.widgetName().getValue());
        stage.setWidth(model.positionWidth().getValue());
        stage.setHeight(model.positionHeight().getValue());

        final Group parent = new Group();
        final Scene scene = new Scene(parent,
                model.positionWidth().getValue().doubleValue(),
                model.positionHeight().getValue().doubleValue());
        stage.setScene(scene);

        // Set style sheet
        scene.getStylesheets().add(getClass().getResource("opibuilder.css").toExternalForm());
        stage.show();

        stage.setOnCloseRequest((WindowEvent event) -> handleCloseRequest(event, model, close_request_handler));

        return parent;
    }

    private void handleCloseRequest(final WindowEvent event, final DisplayModel model,
                                    final Predicate<DisplayModel> close_request_handler)
    {
        try
        {
            if (close_request_handler.test(model) == false)
                event.consume();
        }
        catch (Exception ex)
        {
            Logger.getLogger(getClass().getName())
                  .log(Level.WARNING, "Close request handler failed", ex);
        }
    }

    @Override
    public void execute(final Runnable command)
    {
        Platform.runLater(command);
    }

    @Override
    public Group disposeRepresentation(final DisplayModel model)
    {
        final Group parent = model.getUserData(DisplayModel.USER_DATA_TOOLKIT_PARENT);
        parent.getChildren().clear();
        return parent;
    }
}
