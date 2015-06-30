/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionHeight;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionY;

import java.beans.PropertyChangeEvent;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.WidgetRepresentation;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Base class for all SWT widget representations
 *  @param <W> SWT widget
 *  @param <MW> Model Widget
 *  @author Kay Kasemir
 */
abstract public class SWTBaseRepresentation<W extends Control, MW extends Widget> extends WidgetRepresentation<Composite, Control, MW>
{
    protected W control;

    private final DirtyFlag dirty_position = new DirtyFlag();

    /** Construct representation for a model widget
     *  @param toolkit Toolkit helper
     *  @param model_widget Model widget
     */
    public SWTBaseRepresentation(final ToolkitRepresentation<Composite, Control> toolkit,
                                 final MW model_widget)
    {
        super(toolkit, model_widget);
    }

    /** {@inheritDoc} */
    @Override
    final public Composite init(final Composite parent) throws Exception
    {
        control = createSWTControl(parent);
        registerListeners();
        updateChanges();
        return getChildParent(parent);
    }

    /** Implementation needs to create the SWT control
     *  or composite for the model widget.
     *
     *  @return (Primary) SWT control
     *  @throws Exception on error
     */
    abstract protected W createSWTControl(final Composite parent) throws Exception;

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
    protected Composite getChildParent(final Composite parent)
    {
        return parent;
    }

    /** Register model widget listeners.
     *
     *  <p>Override must call base class
     */
    protected void registerListeners()
    {
        model_widget.addPropertyListener(positionX, this::positionChanged);
        model_widget.addPropertyListener(positionY, this::positionChanged);
        model_widget.addPropertyListener(positionWidth, this::positionChanged);
        model_widget.addPropertyListener(positionHeight, this::positionChanged);
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
            control.setBounds(model_widget.getPropertyValue(positionX).intValue(),
                              model_widget.getPropertyValue(positionY).intValue(),
                              model_widget.getPropertyValue(positionWidth).intValue(),
                              model_widget.getPropertyValue(positionHeight).intValue());
    }
}
