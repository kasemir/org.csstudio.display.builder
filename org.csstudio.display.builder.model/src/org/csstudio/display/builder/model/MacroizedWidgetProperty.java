/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.macros.MacroValueProvider;
import org.csstudio.display.builder.model.properties.IntegerWidgetProperty;

/** Base for Property that supports macros.
 *
 *  <p>Properties are typed.
 *  For example, the {@link IntegerWidgetProperty} has
 *  a value of type Integer.
 *
 *  <p>Macro-based properties have an additional 'specification',
 *  a text that may contain macros, for example "$(SOME_MACRO)".
 *
 *  <p>A model editor presents the specification to the user,
 *  and macro based properties persist the specification.
 *
 *  <p>At runtime, the specification is evaluated by
 *  replacing macros, setting the actual value.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
abstract public class MacroizedWidgetProperty<T> extends WidgetProperty<T>
{
// Also allow entering dynamic value description
// (PVManager formula "=`pv1`*2" ?)?
// Runtime then establishes subscription and updates value?

    /** Specification of the value, may contain macros that need to be expanded */
    protected volatile String specification;

    /** Constructor
     *  @param descriptor Property descriptor
     *  @param widget Widget that holds the property and handles listeners
     *  @param default_value Default and initial value
     */
    public MacroizedWidgetProperty(
            final WidgetPropertyDescriptor<T> descriptor,
            final Widget widget,
            final T default_value)
    {
        super(descriptor, widget, default_value);
        specification = String.valueOf(default_value);
    }

    /** @return Value specification. Text that may contain macros */
    public String getSpecification()
    {
        return specification;
    }

    /** Update the specification.
     *
     *  <p>Invalidates the typed value of the property,
     *  which is re-calculated when fetched the next time.
     *
     *  @param specification Specification of the value. Text that may contain macros
     */
    public void setSpecification(final String specification)
    {
        this.specification = specification;
        this.value = null;
        firePropertyChange(this, null, null);
    }

    /** Macro-based properties implement this to parse
     *  a specification text where all macros have been
     *  evaluated into the typed value.
     *
     *  <p>If implementation throws an exception,
     *  the default value of the property is used.
     *
     *  @param text Specification text, all known macros have been resolved
     *  @return Typed value
     *  @throws Exception on parse error.
     */
    abstract protected T parseExpandedSpecification(String text) throws Exception;

    /** Evaluates value based on specification
     *  @return Current value of the property
     */
    @Override
    public synchronized T getValue()
    {
        if (value == null)
        {
            final MacroValueProvider macros = widget.getMacrosOrProperties();
            String expanded;
            try
            {
                expanded = MacroHandler.replace(macros, specification);
            }
            catch (final Exception ex)
            {
                Logger.getLogger(getClass().getName())
                      .log(Level.WARNING, widget + " property " + getName() + " cannot expand macros for " + specification, ex);
                expanded = specification;
            }

            // TODO Do not allow this...
            if (MacroHandler.containsMacros(expanded))
                Logger.getLogger(getClass().getName())
                      .log(Level.WARNING, widget + " '" + getName() + "' is not fully resolved: " + expanded);

            try
            {
                super.setValue(parseExpandedSpecification(expanded));
            }
            catch (final Exception ex)
            {
                Logger.getLogger(getClass().getName())
                      .log(Level.WARNING, widget + " property " + getName() + " cannot evaluate " + expanded, ex);
                value = default_value;
            }
        }
        return value;
    }

    /** Sets property to a typed value.
     *
     *  <p>Updates the specification to string representation
     *  of the value.
     *
     *  @param value New typed value of the property
     */
    @Override
    public void setValue(final T value)
    {
        specification = String.valueOf(value);
        super.setValue(value);
    }

    /** @return Debug representation */
    @Override
    public String toString()
    {
        final T safe_copy = value;
        if (safe_copy == null)
            return "'" + getName() + "' = " + specification;
        else
            return "'" + getName() + "' = " + value;
    }
}
