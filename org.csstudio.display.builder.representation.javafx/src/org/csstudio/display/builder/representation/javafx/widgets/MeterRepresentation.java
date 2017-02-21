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
import org.csstudio.display.builder.model.widgets.MeterWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelLocation;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 25 Jan 2017
 */
public class MeterRepresentation extends BaseMeterRepresentation<MeterWidget> {

    private final DirtyFlag          dirtyLimits    = new DirtyFlag();
    private final DirtyFlag          dirtyLook      = new DirtyFlag();
    private MeterWidget.Skin         skin           = null;
    private MeterWidget.KnobPosition knobPosition   = null;
    private MeterWidget.KnobType     knobType       = null;
    private volatile boolean         zonesHighlight = true;

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        Object value;

        if ( dirtyLook.checkAndClear() ) {

            value = model_widget.propSkin().getValue();

            if ( !Objects.equals(value, skin) ) {

                skin = (MeterWidget.Skin) value;

                final Gauge.SkinType skinType;

                switch ( skin ) {
                    case THREE_QUARTERS:
                        skinType = Gauge.SkinType.GAUGE;
                        break;
                    default:
                        skinType = Gauge.SkinType.valueOf(skin.name());
                        break;
                }

                changeSkin(skinType);

                switch ( skin ) {
                    case THREE_QUARTERS:
                        jfx_node.setAngleRange(270);
                        jfx_node.setStartAngle(0);
                        break;
                    default:
                        break;
                }

            }

            value = model_widget.propAverage().getValue();

            if ( !Objects.equals(value, jfx_node.isAverageVisible()) || !Objects.equals(value, jfx_node.isAveragingEnabled()) ) {
                jfx_node.setAverageVisible((boolean) value);
                jfx_node.setAveragingEnabled((boolean) value);
            }

            value = JFXUtil.convert(model_widget.propAverageColor().getValue());

            if ( !Objects.equals(value, jfx_node.getAverageColor()) ) {
                jfx_node.setAverageColor((Color) value);
            }

            value = model_widget.propAverageSamples().getValue();

            if ( !Objects.equals(value, jfx_node.getAveragingPeriod()) ) {
                jfx_node.setAveragingPeriod((int) value);
            }

            value = JFXUtil.convert(model_widget.propForegroundColor().getValue());

            if ( !Objects.equals(value, jfx_node.getForegroundPaint()) ) {
                jfx_node.setForegroundPaint((Color) value);
            }

            value = JFXUtil.convert(model_widget.propKnobColor().getValue());

            if ( !Objects.equals(value, jfx_node.getKnobColor()) ) {
                jfx_node.setKnobColor((Color) value);
            }

            value = model_widget.propKnobPosition().getValue();

            if ( !Objects.equals(value,  knobPosition) ) {

                knobPosition = (MeterWidget.KnobPosition) value;

                jfx_node.setKnobPosition(Pos.valueOf(knobPosition.name()));

            }

            value = model_widget.propKnobType().getValue();

            if ( !Objects.equals(value,  knobType) ) {

                knobType = (MeterWidget.KnobType) value;

                jfx_node.setKnobType(Gauge.KnobType.valueOf(knobType.name()));

            }

        }

        if ( dirtyLimits.checkAndClear() ) {
            jfx_node.setHighlightSections(zonesHighlight);
            jfx_node.setSections(createZones());
        }

    }

    @Override
    protected void changeSkin ( final Gauge.SkinType skinType ) {

        super.changeSkin(skinType);

        jfx_node.setAverageColor(JFXUtil.convert(model_widget.propAverageColor().getValue()));
        jfx_node.setAverageVisible(model_widget.propAverage().getValue());
        jfx_node.setAveragingEnabled(model_widget.propAverage().getValue());
        jfx_node.setAveragingPeriod(model_widget.propAverageSamples().getValue());
        jfx_node.setForegroundPaint(JFXUtil.convert(model_widget.propForegroundColor().getValue()));
        jfx_node.setHighlightSections(zonesHighlight);
        jfx_node.setKnobColor(JFXUtil.convert(model_widget.propKnobColor().getValue()));
        jfx_node.setKnobPosition(Pos.valueOf(knobPosition.name()));
        jfx_node.setKnobType(Gauge.KnobType.valueOf(knobType.name()));
        jfx_node.setKnobVisible(true);
        jfx_node.setTickLabelLocation(TickLabelLocation.INSIDE);

    }

    @Override
    protected Gauge createJFXNode ( ) throws Exception {

        knobPosition = model_widget.propKnobPosition().getValue();
        knobType = model_widget.propKnobType().getValue();
        skin = model_widget.propSkin().getValue();

        Gauge.SkinType skinType;

        switch ( skin ) {
            case THREE_QUARTERS:
                skinType = Gauge.SkinType.GAUGE;
                break;
            default:
                skinType = Gauge.SkinType.valueOf(skin.name());
                break;
        }

        Gauge gauge = super.createJFXNode(skinType);

        gauge.setAverageColor(JFXUtil.convert(model_widget.propAverageColor().getValue()));
        gauge.setAverageVisible(model_widget.propAverage().getValue());
        gauge.setAveragingEnabled(model_widget.propAverage().getValue());
        gauge.setAveragingPeriod(model_widget.propAverageSamples().getValue());
        gauge.setForegroundPaint(JFXUtil.convert(model_widget.propForegroundColor().getValue()));
        gauge.setHighlightSections(zonesHighlight);
        gauge.setKnobColor(JFXUtil.convert(model_widget.propKnobColor().getValue()));
        gauge.setKnobPosition(Pos.valueOf(knobPosition.name()));
        gauge.setKnobType(Gauge.KnobType.valueOf(knobType.name()));
        gauge.setKnobVisible(true);
        gauge.setTickLabelLocation(TickLabelLocation.INSIDE);

        switch ( skin ) {
            case THREE_QUARTERS:
                gauge.setAngleRange(270);
                gauge.setStartAngle(0);
                break;
            default:
                break;
        }

        return gauge;

    }

    /**
     * Creates a new zone with the given parameters.
     *
     * @param start The zone's starting value.
     * @param end   The zone's ending value.
     * @param name  The zone's name.
     * @param color The zone's color.
     * @return A {@link Section} representing the created zone.
     */
    @Override
    protected Section createZone ( double start, double end, String name, Color color ) {
        return createZone(zonesHighlight, start, end, name, color);
    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propAverage().addUntypedPropertyListener(this::lookChanged);
        model_widget.propAverageColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propAverageSamples().addUntypedPropertyListener(this::lookChanged);
        model_widget.propForegroundColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propKnobColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propKnobPosition().addUntypedPropertyListener(this::lookChanged);
        model_widget.propKnobType().addUntypedPropertyListener(this::lookChanged);
        model_widget.propSkin().addUntypedPropertyListener(this::lookChanged);

        model_widget.propHighlightZones().addUntypedPropertyListener(this::limitsChanged);

    }

    @Override
    protected boolean updateLimits ( ) {

        boolean somethingChanged = super.updateLimits();

        //  Model's values.
        boolean newZonesHighlight = model_widget.propHighlightZones().getValue();

        if ( zonesHighlight != newZonesHighlight ) {
            zonesHighlight = newZonesHighlight;
            somethingChanged = true;
        }

        return somethingChanged;

    }

    private void limitsChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        if ( updateLimits() ) {
            dirtyLimits.mark();
            toolkit.scheduleUpdate(this);
        }
    }

    private void lookChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyLook.mark();
        toolkit.scheduleUpdate(this);
    }

}
