/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;

import java.util.Objects;

import org.csstudio.display.builder.model.BaseWidget;
import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.representation.WidgetRepresentation;
import org.csstudio.display.builder.representation.javafx.actions.CopyPVNameToClipboard;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/** Base class for all JavaFX widget representations
 *  @param <JFX> JFX Widget
 *  @param <MW> Model widget
 *  @author Kay Kasemir
 */
abstract public class JFXBaseRepresentation<JFX extends Node, MW extends BaseWidget> extends WidgetRepresentation<Group, Node, MW>
{
    protected JFX jfx_node;

    private final DirtyFlag dirty_position = new DirtyFlag();

    /** {@inheritDoc} */
    @Override
    public Group createComponents(final Group parent) throws Exception
    {
        jfx_node = createJFXNode();
        if (jfx_node != null)
        {
            parent.getChildren().add(jfx_node);
            // Any visible item can be 'clicked' to allow editor to 'select' it
            jfx_node.setOnMousePressed((event) ->
            {
                toolkit.fireClick(model_widget, event.isControlDown());
                event.consume();
            });
        }
        createContextMenu();
        registerListeners();
        updateChanges();
        return getChildParent(parent);
    }

    /** Create context menu */
    private void createContextMenu()
    {
        // TODO Dummy context menu
        // Need to get fixed entries set by either runtime or editor,
        // then PV-based contributions
        final ContextMenu menu = new ContextMenu();
        final ObservableList<MenuItem> items = menu.getItems();
        if (model_widget.hasProperty(behaviorPVName)  &&
            ! model_widget.getProperty(behaviorPVName).getValue().isEmpty())
            items.add(new CopyPVNameToClipboard(model_widget));

        if (items.isEmpty())
            return;

        // While functional on other platforms, a menu set via
        //    Control#setContextMenu(menu)
        // will not activate on Linux for a JFX scene inside FXCanvas/SWT.
        // Directly handling the context menu event works on all platforms,
        // plus allows attaching a menu to even a basic Node.
        jfx_node.setOnContextMenuRequested((event) ->
        {
            event.consume();
            menu.show(jfx_node, event.getScreenX(), event.getScreenY());
        });

        // TODO Still fall back to Control#setContextMenu(menu) when not on Linux?

        // TODO Handle clash of this context menu with the built-in copy/paste context
        //      menu of text field widgets
    }

    /** Implementation needs to create the JavaFX node
     *  or node tree for the model widget.
     *
     *  @return (Primary) JavaFX node
     *  @throws Exception on error
     */
    abstract protected JFX createJFXNode() throws Exception;

    /** {@inheritDoc} */
    @Override
    public void dispose()
    {
        Objects.requireNonNull(jfx_node);
        final Group group = (Group) jfx_node.getParent();
        Objects.requireNonNull(group);
        group.getChildren().remove(jfx_node);
    }

    /** Get parent that would be used for child-widgets.
     *
     *  <p>By default, the representation does not itself host
     *  child widgets, so the parent of this widget is used.
     *
     *  <p>Specific implementation can override to return an
     *  inner container which holds child widgets.
     *
     *  @param parent parent of this JavaFX representation
     *  @return Desired parent for child nodes
     */
    protected Group getChildParent(final Group parent)
    {
        return parent;
    }

    /** Register model widget listeners.
     *
     *  <p>Override must call base class
     */
    protected void registerListeners()
    {
        model_widget.positionVisible().addUntypedPropertyListener(this::positionChanged);
        model_widget.positionX().addUntypedPropertyListener(this::positionChanged);
        model_widget.positionY().addUntypedPropertyListener(this::positionChanged);
        // Would like to also listen to positionWidth & height,
        // then call jfx_node.resizeRelocate(x, y, width, height),
        // but resizeRelocate tends to ignore the width & height on
        // several widgets (Rectangle), so have to call their
        // setWith() & setHeight() in specific representation.
    }

    private void positionChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_position.mark();
        toolkit.scheduleUpdate(this);
    }

    /** {@inheritDoc} */
    @Override
    public void updateChanges()
    {
        if (dirty_position.checkAndClear())
        {
            jfx_node.relocate(model_widget.positionX().getValue(),
                              model_widget.positionY().getValue());
            jfx_node.setVisible(model_widget.positionVisible().getValue());
        }
    }
}
