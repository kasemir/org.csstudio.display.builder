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
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.DigitalClockWidget;
import org.csstudio.display.builder.model.widgets.DigitalClockWidget.Design;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.Clock.ClockSkinType;
import eu.hansolo.medusa.LcdDesign;
import eu.hansolo.medusa.LcdFont;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 23 Jan 2017
 */
@SuppressWarnings("nls")
public class DigitalClockRepresentation extends BaseClockRepresentation<DigitalClockWidget> {

    private final DirtyFlag                     dirtyLook           = new DirtyFlag();
    private final UntypedWidgetPropertyListener lookChangedListener = this::lookChanged;

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        Object value;

        if ( dirtyLook.checkAndClear() ) {

            value = model_widget.propLcdCrystalEnabled().getValue();

            if ( !Objects.equals(value, jfx_node.isLcdCrystalEnabled()) ) {
                jfx_node.setLcdCrystalEnabled((boolean) value);
            }

            value = LcdDesign.valueOf(model_widget.propLcdDesign().getValue().name());

            if ( !Objects.equals(value, jfx_node.getLcdDesign()) ) {
                jfx_node.setLcdDesign((LcdDesign) value);
            }

            value = LcdFont.valueOf(model_widget.propLcdFont().getValue().name());

            if ( !Objects.equals(value, jfx_node.getLcdFont()) ) {
                jfx_node.setLcdFont((LcdFont) value);
            }

        }

    }

    @Override
    protected Clock createJFXNode ( ) throws Exception {

        Clock clock = super.createJFXNode(ClockSkinType.LCD);

        clock.setLcdCrystalEnabled(model_widget.propLcdCrystalEnabled().getValue());
        clock.setLcdDesign(LcdDesign.valueOf(model_widget.propLcdDesign().getValue().name()));
        clock.setLcdFont(LcdFont.valueOf(model_widget.propLcdFont().getValue().name()));
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

        model_widget.propLcdCrystalEnabled().addUntypedPropertyListener(lookChangedListener);
        model_widget.propLcdDesign().addUntypedPropertyListener(lookChangedListener);
        model_widget.propLcdFont().addUntypedPropertyListener(lookChangedListener);

    }

    @Override
    protected void unregisterListeners ( ) {

        model_widget.propLcdCrystalEnabled().removePropertyListener(lookChangedListener);
        model_widget.propLcdDesign().removePropertyListener(lookChangedListener);
        model_widget.propLcdFont().removePropertyListener(lookChangedListener);

        super.unregisterListeners();

    }

    private void lookChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyLook.mark();
        toolkit.scheduleUpdate(this);
    }

}
