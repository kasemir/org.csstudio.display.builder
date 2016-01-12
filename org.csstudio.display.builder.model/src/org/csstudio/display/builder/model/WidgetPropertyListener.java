/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import org.csstudio.display.builder.model.properties.PropertyChangeHandler;

/** Typed notification interface for property change
 *
 *  <p>Use for type-safe notifications.
 *
 *  @see UntypedWidgetPropertyListener
 *  @see PropertyChangeHandler
 *  @author Kay Kasemir
 *
 *  @param <T> Type of the property's value
 */
@FunctionalInterface
public interface WidgetPropertyListener<T extends Object> extends BaseWidgetPropertyListener
{
    /** Invoked when property changed.
     *
     *  <p>Details on the old/new value hints depend on the property.
     *  Typically this is the true old and new value.
     *  The new value would match <code>property.getValue()</code>,
     *  except that the property's current value might already have
     *  changed in a multi-threaded scenario while the <code>new_value</code>
     *  is the value at the time the notification was generated.
     *
     *  <p>For list-typed properties, the value hints may only contain
     *  the list of elements that <u>changed</u> instead of the complete data.
     *
     *  @param property Property that changed
     *  @param old_value Old value hint
     *  @param new_value New value hint
     */
    public void propertyChanged(WidgetProperty<T> property, T old_value, T new_value);
}