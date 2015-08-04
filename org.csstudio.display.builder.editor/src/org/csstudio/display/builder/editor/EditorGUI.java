/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.actions.ActionGUIHelper;
import org.csstudio.display.builder.editor.actions.EnableGridAction;
import org.csstudio.display.builder.editor.actions.EnableSnapAction;
import org.csstudio.display.builder.editor.actions.SaveModelAction;
import org.csstudio.display.builder.editor.palette.Palette;
import org.csstudio.display.builder.editor.properties.PropertyPanel;
import org.csstudio.display.builder.editor.tracker.SelectionTracker;
import org.csstudio.display.builder.editor.tree.WidgetTree;
import org.csstudio.display.builder.editor.undo.AddWidgetAction;
import org.csstudio.display.builder.editor.undo.UndoableActionManager;
import org.csstudio.display.builder.editor.util.GeometryTools;
import org.csstudio.display.builder.editor.util.GroupHandler;
import org.csstudio.display.builder.editor.util.Rubberband;
import org.csstudio.display.builder.editor.util.WidgetTransfer;
import org.csstudio.display.builder.model.ContainerWidget;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.representation.ToolkitListener;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;

import javafx.geometry.Point2D;
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
 *  <p>Layers from 'top' down in the 'editor':
 *  <pre>
 *      +-- edit_tools   : SelectionTracker
 *      +-- model_parent : Hosts representation of model widgets
 *  +-- editor_pane      : Automatically resizes to hold all widget representations.
 *  |                      Shows 'rubberband'.
 *  editor               : ScrollPane that shows the 'editor_pane'.
 *                         Drop target for new widgets.
 *                         Starts 'rubberband'.
 *  </pre>
 *
 *  <p>The editor_pane is initially empty.
 *  As widget representations are added in the model_parent,
 *  the editor_pane grows.
 *  The scroll bars of the editor automatically enable
 *  as the content of the editor_pane grows beyond the editor.
 *
 *  <p>The Rubberband hooks into editor mouse events to allow starting
 *  a rubberband anywhere in the visible region. Connecting the Rubberband
 *  to the editor_pane would limit selections to the region that bounds the
 *  visible widgets, one could not rubberband starting from 'below' the bottommost widget.
 *  The Rubberband, however, cannot add itself as a child to the editor, so
 *  it uses the edit_tools for that.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EditorGUI
{
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Executor executor = ForkJoinPool.commonPool();

    private volatile DisplayModel model = new DisplayModel();
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

    private GroupHandler group_handler;
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
        group_handler = new GroupHandler(edit_tools, selection);
        selection_tracker = new SelectionTracker(toolkit, group_handler, selection, undo);

        // BorderPane with
        //    toolbar
        //    center = tree | editor | palette | property_panel
        //    status
        final ToolBar toolbar = new ToolBar(
                ActionGUIHelper.createButton(new SaveModelAction(this::doSaveAs)),
                new Separator(),
                ActionGUIHelper.createToggleButton(new EnableGridAction(selection_tracker)),
                ActionGUIHelper.createToggleButton(new EnableSnapAction(selection_tracker)),
                new Separator(),
                new Button("Something"));

        final Palette palette = new Palette();

        final SplitPane center = new SplitPane();
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

        new Rubberband(editor, edit_tools, this::selectWidgetsInRegion);

        WidgetTransfer.addDropSupport(editor, group_handler, this::handleDroppedModel);

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

                tree.setModel(model);
                group_handler.setModel(model);

                // Representation needs to be created in UI thread
                toolkit.execute(() -> setModel(model));
            }
            catch (final Exception ex)
            {
                logger.log(Level.SEVERE, "Cannot start", ex);
            }
        });
    }

    private void doSaveAs(final File file)
    {
        executor.execute(() ->
        {
            logger.log(Level.FINE, "Save as {0}", file);
            try
            (
                final ModelWriter writer = new ModelWriter(new FileOutputStream(file));
            )
            {
                writer.writeModel(model);
            }
            catch (Exception ex)
            {
                logger.log(Level.SEVERE, "Cannot save as " + file, ex);
            }
        });
    }

    private void setModel(final DisplayModel model)
    {
        selection.clear();
        this.model = Objects.requireNonNull(model);
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
        // Dropped into a sub-group or the main display?
        ContainerWidget container = group_handler.getActiveGroup();
        if (container == null)
            container = model;
        // Correct all dropped widget locations relative to container
        final Point2D offset = GeometryTools.getContainerOffset(container);
        final int dx = (int)offset.getX();
        final int dy = (int)offset.getY();

        // Add dropped widgets
        final List<Widget> dropped = dropped_model.getChildren();
        for (Widget widget : dropped)
        {
            widget.positionX().setValue(widget.positionX().getValue() - dx);
            widget.positionY().setValue(widget.positionY().getValue() - dy);
            undo.execute(new AddWidgetAction(container, widget));
        }
        selection.setSelection(dropped);
    }

    public boolean handleClose()
    {
        toolkit.disposeRepresentation(model);
        return true;
    }
}
