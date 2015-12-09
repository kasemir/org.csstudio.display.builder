/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.EditorUtil;
import org.csstudio.display.builder.editor.WidgetSelectionHandler;
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
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.representation.ToolkitListener;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import javafx.embed.swt.FXCanvas;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/** RCP 'Editor' for the display builder
 *  @author Kay Kasemir
 */
public class DisplayEditorPart extends EditorPart
{
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final JFXRepresentation toolkit = new JFXRepresentation();

    private final UndoableActionManager undo = new UndoableActionManager();

    private final WidgetSelectionHandler selection = new WidgetSelectionHandler();

    private GroupHandler group_handler;

    private SelectionTracker selection_tracker;

    // FXCanvas Scene
    //   |
    // center
    //   |
    //   +----------------------+
    //   |                      |
    // scroll                palette
    //   |
    // editor_pane
    //   |
    //   +----------------------+
    //   |                      |
    // model_parent          edit_tools
    // (model rep. in back)  (on top)
    //
    //
    private FXCanvas fx_canvas;
    private SplitPane center;
    private ScrollPane scroll;
    private final Group model_parent = new Group();
    private final Group edit_tools = new Group();
    private final Pane editor_pane = new Pane(model_parent, edit_tools);

    private DisplayModel model;

    private final WidgetNaming widget_naming = new WidgetNaming();



    public DisplayEditorPart()
    {
        // TODO Properties
        // TODO Outline
    }

    @Override
    public void init(final IEditorSite site, final IEditorInput input)
            throws PartInitException
    {
        setSite(site);
        // TODO setInput(new NoResourceEditorInput(input));
        setInput(input);
    }

    @Override
    public void createPartControl(final Composite parent)
    {
        createElements(parent);
        createToolbar();
        hookListeners();
    }

    private void createElements(final Composite parent)
    {
        parent.setLayout(new FillLayout());

        // Must first create FXCanvas, because that initializes JFX.
        // When creating FXCanvas later, there will be JFX errors
        // like "Not on FX application thread", "Toolkit not initialized"
        fx_canvas = new FXCanvas(parent, SWT.NONE);

        scroll = new ScrollPane(editor_pane);
        group_handler = new GroupHandler(edit_tools, selection);
        selection_tracker = new SelectionTracker(toolkit, group_handler, selection, undo);


        final Palette palette = new Palette(selection);
        final Node palette_node = palette.create();

        center = new SplitPane();
        center.getItems().addAll(scroll, palette_node);
        SplitPane.setResizableWithParent(palette_node, false);

        final Scene scene = new Scene(center);
        EditorUtil.setSceneStyle(scene);

        fx_canvas.setScene(scene);

        edit_tools.getChildren().add(selection_tracker);

        final IEditorInput input = getEditorInput();
        final IFile file = input.getAdapter(IFile.class);
        if (file != null)
            EditorUtil.getExecutor().execute(() -> loadModel(file));
    }

    private void createToolbar()
    {
        // TODO Toolbar..
        final IToolBarManager toolbar = getEditorSite().getActionBars().getToolBarManager();
        toolbar.add(new Action("Test")
        {
        });
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

        center.setOnKeyPressed((KeyEvent event) ->
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

    @Override
    public void setFocus()
    {
        fx_canvas.setFocus();
    }

    private void loadModel(final IFile file)
    {
        try
        {
            final ModelReader reader = new ModelReader(file.getContents());
            final DisplayModel model = reader.readModel();
            setModel(model);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot load " + file, ex);
        }
    }

    private void setModel(final DisplayModel model)
    {
        toolkit.execute(() ->
        {
            setPartName(model.getName());

            widget_naming.clear();
            selection.clear();
            // TODO
            //            tree.setModel(model);
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
        });
    }

    @Override
    public boolean isDirty()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSaveAsAllowed()
    {
        return true;
    }

    @Override
    public void doSave(final IProgressMonitor monitor)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void doSaveAs()
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void dispose()
    {
        // TODO Auto-generated method stub
        super.dispose();
    }
}
