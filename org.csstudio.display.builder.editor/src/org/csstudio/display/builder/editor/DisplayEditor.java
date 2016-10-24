/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.palette.Palette;
import org.csstudio.display.builder.editor.poly.PointsBinding;
import org.csstudio.display.builder.editor.tracker.SelectedWidgetUITracker;
import org.csstudio.display.builder.editor.undo.AddWidgetAction;
import org.csstudio.display.builder.editor.undo.RemoveWidgetsAction;
import org.csstudio.display.builder.editor.util.GeometryTools;
import org.csstudio.display.builder.editor.util.JFXGeometryTools;
import org.csstudio.display.builder.editor.util.ParentHandler;
import org.csstudio.display.builder.editor.util.Rubberband;
import org.csstudio.display.builder.editor.util.WidgetNaming;
import org.csstudio.display.builder.editor.util.WidgetTransfer;
import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.widgets.ArrayWidget;
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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;

/** Display editor UI
 *
 *  <p>Shows DisplayModel, has Palette to add widgets.
 *  Allows interactive move/resize.
 *
 *  <p>Extends the basic JFXRepresentation scene layout:
 *  <pre>
 *  root (SplitPane)
 *   |
 *   +----------------------+
 *   |                      |
 *  model_root (Scroll)    palette
 *   |
 *  scroll_body (Pane)
 *   |
 *   +----------------------------+
 *   |                            |
 *  model_parent (Group)      edit_tools
 *  (model rep. in back)      (on top)
 *   |                            |
 *  widget representations    selection tracker, points, rubberband
 *  </pre>
 *
 *  <p>model_parent hosts representations of model widgets
 *
 *  <p>edit_tools holds GroupHandler, SelectionTracker
 *
 *  <p>scroll_body automatically resizes to hold all widget representations.
 *  Shows 'rubberband'
 *
 *  <p>model_root is ScrollPane, drop target for new widgets, starts 'rubberband'.
 *
 *  <p>The scroll_body is initially empty.
 *  As widget representations are added in the model_parent,
 *  the scroll_body grows.
 *  The scroll bars of the editor automatically enable
 *  as the content of the scroll_body grows beyond the editor.
 *
 *  <p>The Rubberband hooks into editor mouse events to allow starting
 *  a rubberband anywhere in the visible region. Connecting the Rubberband
 *  to the scroll_body would limit selections to the region that bounds the
 *  visible widgets, one could not rubberband starting from 'below' the bottommost widget.
 *  The Rubberband, however, cannot add itself as a child to the scroll, so
 *  it uses the edit_tools for that.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DisplayEditor
{
    /** Suggested logger for the editor */
    public final static Logger logger = Logger.getLogger(DisplayEditor.class.getName());

    private final JFXRepresentation toolkit;

    private DisplayModel model;

    private final WidgetNaming widget_naming = new WidgetNaming();

    private final UndoableActionManager undo;

    private final WidgetSelectionHandler selection = new WidgetSelectionHandler();

    private final ParentHandler group_handler;

    private final SelectedWidgetUITracker selection_tracker;

    private SplitPane root;
    private ScrollPane model_root;
    private Group model_parent;
    private final Group edit_tools = new Group();

    /** @param toolkit JFX Toolkit
     *  @param stack_size Number of undo/redo entries
     */
    public DisplayEditor(final JFXRepresentation toolkit, final int stack_size)
    {
        this.toolkit = toolkit;
        undo = new UndoableActionManager(stack_size);

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
        model_root = toolkit.createModelRoot();
        final Pane scroll_body = (Pane) model_root.getContent();
        model_parent = (Group) scroll_body.getChildren().get(0);
        scroll_body.getChildren().add(edit_tools);

        final Palette palette = new Palette(selection);
        final Node palette_node = palette.create();

        root = new SplitPane();
        root.getItems().addAll(model_root, palette_node);
        root.setDividerPositions(1);

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

    private void hookListeners ( ) {

        toolkit.addListener(new ToolkitListener() {
            @Override
            public void handleClick ( final Widget widget, final boolean with_control ) {
                logger.log(Level.FINE, "Selected {0}", widget);
                // Toggle selection of widget when Ctrl is held
                if ( with_control )
                    selection.toggleSelection(widget);
                else
                    selection.setSelection(Arrays.asList(widget));
            }
        });

        model_root.setOnMousePressed(event -> {
            if ( event.isControlDown() )
                return;
            logger.log(Level.FINE, "Mouse pressed in 'editor', de-select all widgets");
            event.consume();
            selection.clear();
        });

        new Rubberband(model_root, edit_tools, this::selectWidgetsInRegion);
        new PointsBinding(edit_tools, selection, undo);

        WidgetTransfer.addDropSupport(model_root, group_handler, selection_tracker, this::addWidgets);

    }

    private void selectWidgetsInRegion(final Rectangle2D region)
    {
        final List<Widget> found = GeometryTools.findWidgets(model, region);
        logger.log(Level.FINE, "Selected widgets in {0}: {1}",  new Object[] { region, found });
        selection.setSelection(found);
    }

    /** @param widgets Widgets to be added to existing model */
    private void addWidgets(final List<Widget> widgets)
    {
        // Dropped into a sub-group or the main display?
        ChildrenProperty target = group_handler.getActiveParentChildren();
        if (target == null)
            target = model.runtimeChildren();
        Widget container = target.getWidget();
        // Correct all dropped widget locations relative to container
        Point2D offset = GeometryTools.getContainerOffset(container);
        // Also account for scroll pane
        Point2D origin = JFXGeometryTools.getContentOrigin(model_root);
        int dx = (int) (offset.getX() - origin.getX());
        int dy = (int) (offset.getY() - origin.getY());

        // Add dropped widgets
        try
        {
            final ListIterator<Widget> it = widgets.listIterator();
            if (container instanceof ArrayWidget)
            {
                if (target.getValue().isEmpty())
                { //drop first widget into ArrayWidget
                    Widget widget = it.next();
                    widget.propX().setValue(widget.propX().getValue() - dx);
                    widget.propY().setValue(widget.propY().getValue() - dy);
                    widget_naming.setDefaultName(container.getDisplayModel(), widget);
                    undo.execute(new AddWidgetAction(target, widget));
                }

                //hide highlight, since not adding to ArrayWidget container
                if (it.hasNext())
                    group_handler.hide();

                //re-assign target, container, etc. to use ArrayWidget's parent
                target = ChildrenProperty.getParentsChildren(container);
                container = target.getWidget();
                offset = GeometryTools.getContainerOffset(container);
                origin = JFXGeometryTools.getContentOrigin(model_root);
                dx = (int) (offset.getX() - origin.getX());
                dy = (int) (offset.getY() - origin.getY());
            }
            while (it.hasNext())
            {
                Widget widget = it.next();
                widget.propX().setValue(widget.propX().getValue() - dx);
                widget.propY().setValue(widget.propY().getValue() - dy);
                widget_naming.setDefaultName(container.getDisplayModel(), widget);
                undo.execute(new AddWidgetAction(target, widget));
            }
            selection.setSelection(widgets);
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

        undo.clear();
        widget_naming.clear();
        selection.clear();
        group_handler.setModel(model);
        selection_tracker.setModel(model);

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

    /** Copy currently selected widgets to clipboard
     *  @return Widgets that were copied or <code>null</code>
     */
    public List<Widget> copyToClipboard()
    {
        if (selection_tracker.isInlineEditorActive())
            return null;

        final List<Widget> widgets = selection.getSelection();
        if (widgets.isEmpty())
            return null;

        final String xml;
        try
        {
            xml = ModelWriter.getXML(widgets);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot create content for clipboard", ex);
            return null;
        }

        final ClipboardContent content = new ClipboardContent();
        content.putString(xml);
        Clipboard.getSystemClipboard().setContent(content);
        return widgets;
    }

    /** Cut (delete) selected widgets, placing them on the clipboard */
    public void cutToClipboard()
    {
        if (selection_tracker.isInlineEditorActive())
            return;

        // Strictly speaking, delete would not copy to the clipboard...
        final List<Widget> widgets = copyToClipboard();
        if (widgets == null)
            return;
        undo.execute(new RemoveWidgetsAction(widgets));
        selection_tracker.setSelectedWidgets(Collections.emptyList());
    }

    /** Paste widgets from clipboard
     *  @param x Desired coordinate of upper-left widget ..
     *  @param y .. when pasted
     */
    public void pasteFromClipboard(final int x, final int y)
    {
        if (selection_tracker.isInlineEditorActive())
            return;

        final String xml = Clipboard.getSystemClipboard().getString();
        // Anything on clipboard?
        if (xml == null)
            return;
        // Does it look like widget XML?
        if (! (xml.startsWith("<?xml")  &&
               xml.contains("<display")))
            return;
        try
        {
            final DisplayModel model = ModelReader.parseXML(xml);
            final List<Widget> widgets = model.getChildren();
            logger.log(Level.FINE, "Pasted {0} widgets", widgets.size());
            GeometryTools.moveWidgets(x, y, widgets);
            addWidgets(widgets);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Failed to paste content of clipboard", ex);
        }
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
