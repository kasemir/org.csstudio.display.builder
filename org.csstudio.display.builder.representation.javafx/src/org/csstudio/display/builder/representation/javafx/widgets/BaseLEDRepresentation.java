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
import org.diirt.vtype.VType;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;

/** Base for LED type widgets
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
abstract class BaseLEDRepresentation<LED extends BaseLEDWidget> extends RegionBaseRepresentation<Pane, LED>
{
    private final DirtyFlag dirty_size = new DirtyFlag();
    protected final DirtyFlag dirty_content = new DirtyFlag();

    protected volatile Color[] colors = new Color[0];

    protected volatile Color value_color;

    /** Actual LED Ellipse inside {@link Pane} to allow for border */
    private Ellipse led;

    @Override
    public Pane createJFXNode() throws Exception
    {
        colors = createColors();
        value_color = colors[0];

        led = new Ellipse();
        led.getStyleClass().add("led");
        final Pane pane = new Pane(led);
        pane.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        pane.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        return pane;
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

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addPropertyListener(this::sizeChanged);
        model_widget.positionHeight().addPropertyListener(this::sizeChanged);
        model_widget.runtimeValue().addPropertyListener(this::contentChanged);
    }

    private void sizeChanged(final WidgetProperty<Integer> property, final Integer old_value, final Integer new_value)
    {
        dirty_size.mark();
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
        contentChanged(model_widget.runtimeValue(), null, model_widget.runtimeValue().getValue());
    }

    private void contentChanged(final WidgetProperty<VType> property, final VType old_value, final VType new_value)
    {
        int value_index = computeColorIndex(new_value);
        final Color[] save_colors = colors;
        if (value_index < 0)
            value_index = 0;
        if (value_index >= save_colors.length)
            value_index = save_colors.length-1;
        value_color = save_colors[value_index];

        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
        {
            final int w = model_widget.positionWidth().getValue();
            final int h = model_widget.positionHeight().getValue();

            jfx_node.setPrefSize(w, h);
            led.setCenterX(w/2);
            led.setCenterY(h/2);
            led.setRadiusX(w/2);
            led.setRadiusY(h/2);
        }
        if (dirty_content.checkAndClear())
            led.setFill(
                // Put highlight in top-left corner, about 0.2 wide,
                // relative to actual size of LED
                new RadialGradient(0, 0, 0.3, 0.3, 0.4, true, CycleMethod.NO_CYCLE,
                                   new Stop(0, value_color.interpolate(Color.WHITESMOKE, 0.8)),
                                   new Stop(1, value_color)));
    }
}
