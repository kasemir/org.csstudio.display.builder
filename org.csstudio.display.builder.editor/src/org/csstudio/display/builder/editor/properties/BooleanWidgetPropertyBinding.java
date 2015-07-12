/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.csstudio.display.builder.editor.undo.SetMacroizedWidgetProperty;
import org.csstudio.display.builder.editor.undo.UndoableActionManager;
import org.csstudio.display.builder.model.properties.BooleanWidgetProperty;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/** Bidirectional binding between a Widget and Java FX Property
 *  @author Kay Kasemir
 */
public class BooleanWidgetPropertyBinding
       extends WidgetPropertyBinding<ComboBox<String>, BooleanWidgetProperty>
{
    // Would be nice to just listen to jfx_control.valueProperty(),
    // but want to support 'escape' and loss of focus to revert,
    // and only complete text confirmed with Enter is submitted as an undoable action,
    // not each key stroke.

    /** Update control if model changes */
    private final PropertyChangeListener model_listener = (final PropertyChangeEvent evt) ->
    {
        if (updating)
            return;
        updating = true;
        try
        {
            jfx_control.setValue(widget_property.getSpecification());
        }
        finally
        {
            updating = false;
        }
    };

    /** When loosing focus, restore control to current value of property.
     *  (If user just submitted a new value, that's a NOP)
     */
    private final ChangeListener<Boolean> focus_handler =
        (final ObservableValue<? extends Boolean> observable,
         final Boolean old_focus, final Boolean focus) ->
    {
        if (! focus)
            restore();
        updating = focus;
    };

    /** Submit new value, either selected from list or typed with 'Enter' */
    private final EventHandler<ActionEvent> combo_handler = (final ActionEvent event) ->
    {
        submit();
    };

    /** Revert on Escape, otherwise mark as active to prevent model updates */
    private final EventHandler<KeyEvent> key_filter = (final KeyEvent t) ->
    {
        if (t.getCode() == KeyCode.ESCAPE)
        {
            // Revert original value, leave active state
            if (updating)
            {
                restore();
                updating = false;
                t.consume();
            }
        }
        else // Any other key marks the control as being updated by user
            updating = true;
    };

    public BooleanWidgetPropertyBinding(final UndoableActionManager undo,
                                        final ComboBox<String> control,
                                        final BooleanWidgetProperty widget_property)
    {
        super(undo, control, widget_property);
    }

    @Override
    public void bind()
    {
        restore();
        widget_property.addPropertyListener(model_listener);
        jfx_control.focusedProperty().addListener(focus_handler);
        jfx_control.addEventFilter(KeyEvent.KEY_PRESSED, key_filter);
        jfx_control.setOnAction(combo_handler);
    }

    @Override
    public void unbind()
    {
        jfx_control.setOnAction(null);
        jfx_control.removeEventFilter(KeyEvent.KEY_PRESSED, key_filter);
        jfx_control.focusedProperty().removeListener(focus_handler);
        widget_property.removePropertyListener(model_listener);
    }

    private void submit()
    {
        undo.execute(new SetMacroizedWidgetProperty(widget_property,
                                                    jfx_control.getValue()));
        updating = false;
    }

    private void restore()
    {
        final String orig = widget_property.getSpecification();
        // 'value' is the internal value of the combo box
        jfx_control.setValue(orig);
        // Also need to update the editor, which will otherwise
        // soon set the 'value'
        jfx_control.getEditor().setText(orig);
    }
}
