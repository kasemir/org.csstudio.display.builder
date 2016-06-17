/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.tracker;

import static org.csstudio.display.builder.editor.DisplayEditor.logger;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.editor.undo.SetMacroizedWidgetPropertyAction;
import org.csstudio.display.builder.editor.undo.UpdateWidgetLocationAction;
import org.csstudio.display.builder.editor.util.GeometryTools;
import org.csstudio.display.builder.editor.util.ParentHandler;
import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.MacroizedWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.csstudio.javafx.Tracker;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/** Rubber-band-type tracker of currently selected widgets in UI.
 *
 *  <p>UI element that allows selecting widgets,
 *  moving and resizing them.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class SelectedWidgetUITracker extends Tracker
{
    private final ToolkitRepresentation<Parent, Node> toolkit;
    private final ParentHandler group_handler;
    private final UndoableActionManager undo;

    private final TrackerGridConstraint grid_constraint = new TrackerGridConstraint(10);
    private final TrackerSnapConstraint snap_constraint = new TrackerSnapConstraint(this);

    /** Inline editor for widget's PV name or text */
    private TextField inline_editor = null;

    /** Widgets to track */
    private List<Widget> widgets = Collections.emptyList();

    /** Break update loops JFX change -> model change -> JFX change -> ... */
    private boolean updating = false;

    /** Update tracker to match changed widget position */
    private final WidgetPropertyListener<Integer> position_listener = (p, o, n) -> updateTrackerFromWidgets();

    /** Construct a tracker.
     *
     *  <p>It remains invisible until it is asked to track widgets
     *  @param toolkit Toolkit
     *  @param group_handler Group handler
     *  @param selection Selection handler
     *  @param undo 'Undo' manager
     */
    public SelectedWidgetUITracker(final ToolkitRepresentation<Parent, Node> toolkit,
                            final ParentHandler group_handler,
                            final WidgetSelectionHandler selection,
                            final UndoableActionManager undo)
    {
        this.toolkit = toolkit;
        this.group_handler = group_handler;
        this.undo = undo;

        setVisible(false);

        // Track currently selected widgets
        selection.addListener(this::setSelectedWidgets);

        // Pass control-click down to underlying widgets
        addEventFilter(MouseEvent.MOUSE_PRESSED, event ->
        {
            if (event.isControlDown())
                passClickToWidgets(event);
        });

        // Allow 'dragging' selected widgets
        setOnDragDetected(event ->
        {
            if (! event.isControlDown())
                return;

            logger.log(Level.FINE, "Starting to drag {0}", widgets);
            final String xml;
            try
            {
                xml = ModelWriter.getXML(widgets);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot drag-serialize", ex);
                return;
            }
            final Dragboard db = startDragAndDrop(TransferMode.COPY);
            final ClipboardContent content = new ClipboardContent();
            content.putString(xml);
            db.setContent(content);

            // Would like to set tacker snapshot as drag image, but GTK error
            // as soon as the snapshot width is >= 128 pixel:
            // "GdkPixbuf-CRITICAL **: gdk_pixbuf_new_from_data: assertion `width > 0' failed"
            //
            //  final WritableImage snapshot = tracker.snapshot(null, null);
            //  System.out.println(snapshot.getWidth() + " x " + snapshot.getHeight());
            //  db.setDragView(snapshot);
            event.consume();
        });

        // When tracker moved, update widgets
        setListener(this::updateWidgetsFromTracker);
    }

    /** Apply enabled constraints to requested position
     *  @param x Requested X position
     *  @param y Requested Y position
     *  @return Constrained coordinate
     */
    @Override
    protected Point2D constrain(final double x, final double y)
    {
        Point2D result = super.constrain(x, y);
        if (grid_constraint.isEnabled())
            result = grid_constraint.constrain(result.getX(), result.getY());
        if (snap_constraint.isEnabled())
            result = snap_constraint.constrain(result.getX(), result.getY());
        return result;
    }

    /** @param event {@link MouseEvent} */
    @Override
    protected void mousePressed(final MouseEvent event)
    {
        if (event.getClickCount() == 1)
            super.mousePressed(event);
        else
        {
            event.consume();
            if (widgets.size() == 1  &&  inline_editor == null)
                createInlineEditor(widgets.get(0));
        }
    }

    /** @param event {@link MouseEvent} */
    @Override
    protected void mouseReleased(final MouseEvent event)
    {
        if (inline_editor == null)
            super.mouseReleased(event);
    }

    /** Is the inline editor active?
     *
     *  <p>The 'global' key handlers for copy/paste/delete
     *  must be suppressed when the inline editor is active,
     *  because otherwise trying to paste a PV name will
     *  paste a new widget, or deleting a part of a PV name
     *  will delete the currently selected widget.
     *
     *  <p>Since both RCP and JavaFX listen to the keys,
     *  the most practical solution was to have global actions
     *  check this flag
     */
    public boolean isInlineEditorActive()
    {
        return inline_editor != null;
    }

    /** Create an inline editor
     *
     *  <p>Depending on the widget's properties, it will edit
     *  the PV name or the text.
     *
     *  @param widget Widget on which to create an inline editor
     */
    private void createInlineEditor(final Widget widget)
    {
        // Check for an inline-editable property
        Optional<WidgetProperty<String>> check = widget.checkProperty(CommonWidgetProperties.behaviorPVName);
        if (! check.isPresent())
            check = widget.checkProperty(CommonWidgetProperties.displayText);
        if (! check.isPresent())
            return;

        // Create text field, aligned with widget, but assert minimum size
        final MacroizedWidgetProperty<String> property = (MacroizedWidgetProperty<String>)check.get();
        inline_editor = new TextField(property.getSpecification());
        inline_editor.setPromptText(property.getDescription()); // Not really shown since TextField will have focus
        inline_editor.setTooltip(new Tooltip(property.getDescription()));
        inline_editor.relocate(tracker.getX(), tracker.getY());
        inline_editor.resize(Math.max(100, tracker.getWidth()), Math.max(20, tracker.getHeight()));
        getChildren().add(inline_editor);

        // On enter, update the property. On Escape, just close
        inline_editor.setOnKeyPressed(event ->
        {
            switch (event.getCode())
            {
            case ENTER:
                undo.execute(new SetMacroizedWidgetPropertyAction(property, inline_editor.getText()));
                // Fall through, close editor
            case ESCAPE:
                event.consume();
                closeInlineEditor();
            default:
            }
        });
        // Close when focus lost
        inline_editor.focusedProperty().addListener((prop, old, focused) ->
        {
            if (! focused)
                closeInlineEditor();
        });

        inline_editor.selectAll();
        inline_editor.requestFocus();
    }

    private void closeInlineEditor()
    {
        getChildren().remove(inline_editor);
        inline_editor = null;
    }

    /** Tracker is in front of the widgets that it handles,
     *  so it receives all mouse clicks.
     *  When 'Control' key is down, that event should be passed
     *  down to the widgets under the tracker, but JFX blocks them.
     *  This method locates all widgets that contain the mouse coord.
     *  and fires a 'click' on them.
     *  @param event Mouse event that needs to be passed down
     */
    private void passClickToWidgets(final MouseEvent event)
    {
        for (Widget widget : widgets)
            if (GeometryTools.getDisplayBounds(widget).contains(event.getX(), event.getY()))
            {
                logger.log(Level.FINE, "Tracker passes click through to {0}", widget);
                toolkit.fireClick(widget, event.isControlDown());
            }
    }

    @Override
    public void setPosition(final double x, final double y, double width, double height)
    {
        super.setPosition(x, y, width, height);
        // As tracker is being moved, highlight group under tracker
        group_handler.locateParent(x, y, width, height);
    }

    /** Updates widgets to current tracker location and size */
    private void updateWidgetsFromTracker(final Rectangle2D original, final Rectangle2D current)
    {
        if (updating)
            return;
        updating = true;
        try
        {
            group_handler.hide();

            final List<Rectangle2D> orig_position =
                widgets.stream().map(GeometryTools::getBounds).collect(Collectors.toList());

            // If there was only one widget, the tracker bounds represent
            // the desired widget location and size.
            // But tracker bounds can apply to one or more widgets, so need to
            // determine the change in tracker bounds, apply those to each widget.
            final double dx = current.getMinX()   - original.getMinX();
            final double dy = current.getMinY()   - original.getMinY();
            final double dw = current.getWidth()  - original.getWidth();
            final double dh = current.getHeight() - original.getHeight();
            final int N = orig_position.size();
            for (int i=0; i<N; ++i)
            {
                final Widget widget = widgets.get(i);
                final Rectangle2D orig = orig_position.get(i);

                final ChildrenProperty orig_parent_children = ChildrenProperty.getParentsChildren(widget);
                ChildrenProperty parent_children = group_handler.getActiveParentChildren();
                if (parent_children == null)
                    parent_children = widget.getDisplayModel().runtimeChildren();

                if (orig_parent_children == parent_children)
                {   // Slightly faster since parent stays the same
                    widget.positionX().setValue((int) (orig.getMinX() + dx));
                    widget.positionY().setValue((int) (orig.getMinY() + dy));
                }
                else
                {   // Update to new parent
                    final Point2D old_offset = GeometryTools.getDisplayOffset(widget);
                    orig_parent_children.removeChild(widget);
                    parent_children.addChild(widget);
                    final Point2D new_offset = GeometryTools.getDisplayOffset(widget);

                    logger.log(Level.FINE, "{0} moves from {1} ({2}) to {3} ({4})",
                               new Object[] { widget, orig_parent_children.getWidget(), old_offset,
                                                      parent_children.getWidget(), new_offset});
                    // Account for old and new display offset
                    widget.positionX().setValue((int) (orig.getMinX() + dx + old_offset.getX() - new_offset.getX()));
                    widget.positionY().setValue((int) (orig.getMinY() + dy + old_offset.getY() - new_offset.getY()));
                }
                widget.positionWidth().setValue((int) Math.max(1, orig.getWidth() + dw));
                widget.positionHeight().setValue((int) Math.max(1, orig.getHeight() + dh));

                undo.add(new UpdateWidgetLocationAction(widget,
                                                        orig_parent_children,
                                                        parent_children,
                                                        (int) orig.getMinX(),  (int) orig.getMinY(),
                                                        (int) orig.getWidth(), (int) orig.getHeight()));
            }
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "Failed to move/resize widgets", ex);
        }
        finally
        {
            updating = false;
        }
    }

    private void updateTrackerFromWidgets()
    {
        if (updating)
            return;
        final Rectangle2D rect = widgets.stream()
                                        .map(GeometryTools::getDisplayBounds)
                                        .reduce(null, GeometryTools::join);
        updating = true;
        setPosition(rect);
        updating = false;
    }

    /** @param enable Enable grid? */
    public void enableGrid(final boolean enable)
    {
        grid_constraint.setEnabled(enable);
    }

    /** @param enable Enable snap? */
    public void enableSnap(final boolean enable)
    {
        snap_constraint.setEnabled(enable);
    }

    /** Activate the tracker
     *  @param widgets Widgets to control by tracker,
     *                 empty to de-select
     */
    public void setSelectedWidgets(final List<Widget> widgets)
    {
        unbindFromWidgets();

        this.widgets = Objects.requireNonNull(widgets);
        if (widgets.size() <= 0)
        {
            setVisible(false);
            return;
        }

        try
        {
            snap_constraint.configure(widgets.get(0).getDisplayModel(), widgets);
        }
        catch (final Exception ex)
        {
            logger.log(Level.WARNING, "Cannot obtain widget model", ex);
        }

        setVisible(true);

        updateTrackerFromWidgets();

        startDrag(null); // TODO Why?

        bindToWidgets();

        // Get focus to allow use of arrow keys
        tracker.requestFocus();
    }

    private void bindToWidgets()
    {
        for (final Widget widget : widgets)
        {
            widget.positionX().addPropertyListener(position_listener);
            widget.positionY().addPropertyListener(position_listener);
            widget.positionWidth().addPropertyListener(position_listener);
            widget.positionHeight().addPropertyListener(position_listener);
        }
    }

    private void unbindFromWidgets()
    {
        for (final Widget widget : widgets)
        {
            widget.positionX().removePropertyListener(position_listener);
            widget.positionY().removePropertyListener(position_listener);
            widget.positionWidth().removePropertyListener(position_listener);
            widget.positionHeight().removePropertyListener(position_listener);
        }
    }
}
