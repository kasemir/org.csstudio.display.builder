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
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.ByteMonitorWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.VType;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls") // TODO
public class ByteMonitorRepresentation extends RegionBaseRepresentation<Pane, ByteMonitorWidget>
{
    private final DirtyFlag dirty_size = new DirtyFlag();
    protected final DirtyFlag dirty_content = new DirtyFlag();

    protected volatile Color[] colors = new Color[2];

    protected volatile Color[] value_colors = null;

    private volatile int startBit = 0;
    private volatile int numBits = 8;
    private volatile boolean bitReverse = false;
    private volatile boolean horizontal = true;
    private volatile boolean square_led = false;

    private volatile Shape[] leds = null;

    @Override
    //XXX: consider Pane vs Canvas
    protected Pane createJFXNode() throws Exception
    {
        colors = createColors();
        final Pane pane = new Pane();
        numBits = model_widget.displayNumBits().getValue();
        square_led = model_widget.displaySquareLED().getValue();
        horizontal = model_widget.displayHorizontal().getValue();
        addLEDs(pane);
        pane.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        pane.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        return pane;
    }

    private void addLEDs(final Pane pane)
    {
        addLEDs(pane, model_widget.positionWidth().getValue(),
                model_widget.positionHeight().getValue(), horizontal);
    }

    private void addLEDs(final Pane pane, final int w, final int h, final boolean horizontal)
    {
        final int save_bits = numBits;
        final boolean save_sq = square_led;
        final Color [] save_colorVals = value_colors;
        final Shape [] leds = new Shape[save_bits];
        for (int i = 0; i < save_bits; i++)
        {
            final Shape led;
            if (save_sq)
            {
                final Rectangle rect = new Rectangle();
                rect.setX(horizontal ? i*w/save_bits : 0);
                rect.setY(horizontal ? 0 : i*h/save_bits);
                rect.setWidth(horizontal ? w/save_bits : w);
                rect.setHeight(horizontal ? h : h/save_bits);
                led = rect;
            }
            else
            {
                final Ellipse ell = new Ellipse();
                final int d = Math.min(horizontal ? w/save_bits : w, horizontal ? h : h/save_bits);
                ell.setCenterX(horizontal ? d/2 + i*w/save_bits : d/2);
                ell.setCenterY(horizontal ? d/2 : d/2 + i*h/save_bits);
                ell.setRadiusX(d/2);
                ell.setRadiusY(d/2);
                led = ell;
            }
            led.getStyleClass().add("led");
            if (save_colorVals != null && i < save_colorVals.length)
                led.setFill( makeGradient(save_colorVals[i]) );
            leds[i] = led;
        }
        this.leds = leds;
        pane.getChildren().clear();
        pane.getChildren().addAll(leds);
    }

    private LinearGradient makeGradient(final Color color)
    {
        return new LinearGradient(0, 0, .7, .7, true, CycleMethod.NO_CYCLE,
                new Stop(0, color.interpolate(Color.WHITESMOKE, 0.8)),
                new Stop(1, color));
    }

    protected Color[] createColors()
    {
        return new Color[]
        {
            JFXUtil.convert(model_widget.displayOffColor().getValue()),
            JFXUtil.convert(model_widget.displayOnColor().getValue())
        };
    }

    protected int [] computeColorIndices(final VType value)
    {
        int nBits = numBits;
        final int sBit = startBit;
        if (nBits + sBit > 32)
            nBits = 32 - sBit;
        final boolean save_bitRev = bitReverse;

        final int [] colorIndices = new int [nBits];
        final int number = VTypeUtil.getValueNumber(value).intValue();
        for (int i = 0; i < nBits; i++)
            colorIndices[ save_bitRev ? i : nBits-1-i] = number & (1 << (sBit+i));
        return colorIndices;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addPropertyListener(this::sizeChanged);
        model_widget.positionHeight().addPropertyListener(this::sizeChanged);

        model_widget.runtimeValue().addPropertyListener(this::contentChanged);

        model_widget.displayOffColor().addUntypedPropertyListener(this::configChanged);
        model_widget.displayOnColor().addUntypedPropertyListener(this::configChanged);
        model_widget.displayStartBit().addUntypedPropertyListener(this::configChanged);
        model_widget.displayBitReverse().addUntypedPropertyListener(this::configChanged);

        model_widget.displayNumBits().addUntypedPropertyListener(this::lookChanged);
        model_widget.displayHorizontal().addUntypedPropertyListener(this::lookChanged);
        model_widget.displaySquareLED().addUntypedPropertyListener(this::lookChanged);

        //initialization
        configChanged(null, null, null);
        lookChanged(null, null, null);
    }

    /**
     * Invoked when LED shape, number, or arrangement
     * changed (square_led, numBits, horizontal)
     * @param property Ignored
     * @param old_value Ignored
     * @param new_value Ignored
     */
    protected void lookChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        numBits = model_widget.displayNumBits().getValue();
        horizontal = model_widget.displayHorizontal().getValue();
        square_led = model_widget.displaySquareLED().getValue();
            //note: copied to array to safeguard against mid-operation changes
        dirty_size.mark();
        contentChanged(model_widget.runtimeValue(), null, model_widget.runtimeValue().getValue());
    }

    private void sizeChanged(final WidgetProperty<Integer> property, final Integer old_value, final Integer new_value)
    {
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }

    /** Invoked when color, startBit, or bitReverse properties
     *  changed and current colors need to be re-evaluated
     *  @param property Ignored
     *  @param old_value Ignored
     *  @param new_value Ignored
     */
    protected void configChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        startBit = model_widget.displayStartBit().getValue();
        bitReverse = model_widget.displayBitReverse().getValue();
        colors = createColors();
        contentChanged(model_widget.runtimeValue(), null, model_widget.runtimeValue().getValue());
    }

    private void contentChanged(final WidgetProperty<VType> property, final VType old_value, final VType new_value)
    {
        final int value_indices [] = computeColorIndices(new_value);
        final Color[] new_colorVals = new Color[value_indices.length];
        final Color[] save_colors = colors;
        for (int i = 0; i < value_indices.length; i++)
        {
            value_indices[i] = value_indices[i] <= 0 ? 0 : 1;
            new_colorVals[i] = save_colors[value_indices[i]];
        }
        value_colors = new_colorVals;

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
            addLEDs(jfx_node, w, h, horizontal);

        }
        if (dirty_content.checkAndClear())
        {
            final Shape[] save_leds = leds;
            final Color[] save_values = value_colors;
            if (save_leds == null  ||  save_values == null)
                return;

            final int N = Math.min(save_leds.length, save_values.length);
            for (int i = 0; i < N; i++)
                leds[i].setFill( makeGradient(save_values[i]));
        }
    }
}