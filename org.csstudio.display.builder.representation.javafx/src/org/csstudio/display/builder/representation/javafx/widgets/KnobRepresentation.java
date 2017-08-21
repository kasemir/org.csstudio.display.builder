/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.csstudio.display.builder.model.util.FormatOptionHandler;
import org.csstudio.display.builder.model.widgets.KnobWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.Display;
import org.diirt.vtype.ValueUtil;

import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import se.ess.knobs.Knob;
import se.ess.knobs.KnobBuilder;

/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 21 Aug 2017
 */
public class KnobRepresentation extends RegionBaseRepresentation<Knob, KnobWidget> {

    private volatile double               high          = Double.NaN;
    private volatile double               hihi          = Double.NaN;
    private volatile double               lolo          = Double.NaN;
    private volatile double               low           = Double.NaN;
    private volatile double               max           = 100.0;
    private volatile double               min           = 0.0;
    private final AtomicReference<String> unit          = new AtomicReference<>("");
    private final AtomicBoolean           updatingValue = new AtomicBoolean(false);

    @Override
    protected Knob createJFXNode ( ) throws Exception {

        updateLimits();

        unit.set(model_widget.propUnit().getValue());

        Knob knob = KnobBuilder.create()
                .prefHeight(model_widget.propHeight().getValue())
                .prefWidth(model_widget.propWidth().getValue())
                //--------------------------------------------------------
                //  Previous properties must be set first.
                //--------------------------------------------------------
                .backgroundColor(model_widget.propTransparent().getValue() ? Color.TRANSPARENT : JFXUtil.convert(model_widget.propBackgroundColor().getValue()))
                .color(JFXUtil.convert(model_widget.propColor().getValue()))
                .currentValueAlwaysVisible(model_widget.propValueVisible().getValue())
                .currentValueColor(JFXUtil.convert(model_widget.propValueColor().getValue()))
                .decimals(FormatOptionHandler.actualPrecision(model_widget.runtimePropValue().getValue(), model_widget.propPrecision().getValue()))
                .extremaVisible(model_widget.propExtremaVisible().getValue())
                .gradientStops(computeGradientStops())
                .indicatorColor(JFXUtil.convert(model_widget.propKnobColor().getValue()))
                .maxValue(max)
                .minValue(min)
                .tagColor(JFXUtil.convert(model_widget.propTagColor().getValue()))
                .tagVisible(model_widget.propTagVisible().getValue())
                .textColor(JFXUtil.convert(model_widget.propTextColor().getValue()))
                .unit(unit.get())
                .build();

        knob.setDisable(!model_widget.propEnabled().getValue());

        return knob;

    }

    private List<Stop> computeGradientStops ( ) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Updates, if required, the limits and zones.
     *
     * @return {@code true} is something changed and and UI update is required.
     */
    private boolean updateLimits ( ) {

        boolean somethingChanged = false;

        //  Model's values.
        double newMin = model_widget.propMinimum().getValue();
        double newMax = model_widget.propMaximum().getValue();
        double newLoLo = model_widget.propLevelLoLo().getValue();
        double newLow = model_widget.propLevelLow().getValue();
        double newHigh = model_widget.propLevelHigh().getValue();
        double newHiHi = model_widget.propLevelHiHi().getValue();

        if ( model_widget.propLimitsFromPV().getValue() ) {

            //  Try to get display range from PV.
            final Display display_info = ValueUtil.displayOf(model_widget.runtimePropValue().getValue());

            if ( display_info != null ) {
                newMin = display_info.getLowerCtrlLimit();
                newMax = display_info.getUpperCtrlLimit();
                newLoLo = display_info.getLowerAlarmLimit();
                newLow = display_info.getLowerWarningLimit();
                newHigh = display_info.getUpperWarningLimit();
                newHiHi = display_info.getUpperAlarmLimit();
            }

        }

        if ( !model_widget.propShowLoLo().getValue() ) {
            newLoLo = Double.NaN;
        }
        if ( !model_widget.propShowLow().getValue() ) {
            newLow = Double.NaN;
        }
        if ( !model_widget.propShowHigh().getValue() ) {
            newHigh = Double.NaN;
        }
        if ( !model_widget.propShowHiHi().getValue() ) {
            newHiHi = Double.NaN;
        }

        //  If invalid limits, fall back to 0..100 range.
        if ( !( Double.isNaN(newMin) || Double.isNaN(newMax) || newMin < newMax ) ) {
            newMin = 0.0;
            newMax = 100.0;
        }

        if ( Double.compare(min, newMin) != 0 ) {
            min = newMin;
            somethingChanged = true;
        }
        if ( Double.compare(max, newMax) != 0 ) {
            max = newMax;
            somethingChanged = true;
        }
        if ( Double.compare(lolo, newLoLo) != 0 ) {
            lolo = newLoLo;
            somethingChanged = true;
        }
        if ( Double.compare(low, newLow) != 0 ) {
            low = newLow;
            somethingChanged = true;
        }
        if ( Double.compare(high, newHigh) != 0 ) {
            high = newHigh;
            somethingChanged = true;
        }
        if ( Double.compare(hihi, newHiHi) != 0 ) {
            hihi = newHiHi;
            somethingChanged = true;
        }

        return somethingChanged;

    }

}
