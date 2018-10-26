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
import java.util.concurrent.TimeUnit;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.MeterWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.KnobType;
import eu.hansolo.medusa.Gauge.NeedleShape;
import eu.hansolo.medusa.Gauge.NeedleSize;
import eu.hansolo.medusa.Gauge.NeedleType;
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickMarkType;
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
                    case GAUGE:
                        switch ( model_widget.propScaleDirection().getValue() ) {
                            case CLOCKWISE:
                                jfx_node.setStartAngle(320);
                                break;
                            case COUNTER_CLOCKWISE:
                                jfx_node.setStartAngle(40);
                                break;
                        }
                        break;
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

            value = JFXUtil.convert(model_widget.propKnobColor().getValue());

            if ( !Objects.equals(value, jfx_node.getKnobColor()) ) {
                jfx_node.setKnobColor((Color) value);
            }

            value = model_widget.propKnobPosition().getValue();

            if ( !Objects.equals(value,  knobPosition) ) {

                knobPosition = (MeterWidget.KnobPosition) value;

                jfx_node.setKnobPosition(Pos.valueOf(knobPosition.name()));

            }

            value = model_widget.propMajorTickVisible().getValue();

            if ( !Objects.equals(value, jfx_node.getMajorTickMarksVisible()) ) {
                jfx_node.setMajorTickMarksVisible((boolean) value);
            }

            value = model_widget.propMediumTickVisible().getValue();

            if ( !Objects.equals(value, jfx_node.getMediumTickMarksVisible()) ) {
                jfx_node.setMediumTickMarksVisible((boolean) value);
            }

            value = model_widget.propMinorTickVisible().getValue();

            if ( !Objects.equals(value, jfx_node.getMinorTickMarksVisible()) ) {
                jfx_node.setMinorTickMarksVisible((boolean) value);
            }

            value = JFXUtil.convert(model_widget.propNeedleColor().getValue());

            if ( !Objects.equals(value, jfx_node.getNeedleColor()) ) {
                jfx_node.setNeedleColor((Color) value);
                jfx_node.setNeedleBorderColor(((Color) value).darker());
            }

            value = model_widget.propOnlyExtremaVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isOnlyFirstAndLastTickLabelVisible()) ) {
                jfx_node.setOnlyFirstAndLastTickLabelVisible((boolean) value);
            }

            value = ScaleDirection.valueOf(model_widget.propScaleDirection().getValue().name());

            if ( !Objects.equals(value, jfx_node.getScaleDirection()) ) {

                jfx_node.setScaleDirection((ScaleDirection) value);

                switch ( model_widget.propSkin().getValue() ) {
                    case GAUGE:
                        switch ( model_widget.propScaleDirection().getValue() ) {
                            case CLOCKWISE:
                                jfx_node.setStartAngle(320);
                                break;
                            case COUNTER_CLOCKWISE:
                                jfx_node.setStartAngle(40);
                                break;
                        }
                        break;
                    case THREE_QUARTERS:
                        jfx_node.setAngleRange(270);
                        jfx_node.setStartAngle(0);
                        break;
                    default:
                        break;
                }

            }

            value = model_widget.propTickLabelDecimals().getValue();

            if ( !Objects.equals(value, jfx_node.getTickLabelDecimals()) ) {
                jfx_node.setTickLabelDecimals((int) value);
            }

            value = model_widget.propTickLabelsVisible().getValue();

            if ( !Objects.equals(value, jfx_node.getTickLabelsVisible()) ) {
                jfx_node.setTickLabelsVisible((boolean) value);
            }

            value = model_widget.propTickMarkRingVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isTickMarkRingVisible()) ) {
                jfx_node.setTickMarkRingVisible((boolean) value);
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
        jfx_node.setHighlightSections(zonesHighlight);
        jfx_node.setKnobColor(JFXUtil.convert(model_widget.propKnobColor().getValue()));
        jfx_node.setKnobPosition(Pos.valueOf(knobPosition.name()));
        jfx_node.setKnobType(KnobType.STANDARD);
        jfx_node.setKnobVisible(true);
        jfx_node.setMajorTickMarkLengthFactor(0.515);
        jfx_node.setMajorTickMarkType(TickMarkType.LINE);
        jfx_node.setMajorTickMarksVisible(model_widget.propMajorTickVisible().getValue());
        jfx_node.setMediumTickMarkLengthFactor(0.475);
        jfx_node.setMediumTickMarkType(TickMarkType.LINE);
        jfx_node.setMediumTickMarksVisible(model_widget.propMediumTickVisible().getValue());
        jfx_node.setMinorTickMarkType(TickMarkType.LINE);
        jfx_node.setMinorTickMarksVisible(model_widget.propMinorTickVisible().getValue());
        jfx_node.setNeedleBorderColor(JFXUtil.convert(model_widget.propNeedleColor().getValue()).darker());
        jfx_node.setNeedleColor(JFXUtil.convert(model_widget.propNeedleColor().getValue()));
        jfx_node.setNeedleShape(NeedleShape.ANGLED);
        jfx_node.setNeedleSize(NeedleSize.THIN);
        jfx_node.setNeedleType(NeedleType.STANDARD);
        jfx_node.setOnlyFirstAndLastTickLabelVisible(model_widget.propOnlyExtremaVisible().getValue());
        jfx_node.setScaleDirection(ScaleDirection.valueOf(model_widget.propScaleDirection().getValue().name()));
        jfx_node.setShadowsEnabled(true);
        jfx_node.setThresholdVisible(false);
        jfx_node.setTickLabelDecimals(model_widget.propTickLabelDecimals().getValue());
        jfx_node.setTickLabelsVisible(model_widget.propTickLabelsVisible().getValue());
        jfx_node.setTickMarkRingVisible(model_widget.propTickMarkRingVisible().getValue());

        switch ( skin ) {
            case HORIZONTAL:
                knobPosition = MeterWidget.KnobPosition.BOTTOM_CENTER;
                break;
            case QUARTER:
                knobPosition = MeterWidget.KnobPosition.BOTTOM_RIGHT;
                break;
            case THREE_QUARTERS:
                knobPosition = MeterWidget.KnobPosition.CENTER;
                jfx_node.setAngleRange(270);
                jfx_node.setStartAngle(0);
                break;
            case VERTICAL:
                knobPosition = MeterWidget.KnobPosition.CENTER_RIGHT;
                break;
            case GAUGE:
                knobPosition = MeterWidget.KnobPosition.CENTER;
                switch ( model_widget.propScaleDirection().getValue() ) {
                    case CLOCKWISE:
                        jfx_node.setStartAngle(320);
                        break;
                    case COUNTER_CLOCKWISE:
                        jfx_node.setStartAngle(40);
                        break;
                }
                break;
            default:
                knobPosition = MeterWidget.KnobPosition.CENTER;
                break;
        }

        model_widget.propKnobPosition().setValue(knobPosition);
        jfx_node.setKnobPosition(Pos.valueOf(knobPosition.name()));

    }

    @Override
    protected Gauge createJFXNode ( ) throws Exception {

        try {

            knobPosition = model_widget.propKnobPosition().getValue();
            skin = model_widget.propSkin().getValue();

            Gauge gauge = super.createJFXNode();

            gauge.setKnobPosition(Pos.valueOf(knobPosition.name()));
            gauge.setKnobType(KnobType.STANDARD);
            gauge.setKnobVisible(true);
            gauge.setMajorTickMarkLengthFactor(0.515);
            gauge.setMajorTickMarkType(TickMarkType.LINE);
            gauge.setMediumTickMarkLengthFactor(0.475);
            gauge.setMediumTickMarkType(TickMarkType.LINE);
            gauge.setMinorTickMarkType(TickMarkType.LINE);
            gauge.setNeedleShape(NeedleShape.ANGLED);
            gauge.setNeedleSize(NeedleSize.THIN);
            gauge.setNeedleType(NeedleType.STANDARD);
            gauge.setShadowsEnabled(true);
            gauge.setThresholdVisible(false);

            return gauge;

        } finally {
            dirtyLimits.mark();
            dirtyLook.mark();
            toolkit.schedule( ( ) -> {
                if ( jfx_node != null ) {
                    //  The next 2 lines necessary because of a Medusa problem.
                    jfx_node.setAutoScale(!jfx_node.isAutoScale());
                    jfx_node.setAutoScale(!jfx_node.isAutoScale());
                }
                valueChanged(null, null, null);
            }, 77 + (long) ( 34.0 * Math.random() ), TimeUnit.MILLISECONDS);
        }

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
    protected Gauge.SkinType getSkin() {

        Gauge.SkinType skinType;

        switch ( skin ) {
            case THREE_QUARTERS:
                skinType = Gauge.SkinType.GAUGE;
                break;
            default:
                skinType = Gauge.SkinType.valueOf(skin.name());
                break;
        }

        return skinType;

    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propAverage().addUntypedPropertyListener(this::lookChanged);
        model_widget.propAverageColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propAverageSamples().addUntypedPropertyListener(this::lookChanged);
        model_widget.propKnobColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propKnobPosition().addUntypedPropertyListener(this::lookChanged);
        model_widget.propMajorTickVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propMediumTickVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propMinorTickVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propNeedleColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propOnlyExtremaVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propScaleDirection().addUntypedPropertyListener(this::lookChanged);
        model_widget.propSkin().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTickLabelDecimals().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTickLabelsVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTickMarkRingVisible().addUntypedPropertyListener(this::lookChanged);

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
