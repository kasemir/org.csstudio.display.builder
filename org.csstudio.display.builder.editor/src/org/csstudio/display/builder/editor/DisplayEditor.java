/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.palette.Palette;
import org.csstudio.display.builder.editor.poly.PointsBinding;
import org.csstudio.display.builder.editor.tracker.SelectedWidgetUITracker;
import org.csstudio.display.builder.editor.undo.AddWidgetAction;
import org.csstudio.display.builder.editor.util.GeometryTools;
import org.csstudio.display.builder.editor.util.ParentHandler;
import org.csstudio.display.builder.editor.util.JFXGeometryTools;
import org.csstudio.display.builder.editor.util.Rubberband;
import org.csstudio.display.builder.editor.util.WidgetNaming;
import org.csstudio.display.builder.editor.util.WidgetTransfer;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.representation.ToolkitListener;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.display.builder.util.undo.UndoableActionManager;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;

/** Display editor UI
 *
 *  <p>Shows DisplayModel, has Palette to add widgets.
 *  Allows interactive move/resize.
 *
 *  <pre>
 *  root
 *   |
 *   +----------------------+
 *   |                      |
 *  scroll                palette
 *   |
 *  editor_pane
 *   |
 *   +----------------------+
 *   |                      |
 *  model_parent          edit_tools
 *  (model rep. in back)  (on top)
 *  </pre>
 *
 *  <p>model_parent hosts representation of model widgets
 *
 *  <p>edit_tools holds GroupHandler, SelectionTracker
 *
 *  <p>editor_pane automatically resizes to hold all widget representations.
 *  Shows 'rubberband'
 *
 *  <p>scroll is ScrollPane, drop target for new widgets, starts 'rubberband'.
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
 *  The Rubberband, however, cannot add itself as a child to the scroll, so
 *  it uses the edit_tools for that.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DisplayEditor
{
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final JFXRepresentation toolkit;

    private DisplayModel model;

    private final WidgetNaming widget_naming = new WidgetNaming();


    private final UndoableActionManager undo = new UndoableActionManager();

    private final WidgetSelectionHandler selection = new WidgetSelectionHandler();

    private final ParentHandler group_handler;

    private final SelectedWidgetUITracker selection_tracker;

    private SplitPane root;
    private ScrollPane scroll;
    private final Group model_parent = new Group();
    private final Group edit_tools = new Group();
    private final Pane editor_pane = new Pane(model_parent, edit_tools);

    /** @param toolkit JFX Toolkit */
    public DisplayEditor(final JFXRepresentation toolkit)
    {
        this.toolkit = toolkit;
        group_handler = new ParentHandler(edit_tools, selection);

        selection_tracker = new SelectedWidgetUITracker(toolkit, group_handler, selection, undo);
        selection_tracker.enableSnap(true);
        selection_tracker.enableGrid(true);
    }

    /** Create UI elements
     *  @return Root Node
     */
    public Parent create()
    {
        scroll = new ScrollPane(editor_pane);

        final Palette palette = new Palette(selection);
        final Node palette_node = palette.create();

        root = new SplitPane();
        root.getItems().addAll(scroll, palette_node);
        SplitPane.setResizableWithParent(palette_node, false);

        edit_tools.getChildren().addAll(selection_tracker);

        hookListeners();

        return root;
    }

    /** @return Selection tracker */
    public SelectedWidgetUITracker getSelectedWidgetUITracker()
    {
        return selection_tracker;
    }

    /** @return Selection tracker */
    public WidgetSelectionHandler getWidgetSelectionHandler()
    {
        return selection;
    }

    /** @return Undo manager */
    public UndoableActionManager getUndoableActionManager()
    {
        return undo;
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

        scroll.setOnMousePressed(event ->
        {
            if (event.isControlDown())
                return;
            logger.log(Level.FINE, "Mouse pressed in 'editor', de-select all widgets");
            event.consume();
            selection.clear();
        });

        new Rubberband(scroll, edit_tools, this::selectWidgetsInRegion);

        new PointsBinding(edit_tools, selection, undo);

        WidgetTransfer.addDropSupport(scroll, group_handler, this::handleDroppedModel);
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
        Widget container = group_handler.getActiveParent();
        if (container == null)
            container = model;
        // Correct all dropped widget locations relative to container
        final Point2D offset = GeometryTools.getContainerOffset(container);
        // Also account for scroll pane
        final Point2D origin = JFXGeometryTools.getContentOrigin(scroll);
        final int dx = (int) (offset.getX() - origin.getX());
        final int dy = (int) (offset.getY() - origin.getY());

        // Add dropped widgets
        try
        {
            final List<Widget> dropped = dropped_model.getChildren();
            for (Widget widget : dropped)
            {
                widget.positionX().setValue(widget.positionX().getValue() - dx);
                widget.positionY().setValue(widget.positionY().getValue() - dy);
                widget_naming.setDefaultName(container.getDisplayModel(), widget);
                undo.execute(new AddWidgetAction(container, widget));
            }
            selection.setSelection(dropped);
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "Cannot add widgets", ex);
        }
    }

    /** Set Model
     * @param model Model to show and edit
     */
    public void setModel(final DisplayModel model)
    {
        // Model in editor should have input file information
        // to allow resolving images etc. relative to that file
        if (model.getUserData(DisplayModel.USER_DATA_INPUT_FILE) == null)
            logger.log(Level.SEVERE, "Model lacks input file information");

        widget_naming.clear();
        selection.clear();
        group_handler.setModel(model);

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
    }

    /** @return Currently edited model */
    public DisplayModel getModel()
    {
        return model;
    }

    /** Print debug info */
    public void debug()
    {
        System.out.println("JavaFX Nodes for Model's Representation");
        final int nodes = countAndDumpNodes(model_parent, 1);
        System.out.println("Node Count: " + nodes);
    }

    /** Recursively dump nodes
     *  @param parent {@link Parent}
     *  @param level Indentation level
     *  @return Number of nodes and sub-nodes
     */
    private int countAndDumpNodes(final Parent parent, final int level)
    {
        int count = 0;
        for (Node node : parent.getChildrenUnmodifiable())
        {
            ++count;
            for (int i=0; i<level; ++i)
                System.out.print("  ");
            System.out.println(node.getClass().getSimpleName());
            if (node instanceof Parent)
                count += countAndDumpNodes((Parent) node, level + 1);
        }
        return count;
    }

    public void dispose()
    {
        if (model != null)
            toolkit.disposeRepresentation(model);
    }
}
