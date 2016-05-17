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
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
public class ByteMonitorRepresentation extends RegionBaseRepresentation<Pane, ByteMonitorWidget>
{
	//TODO: add these properties
    int startBit = 0;
    int numBits = 8;
    boolean bitReverse = false;
    boolean horizontal = true;
    boolean square_led = false;
    
    private final DirtyFlag dirty_size = new DirtyFlag();
    protected final DirtyFlag dirty_content = new DirtyFlag();

    protected volatile Color[] colors = new Color[2];

    protected volatile Color[] value_colors = new Color [numBits];
        //TODO: accommodate maxNumBits? also see createJFXNode
    
    /** LED Ellipses inside {@link Pane} for grouping and alignment */
    private Ellipse [] leds = new Ellipse [numBits];
    //TODO: add logic for square_led property
    
    @Override
    protected Pane createJFXNode() throws Exception {
        colors = createColors();
        //final Pane pane = new Pane();
        for (int i = 0; i < numBits; i++) { //or maxNumBits?
            value_colors[i] = colors[0];
            leds[i] = new Ellipse();
            leds[i].getStyleClass().add("led");
            //if (i < numBits)
                //pane.getChildren().add(reversed?0:length-1, leds[i]);
        }
        final Pane pane = new Pane(leds);
        //or = new Pane(Arrays.toList(leds).subList(startBit,startBit+numBits)), or somesuch
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
        //TODO: add logic for bitReverse property
        for (int i = 0; i < numBits; i++) {
            colorIndices[i] = ( number & (1 << (startBit+i)) );
        }
        //modeled off of:
        /*int number = VTypeUtil.getValueNumber(value).intValue();
        final int bit = model_widget.bit().getValue();
        if (bit >= 0)
            number &= (1 << bit);
        return number == 0 ? 0 : 1;*/
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
        //model_widget.displayNumBits().addUntypedPropertyListener(this::configChanged);
    }
    
    private void sizeChanged(final WidgetProperty<Integer> property, final Integer old_value, final Integer new_value)
    {
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }
    
    /** Invoked when bit/color properties changed
     *  and current colors need to be re-evaluated
     *  @param property Ignored
     *  @param old_value Ignored
     *  @param new_value Ignored
     */
    protected void configChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        //TODO: check on numBits here
        colors = createColors();
        contentChanged(model_widget.runtimeValue(), null, model_widget.runtimeValue().getValue());
    }

    private void contentChanged(final WidgetProperty<VType> property, final VType old_value, final VType new_value)
    {
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

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
        {
            final int w = model_widget.positionWidth().getValue();
            final int h = model_widget.positionHeight().getValue();
            jfx_node.setPrefSize(w, h);
            //if not rectangular {
                final int d = Math.min(horizontal ? w/numBits : w, horizontal ? h : h/numBits);
                for (int i = 0; i < numBits; i++) {
                	leds[i].setCenterX(horizontal ? d/2 + i*w/numBits : d/2);
                	leds[i].setCenterY(horizontal ? d/2 : d/2 + i*h/numBits);
                	leds[i].setRadiusX(d/2);
                	leds[i].setRadiusY(d/2);
                }
            //}
        }
        if (dirty_content.checkAndClear())
        	for (int i = 0; i < numBits; i++)
                leds[i].setFill(
                    // Put highlight in top-left corner, about 0.2 wide,
                    // relative to actual size of LED
                    new RadialGradient(0, 0, 0.3, 0.3, 0.4, true, CycleMethod.NO_CYCLE,
                                       new Stop(0, value_colors[i].interpolate(Color.WHITESMOKE, 0.8)),
                                       new Stop(1, value_colors[i])));
    }
}
