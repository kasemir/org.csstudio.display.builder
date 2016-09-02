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
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.BoolButtonWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.VType;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;

/** Creates JavaFX item for model widget
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class BoolButtonRepresentation extends RegionBaseRepresentation<ButtonBase, BoolButtonWidget>
{

    private final DirtyFlag dirty_representation = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();
    protected volatile int on_state = 1;
    protected volatile int use_bit = 0;
    protected volatile Integer rt_value = 0;

    private volatile Button button;
    private volatile Ellipse led;

    protected volatile Color[] state_colors;
    protected volatile Color value_color;
    protected volatile String[] state_labels;
    protected volatile String value_label;

    @Override
    public ButtonBase createJFXNode() throws Exception
    {
        final ButtonBase base;
        led = new Ellipse();
        button = new Button("BoolButton", led);
        button.setOnAction(event -> handlePress());
        base = button;

        // Model has width/height, but JFX widget has min, pref, max size.
        // updateChanges() will set the 'pref' size, so make min use that as well.
        base.setMinSize(ButtonBase.USE_PREF_SIZE, ButtonBase.USE_PREF_SIZE);

        // Monitor keys that modify the OpenDisplayActionInfo.Target.
        // Use filter to capture event that's otherwise already handled.
        base.addEventFilter(MouseEvent.MOUSE_PRESSED, this::checkModifiers);
        return base;
    }

    /** @param event Mouse event to check for target modifier keys */
    private void checkModifiers(final MouseEvent event)
    {
        // At least on Linux, a Control-click or Shift-click
        // will not 'arm' the button, so the click is basically ignored.
        // Force the 'arm', so user can Control-click or Shift-click to
        // invoke the button
        if (event.isControlDown() ||
            event.isShiftDown())
            jfx_node.arm();
    }

    /** @param respond to button press */
    private void handlePress()
    {
        logger.log(Level.FINE, "{0} pressed", model_widget);
        int new_val = (rt_value ^ ((use_bit < 0) ? 1 : (1 << use_bit)) );
        toolkit.fireWrite(model_widget, new_val);
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        representationChanged(null,null,null);
        model_widget.positionWidth().addUntypedPropertyListener(this::representationChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::representationChanged);
        model_widget.displayOnLabel().addUntypedPropertyListener(this::representationChanged);
        model_widget.displayOnColor().addUntypedPropertyListener(this::representationChanged);
        model_widget.displayOffLabel().addUntypedPropertyListener(this::representationChanged);
        model_widget.displayOffColor().addUntypedPropertyListener(this::representationChanged);
        model_widget.displayFont().addUntypedPropertyListener(this::representationChanged);

        bitChanged(model_widget.behaviorBit(), null, model_widget.behaviorBit().getValue());
        model_widget.behaviorBit().addPropertyListener(this::bitChanged);
        model_widget.runtimeValue().addPropertyListener(this::contentChanged);

        //representationChanged(null,null,null);
    }

    protected Color[] createColors()
    {
        return new Color[]
        {
            JFXUtil.convert(model_widget.displayOffColor().getValue()),
            JFXUtil.convert(model_widget.displayOnColor().getValue())
        };
    }

    private void stateChanged()
    {
        on_state = ((use_bit < 0) ? (rt_value != 0) : (((rt_value >> use_bit) & 1) == 1)) ? 1 : 0;
        value_color = state_colors[on_state];
        value_label = state_labels[on_state];

        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    private void bitChanged(final WidgetProperty<Integer> property, final Integer old_value, final Integer new_value)
    {
        use_bit = new_value;

        stateChanged();
    }

    private void contentChanged(final WidgetProperty<VType> property, final VType old_value, final VType new_value)
    {
        rt_value = VTypeUtil.getValueNumber(new_value).intValue();
        stateChanged();
    }


    private void representationChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        state_colors = createColors();
        state_labels = new String[] { model_widget.displayOffLabel().getValue(), model_widget.displayOnLabel().getValue() };
        value_color = state_colors[on_state];
        value_label = state_labels[on_state];
        dirty_representation.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_content.checkAndClear())
        {
            jfx_node.setText(value_label);

            led.setFill(
                    // Put highlight in top-left corner, about 0.2 wide,
                    // relative to actual size of LED
                    new RadialGradient(0, 0, 0.3, 0.3, 0.4, true, CycleMethod.NO_CYCLE,
                                       new Stop(0, value_color.interpolate(Color.WHITESMOKE, 0.8)),
                                       new Stop(1, value_color)));
        }
        if (dirty_representation.checkAndClear())
        {
            jfx_node.setText(value_label);

            jfx_node.setPrefSize(model_widget.positionWidth().getValue(),
                                 model_widget.positionHeight().getValue());
            jfx_node.setFont(JFXUtil.convert(model_widget.displayFont().getValue()));

            led.setFill(
                    // Put highlight in top-left corner, about 0.2 wide,
                    // relative to actual size of LED
                    new RadialGradient(0, 0, 0.3, 0.3, 0.4, true, CycleMethod.NO_CYCLE,
                                       new Stop(0, value_color.interpolate(Color.WHITESMOKE, 0.8)),
                                       new Stop(1, value_color)));

            led.setRadiusX(model_widget.positionWidth().getValue() / 15.0);
            led.setRadiusY(model_widget.positionWidth().getValue() / 10.0);
        }
    }
}
