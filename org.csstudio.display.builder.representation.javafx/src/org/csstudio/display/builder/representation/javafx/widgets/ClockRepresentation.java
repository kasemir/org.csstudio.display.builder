/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx.widgets;


import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.ClockWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.ClockBuilder;
import javafx.scene.paint.Color;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 18 Jan 2017
 */
public class ClockRepresentation extends RegionBaseRepresentation<Clock, ClockWidget> {

    private final DirtyFlag dirtyBehavior = new DirtyFlag();
    private final DirtyFlag dirtyGeometry = new DirtyFlag();
    private final DirtyFlag dirtyLook = new DirtyFlag();

    @Override
    public void updateChanges ( ) {

        if ( dirtyBehavior.checkAndClear() ) {
            jfx_node.setDiscreteHours(model_widget.propDiscreteHours().getValue());
            jfx_node.setDiscreteMinutes(model_widget.propDiscreteMinutes().getValue());
            jfx_node.setDiscreteSeconds(model_widget.propDiscreteSeconds().getValue());
            jfx_node.setRunning(model_widget.propRunning().getValue());
        }

        if ( dirtyGeometry.checkAndClear() ) {
            jfx_node.setVisible(model_widget.propVisible().getValue());
            jfx_node.setLayoutX(model_widget.propX().getValue());
            jfx_node.setLayoutY(model_widget.propY().getValue());
            jfx_node.setPrefWidth(model_widget.propWidth().getValue());
            jfx_node.setPrefHeight(model_widget.propHeight().getValue());
        }

        if ( dirtyLook.checkAndClear() ) {
//            jfx_node.setSkinType(model_widget.propSkin().getValue().skinType());
            jfx_node.setBackgroundPaint(model_widget.propTransparent().getValue() ? Color.TRANSPARENT : JFXUtil.convert(model_widget.propBackgroundColor().getValue()));
            jfx_node.setBorderPaint(JFXUtil.convert(model_widget.propBorderColor().getValue()));
            jfx_node.setBorderWidth(model_widget.propBorderWidth().getValue());
            jfx_node.setDateColor(JFXUtil.convert(model_widget.propDateColor().getValue()));
            jfx_node.setDateVisible(model_widget.propDateVisible().getValue());
            jfx_node.setHourColor(JFXUtil.convert(model_widget.propHourColor().getValue()));
            jfx_node.setHourTickMarkColor(JFXUtil.convert(model_widget.propHourTickMarkColor().getValue()));
            jfx_node.setHourTickMarksVisible(model_widget.propHourTickMarkVisible().getValue());
            jfx_node.setKnobColor(JFXUtil.convert(model_widget.propKnobColor().getValue()));
            jfx_node.setMinuteColor(JFXUtil.convert(model_widget.propMinuteColor().getValue()));
            jfx_node.setMinuteTickMarkColor(JFXUtil.convert(model_widget.propMinuteTickMarkColor().getValue()));
            jfx_node.setMinuteTickMarksVisible(model_widget.propMinuteTickMarkVisible().getValue());
            jfx_node.setSecondColor(JFXUtil.convert(model_widget.propSecondColor().getValue()));
            jfx_node.setSecondsVisible(model_widget.propSecondVisible().getValue());
        }

    }

    @Override
    protected Clock createJFXNode ( ) throws Exception {

        Clock clock = ClockBuilder.create()
                                  .skinType(model_widget.propSkin().getValue().skinType())
                                  .prefHeight(model_widget.propHeight().getValue())
                                  .prefWidth(model_widget.propWidth().getValue())
                                  .backgroundPaint(model_widget.propTransparent().getValue() ? Color.TRANSPARENT : JFXUtil.convert(model_widget.propBackgroundColor().getValue()))
                                  .borderPaint(JFXUtil.convert(model_widget.propBorderColor().getValue()))
                                  .borderWidth(model_widget.propBorderWidth().getValue())
                                  .dateColor(JFXUtil.convert(model_widget.propDateColor().getValue()))
                                  .dateVisible(model_widget.propDateVisible().getValue())
                                  .discreteHours(model_widget.propDiscreteHours().getValue())
                                  .discreteMinutes(model_widget.propDiscreteMinutes().getValue())
                                  .discreteSeconds(model_widget.propDiscreteSeconds().getValue())
                                  .hourColor(JFXUtil.convert(model_widget.propHourColor().getValue()))
                                  .hourTickMarkColor(JFXUtil.convert(model_widget.propHourTickMarkColor().getValue()))
                                  .hourTickMarksVisible(model_widget.propHourTickMarkVisible().getValue())
                                  .knobColor(JFXUtil.convert(model_widget.propKnobColor().getValue()))
                                  .minuteColor(JFXUtil.convert(model_widget.propMinuteColor().getValue()))
                                  .minuteTickMarkColor(JFXUtil.convert(model_widget.propMinuteTickMarkColor().getValue()))
                                  .minuteTickMarksVisible(model_widget.propMinuteTickMarkVisible().getValue())
                                  .running(model_widget.propRunning().getValue())
                                  .secondColor(JFXUtil.convert(model_widget.propSecondColor().getValue()))
                                  .secondsVisible(model_widget.propSecondVisible().getValue())
                                  .build();

        return clock;

    }

    @Override
    protected void registerListeners ( ) {

        model_widget.propVisible().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propX().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propY().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propWidth().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::geometryChanged);

        model_widget.propBackgroundColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propBorderColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propBorderWidth().addUntypedPropertyListener(this::lookChanged);
        model_widget.propDateColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propDateVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propHourColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propHourTickMarkColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propHourTickMarkVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propKnobColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propMinuteColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propMinuteTickMarkColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propMinuteTickMarkVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propSecondColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propSecondVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propSkin().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTransparent().addUntypedPropertyListener(this::lookChanged);

        model_widget.propDiscreteHours().addUntypedPropertyListener(this::behaviorChanged);
        model_widget.propDiscreteMinutes().addUntypedPropertyListener(this::behaviorChanged);
        model_widget.propDiscreteSeconds().addUntypedPropertyListener(this::behaviorChanged);
        model_widget.propRunning().addUntypedPropertyListener(this::behaviorChanged);

    }

    private void behaviorChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyBehavior.mark();
        toolkit.scheduleUpdate(this);
    }

    private void geometryChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyGeometry.mark();
        toolkit.scheduleUpdate(this);
    }

    private void lookChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyLook.mark();
        toolkit.scheduleUpdate(this);
    }

}
