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
import org.csstudio.display.builder.model.widgets.DigitalClockWidget.Design;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.Clock.ClockSkinType;
import eu.hansolo.medusa.LcdDesign;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 23 Jan 2017
 */
@SuppressWarnings("nls")
public class DigitalClockRepresentation extends BasicClockRepresentation<DigitalClockWidget> {

    private final DirtyFlag dirtyLook = new DirtyFlag();

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        Object value;

        if ( dirtyLook.checkAndClear() ) {

            value = LcdDesign.valueOf(model_widget.propLcdDesign().getValue().name());

            if ( !Objects.equals(value, jfx_node.getLcdDesign()) ) {
                jfx_node.setLcdDesign((LcdDesign) value);
            }

            value = model_widget.propLcdCrystalEnabled().getValue();

            if ( !Objects.equals(value, jfx_node.isLcdCrystalEnabled()) ) {
                jfx_node.setLcdCrystalEnabled((boolean) value);
            }

        }

    }

    @Override
    protected Clock createJFXNode ( ) throws Exception {

        Clock clock = super.createJFXNode();

        clock.setSkinType(ClockSkinType.LCD);
        clock.setLcdDesign(LcdDesign.valueOf(model_widget.propLcdDesign().getValue().name()));
        clock.setLcdCrystalEnabled(model_widget.propLcdCrystalEnabled().getValue());
        clock.setTextVisible(true);

        clock.lcdCrystalEnabledProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propLcdCrystalEnabled().getValue()) ) {
                model_widget.propLcdCrystalEnabled().setValue(n);
            }
        });
        clock.lcdDesignProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propLcdDesign().getValue()) ) {
                model_widget.propLcdDesign().setValue(Design.valueOf(n.name()));
            }
        });

        return clock;

    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propLcdCrystalEnabled().addUntypedPropertyListener(this::lookChanged);
        model_widget.propLcdDesign().addUntypedPropertyListener(this::lookChanged);

    }

    private void lookChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyLook.mark();
        toolkit.scheduleUpdate(this);
    }

}
