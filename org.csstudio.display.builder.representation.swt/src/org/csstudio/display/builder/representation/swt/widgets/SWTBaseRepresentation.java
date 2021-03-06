/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.representation.WidgetRepresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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

    /** {@inheritDoc} */
    @Override
    final public Composite createComponents(final Composite parent) throws Exception
    {
        control = createSWTControl(parent);
        control.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseDown(final MouseEvent e)
            {
                toolkit.fireClick(model_widget, (e.stateMask & SWT.CONTROL) != 0);
            }
        });

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

    /** {@inheritDoc} */
    @Override
    public void dispose()
    {
        control.dispose();
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
        model_widget.propX().addUntypedPropertyListener(this::positionChanged);
        model_widget.propY().addUntypedPropertyListener(this::positionChanged);
        model_widget.propWidth().addUntypedPropertyListener(this::positionChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::positionChanged);
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
            control.setBounds(model_widget.propX().getValue(),
                              model_widget.propY().getValue(),
                              model_widget.propWidth().getValue(),
                              model_widget.propHeight().getValue());
    }
}
