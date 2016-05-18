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
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
public class ByteMonitorRepresentation extends RegionBaseRepresentation<Pane, ByteMonitorWidget>
//consider: extend BaseLEDRepresentation instead (but fundamentally arrays, not singleton values)
{
    private final DirtyFlag dirty_size = new DirtyFlag();
    protected final DirtyFlag dirty_content = new DirtyFlag();

    protected volatile Color[] colors = new Color[2];

    protected volatile Color[] value_colors = new Color [16];
    
    private volatile int startBit = 0;
    private volatile int numBits = 8;
    private volatile boolean horizontal = true;
    private volatile boolean square_led = false;

    /** LED Ellipses inside {@link Pane} for grouping and alignment */
    private Shape [] leds = new Shape [16];
    
    @Override
    //XXX: consider Pane vs Canvas;
    protected Pane createJFXNode() throws Exception {
        numBits = model_widget.displayNumBits().getValue();
        square_led = model_widget.displaySquareLED().getValue();
        colors = createColors();
        final Pane pane = new Pane();
        for (int i = 0; i < numBits; i++) {
            value_colors[i] = colors[0];
            leds[i] = square_led ? new Rectangle() : new Ellipse();
            leds[i].getStyleClass().add("led");
            pane.getChildren().add(leds[i]);
        }
        pane.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        pane.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        return pane;
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
        int [] colorIndices = new int [numBits];
        int number = VTypeUtil.getValueNumber(value).intValue();
        for (int i = 0; i < numBits; i++) {
            colorIndices[model_widget.displayBitReverse().getValue() ? i : numBits-1-i] =
                    ( number & (1 << (startBit+i)) );
        }
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
        model_widget.displayNumBits().addUntypedPropertyListener(this::lookChanged);
        model_widget.displayStartBit().addUntypedPropertyListener(this::configChanged);
        model_widget.displayBitReverse().addUntypedPropertyListener(this::configChanged);
        model_widget.displayHorizontal().addUntypedPropertyListener(this::lookChanged);
        model_widget.displaySquareLED().addUntypedPropertyListener(this::lookChanged);
    }
    
    /**
     * Invoked when type, number, or arrangement of
     * LEDs changed (squareLED, numBits, or horizontal)
     * @param property Ignored
     * @param old_value Ignored
     * @param new_value Ignored
     */
    protected void lookChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        numBits = model_widget.displayNumBits().getValue();
        horizontal = model_widget.displayHorizontal().getValue();
        square_led = model_widget.displaySquareLED().getValue();
        dirty_size.mark();
        contentChanged(model_widget.runtimeValue(), null, model_widget.runtimeValue().getValue());
    }
    
    private void sizeChanged(final WidgetProperty<Integer> property, final Integer old_value, final Integer new_value)
    {
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }
    
    /** Invoked when color or bitReverse properties changed
     *  and current colors need to be re-evaluated
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
        startBit = model_widget.displayStartBit().getValue();
        if (startBit + numBits > 16)
            startBit = 16 - numBits;

        int value_indices [] = computeColorIndices(new_value);
        final Color[] save_colors = colors;
        for (int i = 0; i < numBits; i++) {
            if (value_indices[i] < 0)
                value_indices[i] = 0;
            if (value_indices[i] >= save_colors.length)
                value_indices[i] = save_colors.length-1;
            value_colors[i] = save_colors[value_indices[i]];
        }
        
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }
    
    /** helper for updateChanges, when the shape of the LED is changed */
    private int replaceShape(Shape shape, int index, int numChildren) {
        leds[index] = shape;
        leds[index].getStyleClass().add("led");
        if (index < numChildren)
            jfx_node.getChildren().remove(index, numChildren);
        jfx_node.getChildren().add(leds[index]);
        return index+1;
    }
    
    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
        {
            //adjust number and type of child LEDs in pane
            int listSize = jfx_node.getChildren().size();
            for (int i = 0; i < numBits; i++) {
                if (square_led && !(leds[i] instanceof Rectangle))
                    listSize = replaceShape(new Rectangle(), i, listSize);
                else if (!square_led && !(leds[i] instanceof Ellipse))
                   listSize = replaceShape(new Ellipse(), i, listSize);
                else if (i >= listSize)
                    jfx_node.getChildren().add(leds[i]);
            }
            if (listSize > numBits)
                jfx_node.getChildren().remove(numBits, listSize);
            
            //adjust size and position of LEDs in pane
            final int w = model_widget.positionWidth().getValue();
            final int h = model_widget.positionHeight().getValue();
            jfx_node.setPrefSize(w, h);
            if (numBits > 0) {
                if (!square_led) {
                    final int d = Math.min(horizontal ? w/numBits : w, horizontal ? h : h/numBits);
                    for (int i = 0; i < numBits; i++) {
                        ((Ellipse)leds[i]).setCenterX(horizontal ? d/2 + i*w/numBits : d/2);
                        ((Ellipse)leds[i]).setCenterY(horizontal ? d/2 : d/2 + i*h/numBits);
                        ((Ellipse)leds[i]).setRadiusX(d/2);
                        ((Ellipse)leds[i]).setRadiusY(d/2);
                    }
                } else {
                    for (int i = 0; i < numBits; i++) {
                        ((Rectangle)leds[i]).setX(horizontal ? i*w/numBits : 0);
                        ((Rectangle)leds[i]).setY(horizontal ? 0 : i*h/numBits);
                        ((Rectangle)leds[i]).setWidth(horizontal ? w/numBits : w);
                        ((Rectangle)leds[i]).setHeight(horizontal ? h : h/numBits);
                    }
                }
            }
        }
        if (dirty_content.checkAndClear())
            for (int i = 0; i < numBits; i++)
                // Put highlight in top-left corner
                leds[i].setFill( //square_led ?
                                new LinearGradient(0, 0, .5, .5, true, CycleMethod.NO_CYCLE,
                                /*        new Stop(0, value_colors[i].interpolate(Color.WHITESMOKE, 0.8)),
                                        new Stop(1, value_colors[i])) :
                                new RadialGradient(0, 0, 0.3, 0.3, 0.4, true, CycleMethod.NO_CYCLE,*/
                                        new Stop(0, value_colors[i].interpolate(Color.WHITESMOKE, 0.8)),
                                        new Stop(1, value_colors[i])) );
    }
}