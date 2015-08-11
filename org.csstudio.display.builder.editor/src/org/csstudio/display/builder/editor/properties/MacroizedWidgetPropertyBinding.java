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
import org.csstudio.display.builder.model.MacroizedWidgetProperty;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;

/** Bidirectional binding between a WidgetProperty and Java FX Node
 *  @author Kay Kasemir
 */
public class MacroizedWidgetPropertyBinding
       extends WidgetPropertyBinding<TextInputControl, MacroizedWidgetProperty<?>>
{
    private final PropertyChangeListener model_listener = new PropertyChangeListener()
    {
        @Override
        public void propertyChange(final PropertyChangeEvent evt)
        {
            if (updating)
                return;
            updating = true;
            try
            {
                jfx_node.setText(widget_property.getSpecification());
            }
            finally
            {
                updating = false;
            }
        }
    };

    // Tried binding to JFX text property, but it changes
    // with every entered character.
    // An 'undoable' change for each char is too fine.
    // Desired are undo steps for new values that the user
    // confirms via 'enter'.
    private final ChangeListener<Boolean> focus_handler =
        (final ObservableValue<? extends Boolean> observable,
         final Boolean old_focus, final Boolean focus) ->
    {
        // Gain focus -> active. Loose focus -> restore
        updating = focus;
        // This will restore the JFX control to the current value
        // regardless if user 'entered' a new value, then looses
        // focus, or just looses focus.
        if (! focus)
            restore();
    };

    private final EventHandler<KeyEvent> key_press_handler =
        (final KeyEvent event) ->
    {
        switch (event.getCode())
        {
        case SHIFT:
        case ALT:
        case CONTROL:
            // Ignore modifier keys
            break;
        case ESCAPE:
            // Revert original value, leave active state
            if (updating)
            {
                restore();
                updating = false;
            }
            break;
        case ENTER:
            // Submit value, leave active state
            submit();
            updating = false;
            break;
        default:
            // Any other key results in active state
            updating = true;
        }
    };


    public MacroizedWidgetPropertyBinding(final UndoableActionManager undo,
                                          final TextInputControl field,
                                          final MacroizedWidgetProperty<?> widget_property)
    {
        super(undo, field, widget_property);
    }

    @Override
    public void bind()
    {
        widget_property.addPropertyListener(model_listener);
        jfx_node.setOnKeyPressed(key_press_handler);
        jfx_node.focusedProperty().addListener(focus_handler);
        restore();
    }

    @Override
    public void unbind()
    {
        jfx_node.focusedProperty().removeListener(focus_handler);
        jfx_node.setOnKeyPressed(null);
        widget_property.removePropertyListener(model_listener);
    }

    private void submit()
    {
        undo.execute(new SetMacroizedWidgetProperty(widget_property,
                                                    jfx_node.getText()));
    }

    private void restore()
    {
        jfx_node.setText(widget_property.getSpecification());
    }
}
