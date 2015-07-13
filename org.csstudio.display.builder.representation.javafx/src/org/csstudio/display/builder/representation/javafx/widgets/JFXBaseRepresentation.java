/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.beans.PropertyChangeEvent;

import org.csstudio.display.builder.model.BaseWidget;
import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.WidgetRepresentation;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/** Base class for all JavaFX widget representations
 *  @param <JFX> JFX Widget
 *  @param <MW> Model widget
 *  @author Kay Kasemir
 */
abstract public class JFXBaseRepresentation<JFX extends Node, MW extends BaseWidget> extends WidgetRepresentation<Group, Node, MW>
{
    protected JFX jfx_node;

    private final DirtyFlag dirty_position = new DirtyFlag();

    /** Construct representation for a model widget
     *  @param toolkit Toolkit helper
     *  @param model_widget Model widget
     */
    public JFXBaseRepresentation(final ToolkitRepresentation<Group, Node> toolkit,
                             final MW model_widget)
    {
        super(toolkit, model_widget);
    }

    /** {@inheritDoc} */
    @Override
    public Group init(final Group parent) throws Exception
    {
        jfx_node = createJFXNode();
        if (jfx_node != null)
        {
            parent.getChildren().add(jfx_node);

            // TODO Fix Ctrl-click to de-select
            // Initial click on widget is reported and widget is selected in editor.
            // Follow-up clicks appear to be captured by tracker,
            // even though it doesn't consume() the Ctrl-click
            jfx_node.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) ->
//            jfx_node.setOnMousePressed((event) ->
            {
                System.out.println("Mouse pressed in " + model_widget);
                toolkit.fireClick(model_widget, event.isControlDown());
                event.consume();
            });
        }
        registerListeners();
        updateChanges();
        return getChildParent(parent);
    }

    /** Implementation needs to create the JavaFX node
     *  or node tree for the model widget.
     *
     *  @return (Primary) JavaFX node
     *  @throws Exception on error
     */
    abstract protected JFX createJFXNode() throws Exception;

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
        model_widget.positionVisible().addPropertyListener(this::positionChanged);
        model_widget.positionX().addPropertyListener(this::positionChanged);
        model_widget.positionY().addPropertyListener(this::positionChanged);
        // Would like to also listen to positionWidth & height,
        // then call jfx_node.resizeRelocate(x, y, width, height),
        // but resizeRelocate tends to ignore the width & height on
        // several widgets (Rectangle), so have to call their
        // setWith() & setHeight() in specific representation.
    }

    private void positionChanged(final PropertyChangeEvent event)
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
