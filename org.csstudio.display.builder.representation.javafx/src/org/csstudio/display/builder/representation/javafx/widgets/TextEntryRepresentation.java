/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.beans.PropertyChangeEvent;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.TextEntryWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.epics.vtype.VType;

import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TextEntryRepresentation extends JFXBaseRepresentation<TextField, TextEntryWidget>
{
    /** Is user actively editing the content, so updates should be suppressed? */
    private volatile boolean active = false;

    private final DirtyFlag dirty_content = new DirtyFlag();
    private volatile String value_text = "<?>";

    public TextEntryRepresentation(final ToolkitRepresentation<Group, Node> toolkit,
                                   final TextEntryWidget model_widget)
    {
        super(toolkit, model_widget);
        value_text = "<" + model_widget.behaviorPVName().getValue() + ">";
    }

    @Override
    public TextField createJFXNode() throws Exception
    {
        final TextField text = new TextField();

        // Determine 'active' state (gain focus, entered first character)
        text.focusedProperty().addListener((final ObservableValue<? extends Boolean> observable,
                                            final Boolean old_value, final Boolean focus) ->
        {
            active = focus;
            if (! focus)
                restore();
        });
        text.setOnKeyPressed((final KeyEvent event) ->
        {
            switch (event.getCode())
            {
            case SHIFT:
            case ALT:
            case CONTROL:
                // Ignore modifier keys
                break;
            case ESCAPE:
                // Leave active state, reverting to original value
                if (active)
                {
                    active = false;
                    restore();
                }
                break;
            case ENTER:
                // Leave active state, submit value
                active = false;
                submit();
                break;
            default:
                // Any other key results in active state
                active = true;
            }
        });

        return text;
    }

    private void restore()
    {
        jfx_node.setText(value_text);
        // Request a refresh with 'value_text'
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    private void submit()
    {
        // TODO Submit entered text
        final String text = jfx_node.getText();
        System.out.println("Submit '" + text + "'");
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.runtimeValue().addPropertyListener(this::contentChanged);
    }

    private void contentChanged(final PropertyChangeEvent event)
    {
        value_text = VTypeUtil.getValueString((VType)event.getNewValue(), true);
        dirty_content.mark();
        if (! active)
            toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (active)
            return;
        if (dirty_content.checkAndClear())
            jfx_node.setText(value_text);
    }
}
