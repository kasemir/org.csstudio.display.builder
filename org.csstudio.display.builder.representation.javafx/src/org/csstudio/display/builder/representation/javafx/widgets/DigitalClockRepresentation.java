/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx.widgets;


import java.util.Objects;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.DigitalClockWidget;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.Clock.ClockSkinType;
import eu.hansolo.medusa.ClockBuilder;
import eu.hansolo.medusa.LcdDesign;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 23 Jan 2017
 */
public class DigitalClockRepresentation extends RegionBaseRepresentation<Clock, DigitalClockWidget> {

    private final DirtyFlag dirtyBehavior = new DirtyFlag();
    private final DirtyFlag dirtyGeometry = new DirtyFlag();
    private final DirtyFlag dirtyLook = new DirtyFlag();

    @Override
    public void updateChanges ( ) {

        Object value;

        if ( dirtyBehavior.checkAndClear() ) {

            value = model_widget.propRunning().getValue();

            if ( !Objects.equals(value, jfx_node.isRunning()) ) {
                jfx_node.setRunning((boolean) value);
            }

        }

        if ( dirtyGeometry.checkAndClear() ) {

            value = model_widget.propVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isVisible()) ) {
                jfx_node.setVisible((boolean) value);
            }

            jfx_node.setLayoutX(model_widget.propX().getValue());
            jfx_node.setLayoutY(model_widget.propY().getValue());
            jfx_node.setPrefWidth(model_widget.propWidth().getValue());
            jfx_node.setPrefHeight(model_widget.propHeight().getValue());

        }

        if ( dirtyLook.checkAndClear() ) {

            value = model_widget.propLcdDesign().getValue();

            if ( !Objects.equals(value, jfx_node.getLcdDesign()) ) {
                jfx_node.setLcdDesign((LcdDesign) value);
            }

            value = model_widget.propDateVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isDateVisible()) ) {
                jfx_node.setDateVisible((boolean) value);
            }

            value = model_widget.propLcdCrystalEnabled().getValue();

            if ( !Objects.equals(value, jfx_node.isLcdCrystalEnabled()) ) {
                jfx_node.setLcdCrystalEnabled((boolean) value);
            }

            value = model_widget.propSecondVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isSecondsVisible()) ) {
                jfx_node.setSecondsVisible((boolean) value);
            }

            value = model_widget.propShadowsEnabled().getValue();

            if ( !Objects.equals(value, jfx_node.getShadowsEnabled()) ) {
                jfx_node.setShadowsEnabled((boolean) value);
            }

            value = model_widget.propTitle().getValue();

            if ( !Objects.equals(value, jfx_node.getTitle()) ) {
                jfx_node.setTitle((String) value);
            }

            value = model_widget.propTitleVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isTitleVisible()) ) {
                jfx_node.setTitleVisible((boolean) value);
            }

        }

    }

    @Override
    protected Clock createJFXNode ( ) throws Exception {

        Clock clock = ClockBuilder.create()
                                  .skinType(ClockSkinType.LCD)
                                  .lcdDesign(model_widget.propLcdDesign().getValue())
                                  .prefHeight(model_widget.propHeight().getValue())
                                  .prefWidth(model_widget.propWidth().getValue())
                                  //--------------------------------------------------------
                                  //  Previous properties must be set first.
                                  //--------------------------------------------------------
                                  .dateVisible(model_widget.propDateVisible().getValue())
                                  .lcdCrystalEnabled(model_widget.propLcdCrystalEnabled().getValue())
                                  .secondsVisible(model_widget.propSecondVisible().getValue())
                                  .shadowsEnabled(model_widget.propShadowsEnabled().getValue())
                                  .textVisible(true)
                                  .title(model_widget.propTitle().getValue())
                                  .titleVisible(model_widget.propTitleVisible().getValue())
                                  .build();

        clock.dateVisibleProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propDateVisible().getValue()) ) {
                model_widget.propDateVisible().setValue(n);
            }
        });
        clock.lcdCrystalEnabledProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propLcdCrystalEnabled().getValue()) ) {
                model_widget.propLcdCrystalEnabled().setValue(n);
            }
        });
        clock.lcdDesignProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propLcdDesign().getValue()) ) {
                model_widget.propLcdDesign().setValue(n);
            }
        });
        clock.runningProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propRunning().getValue()) ) {
                model_widget.propRunning().setValue(n);
            }
        });
        clock.secondsVisibleProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propSecondVisible().getValue()) ) {
                model_widget.propSecondVisible().setValue(n);
            }
        });
        clock.shadowsEnabledProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propShadowsEnabled().getValue()) ) {
                model_widget.propShadowsEnabled().setValue(n);
            }
        });
        clock.titleProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propTitle().getValue()) ) {
                model_widget.propTitle().setValue(n);
            }
        });
        clock.titleVisibleProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propTitleVisible().getValue()) ) {
                model_widget.propTitleVisible().setValue(n);
            }
        });

        return clock;

    }

    @Override
    protected void registerListeners ( ) {

        model_widget.propVisible().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propX().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propY().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propWidth().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::geometryChanged);

        model_widget.propDateVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propLcdCrystalEnabled().addUntypedPropertyListener(this::lookChanged);
        model_widget.propLcdDesign().addUntypedPropertyListener(this::lookChanged);
        model_widget.propSecondVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propShadowsEnabled().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTitle().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTitleVisible().addUntypedPropertyListener(this::lookChanged);

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
