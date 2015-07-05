/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.csstudio.display.builder.model.WidgetProperty;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.StringProperty;

/** Bidirectional binding between a Widget and Java FX Property
 *  @author Kay Kasemir
 */
abstract public class WidgetPropertyBinding<WP extends WidgetProperty<?>>
{
    private final StringProperty jfx_property;
    protected final WP widget_property;

    /** Break update loops JFX change -> model change -> JFX change -> ... */
    private boolean updating = false;

    private final InvalidationListener jfx_listener = new InvalidationListener()
    {
        @Override
        public void invalidated(Observable observable)
        {
            if (updating)
                return;
            updating = true;
            try
            {
                setWidgetText(jfx_property.getValue());
            }
            finally
            {
                updating = false;
            }
        }
    };

    private final PropertyChangeListener model_listener = new PropertyChangeListener()
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (updating)
                return;
            updating = true;
            try
            {
                jfx_property.setValue(getWidgetText());
            }
            finally
            {
                updating = false;
            }
        }
    };

    /** @param jfx_property Java FX property to monitor and update
     *  @param widget_property Widget property to monitor and update
     */
    public WidgetPropertyBinding(final StringProperty jfx_property, final WP widget_property)
    {
        this.jfx_property = jfx_property;
        this.widget_property = widget_property;
    }

    /** @return Model widget text */
    abstract String getWidgetText();

    /** @param text Text to place into widget's property */
    abstract void setWidgetText(String text);

    /** Establish the binding */
    public void bind()
    {
        jfx_property.setValue(getWidgetText());

        jfx_property.addListener(jfx_listener);
        widget_property.addPropertyListener(model_listener);
    }

    /** Remove the binding */
    public void unbind()
    {
        jfx_property.removeListener(jfx_listener);
        widget_property.removePropertyListener(model_listener);
    }
}
