/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.List;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.BoolButtonWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.VEnum;
import org.diirt.vtype.VType;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
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

    // Design decision: Plain Button.
    // JFX ToggleButton appears natural to reflect two states,
    // but this type of button is updated by both the user
    // (press to 'push', 'release') and the PV.
    // When user pushes button, value is sent to PV
    // and the button should update its state as the
    // update is received from the PV.
    // The ToggleButton, however, will 'select' or not
    // just because of the user interaction.
    // If this is then in addition updated by the PV,
    // the ToggleButton tends to 'flicker'.

    private volatile Button button;
    private volatile Ellipse led;

    protected volatile Color[] state_colors;
    protected volatile Color value_color;
    protected volatile String[] state_labels;
    protected volatile String value_label;

    @Override
    public ButtonBase createJFXNode() throws Exception
    {
        led = new Ellipse();
        button = new Button("BoolButton", led);
        button.setOnAction(event -> handlePress());

        // Model has width/height, but JFX widget has min, pref, max size.
        // updateChanges() will set the 'pref' size, so make min use that as well.
        button.setMinSize(ButtonBase.USE_PREF_SIZE, ButtonBase.USE_PREF_SIZE);
        return button;
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
        model_widget.propWidth().addUntypedPropertyListener(this::representationChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::representationChanged);
        model_widget.propOnLabel().addUntypedPropertyListener(this::representationChanged);
        model_widget.propOnColor().addUntypedPropertyListener(this::representationChanged);
        model_widget.propOffLabel().addUntypedPropertyListener(this::representationChanged);
        model_widget.propOffColor().addUntypedPropertyListener(this::representationChanged);
        model_widget.propFont().addUntypedPropertyListener(this::representationChanged);
        model_widget.propBit().addPropertyListener(this::bitChanged);
        model_widget.runtimePropValue().addPropertyListener(this::contentChanged);
        bitChanged(model_widget.propBit(), null, model_widget.propBit().getValue());
    }

    protected Color[] createColors()
    {
        return new Color[]
        {
            JFXUtil.convert(model_widget.propOffColor().getValue()),
            JFXUtil.convert(model_widget.propOnColor().getValue())
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
        if ((new_value instanceof VEnum)  &&
            model_widget.propLabelsFromPV().getValue())
        {
            final List<String> labels = ((VEnum) new_value).getLabels();
            if (labels.size() == 2)
            {
                model_widget.propOffLabel().setValue(labels.get(0));
                model_widget.propOnLabel().setValue(labels.get(1));
            }
        }

        rt_value = VTypeUtil.getValueNumber(new_value).intValue();
        stateChanged();
    }


    private void representationChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        state_colors = createColors();
        state_labels = new String[] { model_widget.propOffLabel().getValue(), model_widget.propOnLabel().getValue() };
        value_color = state_colors[on_state];
        value_label = state_labels[on_state];
        dirty_representation.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        boolean update_content = dirty_content.checkAndClear();
        if (dirty_representation.checkAndClear())
        {
            jfx_node.setPrefSize(model_widget.propWidth().getValue(),
                                 model_widget.propHeight().getValue());
            jfx_node.setFont(JFXUtil.convert(model_widget.propFont().getValue()));
            led.setRadiusX(model_widget.propWidth().getValue() / 15.0);
            led.setRadiusY(model_widget.propWidth().getValue() / 10.0);
            update_content = true;
        }
        if (update_content)
        {
            jfx_node.setText(value_label);
            // Put highlight in top-left corner, about 0.2 wide,
            // relative to actual size of LED
            led.setFill(new RadialGradient(0, 0, 0.3, 0.3, 0.4, true, CycleMethod.NO_CYCLE,
                                           new Stop(0, value_color.interpolate(Color.WHITESMOKE, 0.8)),
                                           new Stop(1, value_color)));
        }
    }
}
