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
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.actions.ActionGUIHelper;
import org.csstudio.display.builder.editor.actions.EnableGridAction;
import org.csstudio.display.builder.editor.actions.EnableSnapAction;
import org.csstudio.display.builder.editor.palette.Palette;
import org.csstudio.display.builder.editor.properties.PropertyPanel;
import org.csstudio.display.builder.editor.tracker.SelectionTracker;
import org.csstudio.display.builder.editor.tree.WidgetTree;
import org.csstudio.display.builder.editor.undo.AddWidgetAction;
import org.csstudio.display.builder.editor.undo.UndoableActionManager;
import org.csstudio.display.builder.editor.util.GeometryTools;
import org.csstudio.display.builder.editor.util.Rubberband;
import org.csstudio.display.builder.editor.util.WidgetTransfer;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.representation.ToolkitListener;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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

/** All the editor components.
 *
 *  <p>Layers from 'top' down in the 'editor_pane':
 *  <pre>
 *  +-- edit_tools   : SelectionTracker
 *  +-- model_parent : Hosts representation of model widgets
 *  + editor_pane    : Rubberband selection of widgets, drop target for new widgets
 *  </pre>
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EditorGUI
{
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Executor executor = ForkJoinPool.commonPool();

    private volatile DisplayModel model;
    private final WidgetSelectionHandler selection = new WidgetSelectionHandler();

    private final ToolkitRepresentation<Group, Node> toolkit;

    private final UndoableActionManager undo = new UndoableActionManager();

    // TODO Eventually, in CSS, only keep 'toolbar', 'editor' and 'palette' in here.
    //      PropertyPanel and WidgetTree become individual views.
    private final BorderPane toolbar_center_status = new BorderPane();

    private final WidgetTree tree = new WidgetTree(selection); // TODO pass undo

    private final Group model_parent = new Group();
    private final Group edit_tools = new Group();
    // editor: model's representation in background, edit_tools on top
    private final Pane editor_pane = new Pane(model_parent, edit_tools);
    private final ScrollPane editor = new ScrollPane(editor_pane);

    private SelectionTracker selection_tracker;

    private final PropertyPanel property_panel = new PropertyPanel(selection, undo);


    public EditorGUI(final Stage stage)
    {
        toolkit = new JFXRepresentation(stage);
        createElements(stage);
        hookListeners();
    }

    private void createElements(final Stage stage)
    {
        selection_tracker = new SelectionTracker(toolkit, selection, undo);

        // BorderPane with
        //    toolbar
        //    center = tree | editor | palette | property_panel
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
        // Cause inside of 'editor' to fill it
        editor.setFitToWidth(true);
        editor.setFitToHeight(true);
        // editor_pane.getStyleClass().add("debug");
        center.getItems().addAll(tree.create(), editor, palette.create(), property_panel.create());
        center.setDividerPositions(0.2, 0.63, 0.75);

        final Label status = new Label("Status");

        toolbar_center_status.setTop(toolbar);
        toolbar_center_status.setCenter(center);
        toolbar_center_status.setBottom(status);
        BorderPane.setAlignment(center, Pos.TOP_LEFT);

        stage.setTitle("Editor");
        stage.setWidth(1200);
        stage.setHeight(600);
        final Scene scene = new Scene(toolbar_center_status, 1200, 600);
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
            public void handleClick(final Widget widget, final boolean with_control)
            {
                logger.log(Level.FINE, "Selected {0}",  widget);
                // Toggle selection of widget when Ctrl is held
                if (with_control)
                    selection.toggleSelection(widget);
                else
                    selection.setSelection(Arrays.asList(widget));
            }
        });

        editor.addEventHandler(MouseEvent.MOUSE_PRESSED, event ->
        {
            if (event.isControlDown())
                return;
            logger.log(Level.FINE, "Clicked in 'editor' De-select all widgets");
            selection.clear();
        });

        new Rubberband(editor_pane, this::selectWidgetsInRegion);

        WidgetTransfer.addDropSupport(editor_pane, this::handleDroppedModel);

        toolbar_center_status.setOnKeyPressed((KeyEvent event) ->
        {
            if (event.isControlDown())
                if (event.getCode() == KeyCode.Z)
                    undo.undoLast();
                else if (event.getCode() == KeyCode.Y)
                    undo.redoLast();
        });
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

                // TODO selection.set(model, [ selected widgets ])
                tree.setModel(model);
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

    private void selectWidgetsInRegion(final Rectangle2D region)
    {
        final List<Widget> found = GeometryTools.findWidgets(model, region);
        logger.log(Level.FINE, "Selected widgets in {0}: {1}",  new Object[] { region, found });
        selection.setSelection(found);
    }

    /** @param model Dropped model with widgets to be added to existing model */
    private void handleDroppedModel(final DisplayModel dropped_model)
    {
        final List<Widget> dropped = dropped_model.getChildren();
        for (Widget widget : dropped)
            undo.execute(new AddWidgetAction(model, widget));
        selection.setSelection(dropped);
    }

    public boolean handleClose()
    {
        toolkit.disposeRepresentation(model);
        return true;
    }
}
