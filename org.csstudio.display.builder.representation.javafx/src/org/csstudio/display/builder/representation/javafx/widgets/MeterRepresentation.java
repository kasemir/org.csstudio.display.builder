/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx.widgets;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.MeterWidget;
import org.csstudio.display.builder.model.widgets.MeterWidget.Skin;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.VType;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Section;
import javafx.geometry.Orientation;
import javafx.scene.paint.Color;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 25 Jan 2017
 */
public class MeterRepresentation extends RegionBaseRepresentation<Gauge, MeterWidget> {

    private static final Color MINOR_COLOR       = JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_MINOR));
    private static final Color MINOR_COLOR_LIGHT = MINOR_COLOR.deriveColor(0.0, 1.0, 1.0, 0.2);
    private static final Color MAJOR_COLOR       = JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_MAJOR));
    private static final Color MAJOR_COLOR_LIGHT = MAJOR_COLOR.deriveColor(0.0, 1.0, 1.0, 0.2);

    private final DirtyFlag     dirtyBehavior = new DirtyFlag();
    private final DirtyFlag     dirtyGeometry = new DirtyFlag();
    private final DirtyFlag     dirtyLimits   = new DirtyFlag();
    private final DirtyFlag     dirtyLook     = new DirtyFlag();
    private final DirtyFlag     dirtyValue    = new DirtyFlag();
    private volatile double     high          = Double.NaN;
    private volatile Section    highZone      = null;
    private volatile double     hihi          = Double.NaN;
    private volatile Section    hihiZone      = null;
    private volatile double     lolo          = Double.NaN;
    private volatile Section    loloZone      = null;
    private volatile double     low           = Double.NaN;
    private volatile Section    lowZone       = null;
    private volatile double     max           = 100.0;
    private volatile double     min           = 0.0;
    private MeterWidget.Skin    skin          = null;
    private final AtomicBoolean updatingValue = new AtomicBoolean(false);
    private final List<Section> zones         = new ArrayList<>(4);

    @Override
    public void updateChanges ( ) {

        Object value;

        if ( dirtyBehavior.checkAndClear() ) {

            value = model_widget.propAnimated().getValue();

            if ( !Objects.equals(value, jfx_node.isAnimated()) ) {
                jfx_node.setAnimated((boolean) value);
            }

            value = model_widget.propHighlightZones().getValue();

            if ( !Objects.equals(value, jfx_node.isHighlightSections()) ) {
                jfx_node.setHighlightSections((boolean) value);
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

            value = model_widget.propSkin().getValue();

            if ( !Objects.equals(value, skin) ) {

                skin = (Skin) value;

                final Gauge.SkinType skinType;

                switch ( skin ) {
                    case THREE_QUARTERS:
                        skinType = Gauge.SkinType.GAUGE;
                        break;
                    case LINEAR_H:
                    case LINEAR_V:
                        skinType = Gauge.SkinType.LINEAR;
                        break;
                    default:
                        skinType = Gauge.SkinType.valueOf(skin.name());
                        break;
                }

                jfx_node.setSkinType(skinType);
                jfx_node.setPrefWidth(model_widget.propWidth().getValue());
                jfx_node.setPrefHeight(model_widget.propHeight().getValue());

                switch ( skin ) {
                    case THREE_QUARTERS:
                        jfx_node.setAngleRange(270);
                        jfx_node.setStartAngle(0);
                        break;
                    case LINEAR_H:
                        jfx_node.setOrientation(Orientation.HORIZONTAL);
                        break;
                    case LINEAR_V:
                        jfx_node.setOrientation(Orientation.VERTICAL);
                        break;
                    default:
                        break;
                }

            }

            value = model_widget.propTitle().getValue();

            if ( !Objects.equals(value, jfx_node.getTitle()) ) {
                jfx_node.setTitle((String) value);
            }

            value = JFXUtil.convert(model_widget.propTitleColor().getValue());

            if ( !Objects.equals(value, jfx_node.getTitleColor()) ) {
                jfx_node.setTitleColor((Color) value);
            }

        }

        if ( dirtyLimits.checkAndClear() ) {

        }

        if ( dirtyValue.checkAndClear() && updatingValue.compareAndSet(false, true) ) {
            try {

                final VType vtype = model_widget.runtimePropValue().getValue();
                double newval = VTypeUtil.getValueNumber(vtype).doubleValue();

                if ( newval < min ) {
                    newval = min;
                } else if ( newval > max ) {
                    newval = max;
                }

                jfx_node.setValue(newval);

            } finally {
                updatingValue.set(false);
            }

        }

    }

    @Override
    protected Gauge createJFXNode ( ) throws Exception {

        updateLimits();

        skin = model_widget.propSkin().getValue();

        final Gauge.SkinType skinType;

        switch ( skin ) {
            case THREE_QUARTERS:
                skinType = Gauge.SkinType.GAUGE;
                break;
            case LINEAR_H:
            case LINEAR_V:
                skinType = Gauge.SkinType.LINEAR;
                break;
            default:
                skinType = Gauge.SkinType.valueOf(skin.name());
                break;
        }

        Gauge gauge = GaugeBuilder.create()
                                  .skinType(skinType)
                                  .prefHeight(model_widget.propHeight().getValue())
                                  .prefWidth(model_widget.propWidth().getValue())
                                  //--------------------------------------------------------
                                  //  Previous properties must be set first.
                                  //--------------------------------------------------------
                                  .animated(model_widget.propAnimated().getValue())
                                  .highlightSections(model_widget.propHighlightZones().getValue())
                                  .sections(zones.toArray(new Section[zones.size()]))
                                  .sectionsVisible(model_widget.propShowLoLo().getValue() || model_widget.propShowLow().getValue() || model_widget.propShowHigh().getValue() || model_widget.propShowHiHi().getValue())
                                  .title(model_widget.propTitle().getValue())
                                  .titleColor(JFXUtil.convert(model_widget.propTitleColor().getValue()))
                                  .value(( max + min ) / 2.0)
                                  .build();

        switch ( skin ) {
            case THREE_QUARTERS:
                gauge.setAngleRange(270);
                gauge.setStartAngle(0);
                break;
            case LINEAR_H:
                gauge.setOrientation(Orientation.HORIZONTAL);
                break;
            case LINEAR_V:
                gauge.setOrientation(Orientation.VERTICAL);
                break;
            default:
                break;
        }

        gauge.animatedProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propAnimated().getValue()) ) {
                model_widget.propAnimated().setValue(n);
            }
        });
        gauge.layoutXProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propX().getValue()) ) {
                model_widget.propX().setValue(n.intValue());
            }
        });
        gauge.layoutYProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propY().getValue()) ) {
                model_widget.propY().setValue(n.intValue());
            }
        });
        gauge.prefHeightProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propHeight().getValue()) ) {
                model_widget.propHeight().setValue(n.intValue());
            }
        });
        gauge.highlightSectionsProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propHighlightZones().getValue()) ) {
                model_widget.propHighlightZones().setValue(n);
            }
        });
        gauge.prefWidthProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propWidth().getValue()) ) {
                model_widget.propWidth().setValue(n.intValue());
            }
        });
        gauge.titleProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propTitle().getValue()) ) {
                model_widget.propTitle().setValue(n);
            }
        });
        gauge.titleColorProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, JFXUtil.convert(model_widget.propTitleColor().getValue())) ) {
                model_widget.propTitleColor().setValue(JFXUtil.convert(n));
            }
        });

        return gauge;

    }

    @Override
    protected void registerListeners ( ) {

        model_widget.propAnimated().addUntypedPropertyListener(this::behaviorChanged);
        model_widget.propHighlightZones().addUntypedPropertyListener(this::behaviorChanged);

        model_widget.propVisible().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propX().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propY().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propWidth().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::geometryChanged);

        model_widget.propSkin().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTitle().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTitleColor().addUntypedPropertyListener(this::lookChanged);

        model_widget.propLevelHiHi().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propLevelHight().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propLevelLoLo().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propLevelLow().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propLimitsFromPV().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propShowHiHi().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propShowHigh().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propShowLoLo().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propShowLow().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propMaximum().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propMinimum().addUntypedPropertyListener(this::limitsChanged);

        if ( toolkit.isEditMode() ) {
            dirtyValue.checkAndClear();
        } else {
            model_widget.runtimePropValue().addPropertyListener(this::valueChanged);
        }

    }

    private void behaviorChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyBehavior.mark();
        toolkit.scheduleUpdate(this);
    }

    private void geometryChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyGeometry.mark();
        toolkit.scheduleUpdate(this);
    }

    private void limitsChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyLimits.mark();
        updateLimits();
        toolkit.scheduleUpdate(this);
    }

    private void lookChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyLook.mark();
        toolkit.scheduleUpdate(this);
    }

    private void updateLimits ( ) {

    }

    private void valueChanged ( final WidgetProperty<? extends VType> property, final VType old_value, final VType new_value ) {

        if ( model_widget.propLimitsFromPV().getValue() ) {
            limitsChanged(null, null, null);
        }

        dirtyValue.mark();
        toolkit.scheduleUpdate(this);

    }

}
