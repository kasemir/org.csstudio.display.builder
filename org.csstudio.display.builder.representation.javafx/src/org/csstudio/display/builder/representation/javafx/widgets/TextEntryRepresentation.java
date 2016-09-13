/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.FormatOption;
import org.csstudio.display.builder.model.util.FormatOptionHandler;
import org.csstudio.display.builder.model.widgets.TextEntryWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.VType;

import javafx.geometry.Insets;
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
public class TextEntryRepresentation extends RegionBaseRepresentation<TextField, TextEntryWidget>
{
    /** Is user actively editing the content, so updates should be suppressed? */
    private volatile boolean active = false;

    private final DirtyFlag dirty_size = new DirtyFlag();
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();
    private volatile String value_text = "<?>";

    @Override
    public TextField createJFXNode() throws Exception
    {
    	value_text = computeText(null);
        final TextField text = new TextField();
        text.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // Initially used 'focus' to activate the widget, but
        // when a display is opened, one of the text fields likely has the focus.
        // That widget will then NOT show any value, because we're active as if the
        // user just navigated into the field to edit it.
        // Now requiring key press, including use of cursor keys, to activate.
        text.setOnKeyPressed((final KeyEvent event) ->
        {
            switch (event.getCode())
            {
            case ESCAPE:
                if (active)
                {   // Revert original value, leave active state
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
        // While getting the focus does not activate the widget
        // (first need to type something),
        // _loosing_ focus de-activates the widget.
        // Otherwise widget where one moves the cursor, then clicks
        // someplace else would remain active and not show any updates
        text.focusedProperty().addListener((prop, old, focused) ->
        {
            if (active  &&  !focused)
            {
                restore();
                active = false;
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
        // Strip 'units' etc. from text
        final String text = jfx_node.getText();

        final Object value;
        if (model_widget.displayFormat().getValue() == FormatOption.STRING)
            value = text;
        else
            value = FormatOptionHandler.parse(model_widget.runtimeValue().getValue(), text);
        logger.log(Level.FINE, "Writing '" + text + "' as " + value + " (" + value.getClass().getName() + ")");
        toolkit.fireWrite(model_widget, value);

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
        model_widget.propWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::sizeChanged);
        model_widget.displayForegroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayBackgroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayFont().addUntypedPropertyListener(this::styleChanged);
        model_widget.behaviorEnabled().addUntypedPropertyListener(this::styleChanged);

        model_widget.displayFormat().addUntypedPropertyListener(this::contentChanged);
        model_widget.displayPrecision().addUntypedPropertyListener(this::contentChanged);
        model_widget.displayShowUnits().addUntypedPropertyListener(this::contentChanged);
        model_widget.runtimeValue().addUntypedPropertyListener(this::contentChanged);

        model_widget.behaviorPVName().addPropertyListener(this::pvnameChanged);
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

    /** @param value Current value of PV
     *  @return Text to show, "<pv name>" if disconnected (no value)
     */
    private String computeText(final VType value)
    {
        if (value == null)
            return "<" + model_widget.behaviorPVName().getValue() + ">";
        return FormatOptionHandler.format(value,
                                          model_widget.displayFormat().getValue(),
                                          model_widget.displayPrecision().getValue(),
                                          model_widget.displayShowUnits().getValue());
    }

    private void pvnameChanged(final WidgetProperty<String> property, final String old_value, final String new_value)
    {   // PV name typically changes in edit mode.
        // -> Show new PV name.
        // Runtime could deal with disconnect/reconnect for new PV name
        // -> Also OK to show disconnected state until runtime
        //    subscribes to new PV, so we eventually get values from new PV.
        value_text = computeText(null);
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        value_text = computeText(model_widget.runtimeValue().getValue());
        dirty_content.mark();
        if (! active)
            toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
            jfx_node.setPrefSize(model_widget.propWidth().getValue(),
                                 model_widget.propHeight().getValue());
        if (dirty_style.checkAndClear())
        {
            final String color = JFXUtil.webRGB(model_widget.displayForegroundColor().getValue());
            String style = "-fx-text-fill:" + color + ";";
            final Color background = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
            jfx_node.setBackground(new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY)));
            jfx_node.setFont(JFXUtil.convert(model_widget.displayFont().getValue()));

            // Don't disable the widget, because that would also remove the
            // context menu etc.
            // Just apply a style that matches the disabled look.
            final boolean enabled = model_widget.behaviorEnabled().getValue();
            jfx_node.setEditable(enabled);
            if (! enabled)
                style += "-fx-opacity: 0.4;";

            jfx_node.setStyle(style);

        }
        if (active)
            return;
        if (dirty_content.checkAndClear())
            jfx_node.setText(value_text);
    }
}
