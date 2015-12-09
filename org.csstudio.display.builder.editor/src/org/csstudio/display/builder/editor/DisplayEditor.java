/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
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
import org.csstudio.display.builder.editor.tracker.SelectionTracker;
import org.csstudio.display.builder.editor.undo.AddWidgetAction;
import org.csstudio.display.builder.editor.util.GeometryTools;
import org.csstudio.display.builder.editor.util.GroupHandler;
import org.csstudio.display.builder.editor.util.Rubberband;
import org.csstudio.display.builder.editor.util.WidgetNaming;
import org.csstudio.display.builder.editor.util.WidgetTransfer;
import org.csstudio.display.builder.model.ContainerWidget;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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

    private GroupHandler group_handler;

    private SelectionTracker selection_tracker;

    private SplitPane root;
    private ScrollPane scroll;
    private final Group model_parent = new Group();
    private final Group edit_tools = new Group();
    private final Pane editor_pane = new Pane(model_parent, edit_tools);

    public DisplayEditor(final JFXRepresentation toolkit)
    {
        this.toolkit = toolkit;
    }

    /** Create UI elements
     *  @return Root Node
     */
    public Parent create()
    {
        group_handler = new GroupHandler(edit_tools, selection);

        selection_tracker = new SelectionTracker(toolkit, group_handler, selection, undo);
        selection_tracker.enableSnap(true);
        selection_tracker.enableGrid(true);

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

        scroll.addEventHandler(MouseEvent.MOUSE_PRESSED, event ->
        {
            if (event.isControlDown())
                return;
            logger.log(Level.FINE, "Clicked in 'editor' De-select all widgets");
            selection.clear();
        });

        new Rubberband(scroll, edit_tools, this::selectWidgetsInRegion);

        WidgetTransfer.addDropSupport(scroll, group_handler, this::handleDroppedModel);

        root.setOnKeyPressed((KeyEvent event) ->
        {
            if (event.isControlDown())
                if (event.getCode() == KeyCode.Z)
                    undo.undoLast();
                else if (event.getCode() == KeyCode.Y)
                    undo.redoLast();
        });
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

    public void dispose()
    {
        toolkit.disposeRepresentation(model);
    }
}
