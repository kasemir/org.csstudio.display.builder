/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.util.function.Consumer;

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

/** Represent model items in JavaFX toolkit
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class JFXRepresentation extends ToolkitRepresentation<Group, Node>
{
    public static final String ACTIVE_MODEL = "_active_model";

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

    /** Create a Scene suitable for representing model
     *  @return Scene
     *  @see JFXRepresentation#getSceneRoot(Scene)
     */
    public Scene createScene()
    {
        final Group parent = new Group();
        final Scene scene = new Scene(parent);
        // Fetch css relative to JFXRepresentation, not derived class
        final String css = JFXRepresentation.class.getResource("opibuilder.css").toExternalForm();
        scene.getStylesheets().add(css);
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
    public Group openNewWindow(final DisplayModel model, Consumer<DisplayModel> close_handler) throws Exception
    {   // Use JFXStageRepresentation or RCP-based implementation
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void representModel(final Group root, final DisplayModel model) throws Exception
    {
        root.getProperties().put(ACTIVE_MODEL, model);
        super.representModel(root, model);
    }

    @Override
    public Group disposeRepresentation(final DisplayModel model)
    {
        final Group root = super.disposeRepresentation(model);
        root.getProperties().remove(ACTIVE_MODEL);
        return root;
    }

    @Override
    public void execute(final Runnable command)
    {
        Platform.runLater(command);
    }
}
