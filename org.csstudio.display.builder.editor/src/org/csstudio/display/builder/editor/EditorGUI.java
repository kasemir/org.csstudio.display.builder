/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.actions.ActionGUIHelper;
import org.csstudio.display.builder.editor.actions.EnableGridAction;
import org.csstudio.display.builder.editor.actions.EnableSnapAction;
import org.csstudio.display.builder.editor.properties.PropertyPanel;
import org.csstudio.display.builder.editor.tracker.SelectionTracker;
import org.csstudio.display.builder.editor.undo.UndoableActionManager;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.representation.ToolkitListener;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

@SuppressWarnings("nls")
public class EditorGUI
{
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Executor executor = ForkJoinPool.commonPool();
    private final ToolkitRepresentation<Group, Node> toolkit;

    private final UndoableActionManager undo = new UndoableActionManager();

    private final BorderPane toolbar_center_status = new BorderPane();
    private final Group model_parent = new Group();
    private final Group edit_tools = new Group();
    // editor: model's representation in background, edit_tools on top
    private final ScrollPane editor = new ScrollPane(new Pane(model_parent, edit_tools));

    private final SelectionTracker selection_tracker = new SelectionTracker(undo);
    private final PropertyPanel property_panel = new PropertyPanel(undo);

    private volatile DisplayModel model;


    public EditorGUI(final Stage stage)
    {
        toolkit = new JFXRepresentation(stage);
        createElements(stage);
        hookListeners();
    }

    private void createElements(final Stage stage)
    {
        // BorderPane with
        //    toolbar
        //    center = editor | palette | property_panel
        //    status
        final ToolBar toolbar = new ToolBar(
                new Button("Do"),
                new Separator(),
                ActionGUIHelper.createToggleButton(new EnableGridAction(selection_tracker)),
                ActionGUIHelper.createToggleButton(new EnableSnapAction(selection_tracker)),
                new Separator(),
                new Button("Something"));

        final Palette palette = new Palette();

        final SplitPane center = new SplitPane();
        center.getItems().addAll(editor, palette.create(), property_panel.create());
        center.setDividerPositions(0.6, 0.78);

        final Label status = new Label("Status");

        toolbar_center_status.setTop(toolbar);
        toolbar_center_status.setCenter(center);
        toolbar_center_status.setBottom(status);
        BorderPane.setAlignment(center, Pos.TOP_LEFT);

        stage.setTitle("Editor");
        stage.setWidth(1000);
        stage.setHeight(600);
        final Scene scene = new Scene(toolbar_center_status, 1000, 600);
        stage.setScene(scene);

        // Set style sheet
        scene.getStylesheets().add(getClass().getResource("opieditor.css").toExternalForm());

        stage.show();

        edit_tools.getChildren().addAll(selection_tracker);
    }

    private void hookListeners()
    {
        toolkit.addListener(new ToolkitListener()
        {
            @Override
            public void handleClick(final Widget widget)
            {
                // TODO Toggle selection (add to existing) when Ctrl is held
                logger.log(Level.FINE, "Selected {0}",  widget);
                setSelectedWidgets(Arrays.asList(widget));
            }
        });

        editor.setOnMousePressed((MouseEvent event) ->
        {
            logger.log(Level.FINE, "Clicked in 'editor' De-select all widgets");
            setSelectedWidgets(Collections.emptyList());
        });

        toolbar_center_status.setOnKeyPressed((KeyEvent event) ->
        {
            if (event.isControlDown())
                if (event.getCode() == KeyCode.Z)
                    undo.undoLast();
                else if (event.getCode() == KeyCode.Y)
                    undo.redoLast();
        });
    }

    /** @param selected_widgets Selected widgets */
    private void setSelectedWidgets(final List<Widget> selected_widgets)
    {
        selection_tracker.setSelectedWidgets(selected_widgets);
        property_panel.setSelectedWidgets(selected_widgets);
    }

    public void loadModel(final String filename)
    {
        executor.execute(() ->
        {
            try
            {
                final ModelReader reader = new ModelReader(new FileInputStream(filename));
                final DisplayModel model = reader.readModel();

                // Representation needs to be created in UI thread
                toolkit.execute(() -> setModel(model));
            }
            catch (final Exception ex)
            {
                logger.log(Level.SEVERE, "Cannot start", ex);
            }
        });
    }

    private void setModel(final DisplayModel model)
    {
        this.model = model;
        // Create representation for model items
        try
        {
            toolkit.representModel(model_parent, model);
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public boolean handleClose()
    {
        toolkit.disposeRepresentation(model);
        return true;
    }
}
