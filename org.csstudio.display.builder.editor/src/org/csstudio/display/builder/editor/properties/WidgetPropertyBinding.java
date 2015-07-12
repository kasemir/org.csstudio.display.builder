/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import org.csstudio.display.builder.editor.undo.UndoableActionManager;
import org.csstudio.display.builder.model.WidgetProperty;

import javafx.scene.control.Control;

/** Bidirectional binding between a Widget and Java FX Property
 *  @author Kay Kasemir
 *  @param <JFX> JFX Control used to configure the property
 *  @param <WP> Widget property to configure
 */
abstract public class WidgetPropertyBinding<JFX extends Control, WP extends WidgetProperty<?>>
{
    protected final UndoableActionManager undo;
    protected final JFX jfx_control;
    protected final WP widget_property;

    /** Break update loops JFX change -> model change -> JFX change -> ... */
    protected boolean updating = false;

    /** @param control Java FX control to monitor and update
     *  @param widget_property Widget property to monitor and update
     */
    public WidgetPropertyBinding(final UndoableActionManager undo,
                                 final JFX control,
                                 final WP widget_property)
    {
        this.undo = undo;
        this.jfx_control = control;
        this.widget_property = widget_property;
    }

    /** Establish the binding */
    abstract public void bind();

    /** Remove the binding */
    abstract public void unbind();
}
