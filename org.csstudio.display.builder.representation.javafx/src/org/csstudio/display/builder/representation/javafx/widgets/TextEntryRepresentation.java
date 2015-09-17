/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.TextEntryWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.epics.vtype.VType;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TextEntryRepresentation extends JFXBaseRepresentation<TextField, TextEntryWidget>
{
    /** Is user actively editing the content, so updates should be suppressed? */
    private volatile boolean active = false;

    private final DirtyFlag dirty_size = new DirtyFlag();
    private final DirtyFlag dirty_style = new DirtyFlag();
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
        text.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // Determine 'active' state (gain focus, entered first character)
        text.focusedProperty().addListener((final ObservableValue<? extends Boolean> observable,
                                            final Boolean old_value, final Boolean focus) ->
        {
            // Gain focus -> active. Loose focus -> restore
            active = focus;
            // This will restore the JFX control to the current value
            // regardless if user 'entered' a new value, then looses
            // focus, or just looses focus.
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
                // Revert original value, leave active state
                if (active)
                {
                    restore();
                    active = false;
                }
                break;
            case ENTER:
                // Submit value, leave active state
                submit();
                active = false;
                break;
            default:
                // Any other key results in active state
                active = true;
            }
        });

        return text;
    }

    /** Restore representation to last known value,
     *  replacing what user might have entered
     */
    private void restore()
    {
        jfx_node.setText(value_text);
    }

    /** Submit value entered by user */
    private void submit()
    {
        final String text = jfx_node.getText();
        toolkit.fireWrite(model_widget, text);
        // Wrote value. Expected is either
        // a) PV receives that value, PV updates to
        //    submitted value or maybe a 'clamped' value
        // --> We'll receive contentChanged() and display PV's latest.
        // b) PV doesn't receive the value and never sends
        //    an update. JFX control is stuck with the 'text'
        //    the user entered, not reflecting the actual PV
        // --> Request an update to the last known 'value_text'.
        //
        // This could result in a little flicker:
        // User enters "new_value".
        // We send that, but restore "old_value" to handle case b)
        // PV finally sends "new_value", and we show that.
        //
        // In practice, this rarely happens because we only schedule an update.
        // By the time it executes, we already have case a.
        // If it does turn into a problem, could introduce toolkit.scheduleDelayedUpdate()
        // so that case b) only restores the old 'value_text' after some delay,
        // increasing the chance of a) to happen.
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::sizeChanged);
        model_widget.displayBackgroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayFont().addUntypedPropertyListener(this::styleChanged);
        model_widget.runtimeValue().addPropertyListener(this::contentChanged);
    }

    private void sizeChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    private void contentChanged(final WidgetProperty<VType> property, final VType old_value, final VType new_value)
    {
        value_text = VTypeUtil.getValueString(new_value, true);
        dirty_content.mark();
        if (! active)
            toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
            jfx_node.setPrefSize(model_widget.positionWidth().getValue(),
                                 model_widget.positionHeight().getValue());
        if (dirty_style.checkAndClear())
        {
            final Color background = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
            jfx_node.setBackground(new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY)));
            jfx_node.setFont(JFXUtil.convert(model_widget.displayFont().getValue()));
        }
        if (active)
            return;
        if (dirty_content.checkAndClear())
            jfx_node.setText(value_text);
    }
}
