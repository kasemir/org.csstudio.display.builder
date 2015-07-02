/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionHeight;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionY;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;
import static org.csstudio.display.builder.model.widgets.LEDWidget.off_color;
import static org.csstudio.display.builder.model.widgets.LEDWidget.on_color;

import java.beans.PropertyChangeEvent;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.LEDWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.epics.vtype.VType;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class LEDRepresentation extends JFXBaseRepresentation<Ellipse, LEDWidget>
{
    private final DirtyFlag dirty_position = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();

    private volatile Color[] colors = new Color[0];

    private volatile Color value_color = Color.VIOLET;

    public LEDRepresentation(final ToolkitRepresentation<Group, Node> toolkit,
                             final LEDWidget model_widget)
    {
        super(toolkit, model_widget);
    }

    @Override
    public Ellipse createJFXNode() throws Exception
    {
        createColors();

        final Ellipse led = new Ellipse();
        led.getStyleClass().add("led");

        return led;
    }

    private void createColors()
    {
        colors = new Color[]
        {
            JFXUtil.convert(model_widget.offColor().getValue()),
            JFXUtil.convert(model_widget.onColor().getValue())
        };
    }

    @Override
    protected void registerListeners()
    {
        // NOT calling  super.registerListeners()
        // because Ellipse uses center instead of top-left X/Y
        model_widget.addPropertyListener(positionX,      this::positionChanged);
        model_widget.addPropertyListener(positionY,      this::positionChanged);
        model_widget.addPropertyListener(positionWidth,  this::positionChanged);
        model_widget.addPropertyListener(positionHeight, this::positionChanged);
        model_widget.addPropertyListener(off_color,      this::configChanged);
        model_widget.addPropertyListener(on_color,       this::configChanged);
        model_widget.addPropertyListener(runtimeValue,   this::contentChanged);
    }

    private void positionChanged(final PropertyChangeEvent event)
    {
        dirty_position.mark();
        toolkit.scheduleUpdate(this);
    }

    private void configChanged(final PropertyChangeEvent event)
    {
        createColors();
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    private void contentChanged(final PropertyChangeEvent event)
    {
        int value_index = VTypeUtil.getValueNumber((VType)event.getNewValue()).intValue();
        if (value_index < 0)
            value_index = 0;
        if (value_index >= colors.length)
            value_index = colors.length-1;
        value_color = colors[value_index];

        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        if (dirty_position.checkAndClear())
        {
            final int x = model_widget.positionX().getValue();
            final int y = model_widget.positionY().getValue();
            final int w = model_widget.positionWidth().getValue();
            final int h = model_widget.positionHeight().getValue();
            jfx_node.setCenterX(x + w/2);
            jfx_node.setCenterY(y + h/2);
            jfx_node.setRadiusX(w/2);
            jfx_node.setRadiusY(h/2);
        }
        if (dirty_content.checkAndClear())
            jfx_node.setFill(
                // Put highlight in top-left corner, about 0.2 wide,
                // relative to actual size of LED
                new RadialGradient(0, 0, 0.3, 0.3, 0.4, true, CycleMethod.NO_CYCLE,
                                   new Stop(0, value_color.interpolate(Color.WHITESMOKE, 0.8)),
                                   new Stop(1, value_color)));
    }
}
