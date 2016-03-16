/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.Objects;
import java.util.Optional;

import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.widgets.TabsWidget;
import org.csstudio.display.builder.representation.WidgetRepresentation;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;

import javafx.scene.Node;
import javafx.scene.Parent;

/** Base class for all JavaFX widget representations
 *  @param <JFX> JFX Widget
 *  @param <MW> Model widget
 *  @author Kay Kasemir
 */
abstract public class JFXBaseRepresentation<JFX extends Node, MW extends Widget> extends WidgetRepresentation<Parent, Node, MW>
{
    /** JFX node (or root of sub scene graph) that represents the widget
     *  <p>Only accessed on the JFX thread
     */
    protected JFX jfx_node;

    private volatile WidgetProperty<Boolean> visible;

    private final DirtyFlag dirty_position = new DirtyFlag();

    /** {@inheritDoc} */
    @Override
    public Parent createComponents(final Parent parent) throws Exception
    {
        jfx_node = createJFXNode();
        if (jfx_node != null)
        {   // Order JFX children same as model widgets within their container
            final int index;
            final Optional<Widget> container = model_widget.getParent();
            if (container.isPresent())
            {
                if (container.get() instanceof TabsWidget)
                {   // TODO Locate model_widget inside one of the Tab's children
                    index = -1;
                }
                else
                    index = container.get().getProperty(ChildrenProperty.DESCRIPTOR).getValue().indexOf(model_widget);
            }
            else
                index = -1;

            if (index < 0)
                JFXRepresentation.getChildren(parent).add(jfx_node);
            else
                JFXRepresentation.getChildren(parent).add(index, jfx_node);

            // Any visible item can be 'clicked' to allow editor to 'select' it
            jfx_node.setOnMousePressed((event) ->
            {
                event.consume();
                toolkit.fireClick(model_widget, event.isControlDown());
            });

            jfx_node.setOnContextMenuRequested((event) ->
            {
                event.consume();
                toolkit.fireContextMenu(model_widget);
            });
        }
        registerListeners();
        updateChanges();
        return getChildParent(parent);
    }

    // For what it's worth, in case the node eventually has a JFX contest menu:
    //
    // While functional on other platforms, a menu set via
    //    Control#setContextMenu(menu)
    // will not activate on Linux for a JFX scene inside FXCanvas/SWT.
    // Directly handling the context menu event works on all platforms,
    // plus allows attaching a menu to even a basic Node.
    //
    // jfx_node.setOnContextMenuRequested((event) ->
    // {
    //     event.consume();
    //     final ContextMenu menu = new ContextMenu();
    //     menu.getItems().add(new MenuItem("Demo"));
    //     menu.show(jfx_node, event.getScreenX(), event.getScreenY());
    // });

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
        JFXRepresentation.getChildren(jfx_node.getParent()).remove(jfx_node);
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
    protected Parent getChildParent(final Parent parent)
    {
        return parent;
    }

    /** Register model widget listeners.
     *
     *  <p>Override must call base class
     */
    protected void registerListeners()
    {
        visible = model_widget.checkProperty(CommonWidgetProperties.positionVisible).orElse(null);
        if (visible != null)
            visible.addUntypedPropertyListener(this::positionChanged);
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
            if (visible != null)
                jfx_node.setVisible(visible.getValue());
        }
    }
}
