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

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.widgets.ActionButtonWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.model.widgets.ImageWidget;
import org.csstudio.display.builder.model.widgets.LEDWidget;
import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.csstudio.display.builder.model.widgets.ProgressBarWidget;
import org.csstudio.display.builder.model.widgets.RectangleWidget;
import org.csstudio.display.builder.model.widgets.TextEntryWidget;
import org.csstudio.display.builder.model.widgets.TextUpdateWidget;
import org.csstudio.display.builder.model.widgets.XYPlotWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ActionButtonRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.EmbeddedDisplayRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.GroupRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.LEDRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.LabelRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ProgressBarRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.RectangleRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.TextEntryRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.TextUpdateRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.plots.ImageRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.plots.XYPlotRepresentation;

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
    /** Construct new JFX representation */
    public JFXRepresentation()
    {
        // TODO Load available widget representations from registry
        register(ActionButtonWidget.class, ActionButtonRepresentation.class);
        register(EmbeddedDisplayWidget.class, EmbeddedDisplayRepresentation.class);
        register(GroupWidget.class, GroupRepresentation.class);
        register(ImageWidget.class, ImageRepresentation.class);
        register(LabelWidget.class, LabelRepresentation.class);
        register(LEDWidget.class, LEDRepresentation.class);
        register(ProgressBarWidget.class, ProgressBarRepresentation.class);
        register(RectangleWidget.class, RectangleRepresentation.class);
        register(TextEntryWidget.class, TextEntryRepresentation.class);
        register(TextUpdateWidget.class, TextUpdateRepresentation.class);
        register(XYPlotWidget.class, XYPlotRepresentation.class);
    }

    /** Configure an existing Stage
     *  @param stage Stage to configure
     *  @param model Model that provides stage size
     *  @param close_request_handler Close request handler that will be hooked to stage's close handler
     *  @return Top-level Group
     */
    public Group configureStage(final Stage stage, final DisplayModel model, final Predicate<DisplayModel> close_request_handler)
    {
        stage.setTitle(model.widgetName().getValue());
        stage.setWidth(model.positionWidth().getValue());
        stage.setHeight(model.positionHeight().getValue());
        stage.setX(model.positionX().getValue());
        stage.setY(model.positionY().getValue());

        final Scene scene = createScene(model);
        stage.setScene(scene);
        stage.setOnCloseRequest((WindowEvent event) -> handleCloseRequest(event, model, close_request_handler));
        stage.show();

        return getSceneRoot(scene);
    }

    /** Create a Scene suitable for representing model
     *  @param model Model to represent
     *  @return Scene
     *  @see JFXRepresentation#getSceneRoot(Scene)
     */
    public Scene createScene(final DisplayModel model)
    {
        final Group parent = new Group();
        final Scene scene = new Scene(parent,
                model.positionWidth().getValue().doubleValue(),
                model.positionHeight().getValue().doubleValue());
        scene.getStylesheets().add(getClass().getResource("opibuilder.css").toExternalForm());
        return scene;
    }

    /** @see JFXRepresentation#createScene(DisplayModel)
     *  @param scene Scene created for model
     *  @return Root element
     */
    public Group getSceneRoot(final Scene scene)
    {
        return (Group) scene.getRoot();
    }

    @Override
    public Group openNewWindow(final DisplayModel model, final Predicate<DisplayModel> close_request_handler)
    {
        final Stage stage = new Stage();
        return configureStage(stage, model, close_request_handler);
    }

    private void handleCloseRequest(final WindowEvent event, final DisplayModel model,
                                    final Predicate<DisplayModel> close_request_handler)
    {
        try
        {
            if (close_request_handler.test(model) == false)
                event.consume();
        }
        catch (final Exception ex)
        {
            logger.log(Level.WARNING, "Close request handler failed", ex);
        }
    }

    @Override
    public void execute(final Runnable command)
    {
        Platform.runLater(command);
    }
}
