/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.BaseLEDWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.AlarmSeverity;
import org.diirt.vtype.VType;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/** Base for LED type widgets
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
abstract class BaseLEDRepresentation<LED extends BaseLEDWidget> extends RegionBaseRepresentation<Pane, LED>
{

    public static Paint makeLEDGradient ( Color color ) {

        //  Original BaseLEDRepresentation & BoolButtonRepresentation gradient
        //      put highlight in top-left corner, about 0.2 wide,
        //      relative to actual size of LED
//        return new RadialGradient(
//            0, 0,
//            0.3, 0.3, 0.4,
//            true, CycleMethod.NO_CYCLE,
//            new Stop(0, color.interpolate(Color.WHITESMOKE, 0.8)),
//            new Stop(1, color)
//        );

        // Original ByteMonitorRepresentation gradient
//        return new LinearGradient(
//            0, 0,
//            .7, .7,
//            true, CycleMethod.NO_CYCLE,
//            new Stop(0, color.interpolate(Color.WHITESMOKE, 0.8)),
//            new Stop(1, color)
//        );

        return new LinearGradient(
            0, 0,
            .6, .6,
            true, CycleMethod.NO_CYCLE,
            new Stop(0, color.interpolate(Color.WHITESMOKE, 0.777)),
            new Stop(1, color)
        );

    }

    private final DirtyFlag typeChanged = new DirtyFlag();
    private final DirtyFlag styleChanged = new DirtyFlag();
    protected final DirtyFlag dirty_content = new DirtyFlag();

    protected volatile Color[] colors = new Color[0];

    protected volatile Color value_color;

    protected volatile String value_label;

    /** Actual LED Ellipse or Rectangle inside {@link Pane} to allow for border */
    private Shape led;

    protected Label label;

    @Override
    public Pane createJFXNode() throws Exception
    {
        colors = createColors();
        value_color = colors[0];

        return new Pane();
    }

    private void createLED()
    {
        jfx_node.getChildren().clear();
        if (model_widget.propSquare().getValue())
            led = new Rectangle();
        else
            led = new Ellipse();
        led.getStyleClass().add(model_widget.propFlat().getValue() ? "led_flat" : "led");
        label = new Label();
        label.getStyleClass().add("led_label");
        label.setAlignment(Pos.CENTER);
        jfx_node.getChildren().addAll(led, label);
    }

    @Override
    public int[] getBorderRadii()
    {
        if (led instanceof Ellipse)
            return new int[]
            {
                model_widget.propWidth().getValue()/2,
                model_widget.propHeight().getValue()/2,
            };
        return super.getBorderRadii();
    }

    /** Create colors for the states of the LED
     *  @return Colors, must contain at least one element
     */
    abstract protected Color[] createColors();

    /** Compute the index of the currently active color
     *  @param value Current value
     *  @return Index 0, 1, .. to maximum index of array provided by <code>createColors</code>
     */
    abstract protected int computeColorIndex(final VType value);

    /** Compute the label for currently active color index
     *  @param color_index Color index returned by <code>computeColorIndex()</code>
     *  @return String to show in label
     */
    abstract protected String computeLabel(final int color_index);

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.propSquare().addPropertyListener(this::typeChanged);
        model_widget.propFlat().addPropertyListener(this::typeChanged);
        model_widget.propWidth().addUntypedPropertyListener(this::styleChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::styleChanged);
        model_widget.propFont().addUntypedPropertyListener(this::styleChanged);
        model_widget.propForegroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.runtimePropValue().addPropertyListener(this::contentChanged);
        contentChanged(null, null, null);
    }

    private void typeChanged(final WidgetProperty<Boolean> property, final Boolean old_value, final Boolean new_value)
    {
        typeChanged.mark();
        toolkit.scheduleUpdate(this);
    }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        styleChanged.mark();
        toolkit.scheduleUpdate(this);
    }

    /** For derived class to invoke when color changed
     *  and current color needs to be re-evaluated
     *  @param property Ignored
     *  @param old_value Ignored
     *  @param new_value Ignored
     */
    protected void configChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        colors = createColors();
        contentChanged(model_widget.runtimePropValue(), null, model_widget.runtimePropValue().getValue());
    }

    private void contentChanged(final WidgetProperty<VType> property, final VType old_value, final VType new_value)
    {
        final VType value = model_widget.runtimePropValue().getValue();
        if (value == null)
        {
            value_color = alarm_colors[AlarmSeverity.UNDEFINED.ordinal()];
            value_label = "";
        }
        else
        {
            int value_index = computeColorIndex(new_value);
            final Color[] save_colors = colors;
            if (value_index < 0)
                value_index = 0;
            if (value_index >= save_colors.length)
                value_index = save_colors.length-1;
            value_color = save_colors[value_index];
            value_label = computeLabel(value_index);
        }

        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        if (typeChanged.checkAndClear())
        {
            createLED();
            styleChanged.mark();
            dirty_content.mark();
        }
        super.updateChanges();
        if (styleChanged.checkAndClear())
        {
            final Color color = JFXUtil.convert(model_widget.propForegroundColor().getValue());
            label.setTextFill(color);
            label.setFont(JFXUtil.convert(model_widget.propFont().getValue()));

            final int w = model_widget.propWidth().getValue();
            final int h = model_widget.propHeight().getValue();

            jfx_node.setMinSize(w, h);
            jfx_node.setPrefSize(w, h);
            jfx_node.setMaxSize(w, h);
            if (led instanceof Ellipse)
            {
                final Ellipse ell = (Ellipse) led;
                ell.setCenterX(w/2);
                ell.setCenterY(h/2);
                ell.setRadiusX(w/2);
                ell.setRadiusY(h/2);
            }
            else if (led instanceof Rectangle)
            {
                final Rectangle rect = (Rectangle) led;
                rect.setWidth(w);
                rect.setHeight(h);
            }
            label.setPrefSize(w, h);
        }
        if (dirty_content.checkAndClear())
        {
            led.setFill(model_widget.propFlat().getValue() ? value_color : makeLEDGradient(value_color));
            label.setText(value_label);
        }
    }

}
