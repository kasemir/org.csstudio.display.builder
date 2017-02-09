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
import org.csstudio.display.builder.model.widgets.BaseGaugeWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.Display;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueUtil;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelLocation;
import javafx.scene.paint.Color;

/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 8 Feb 2017
 */
public abstract class BaseGaugeRepresentation<W extends BaseGaugeWidget> extends RegionBaseRepresentation<Gauge, W> {

    private static final Color MINOR_COLOR = JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_MINOR));
    private static final Color MAJOR_COLOR = JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_MAJOR));

    private final DirtyFlag     dirtyBehavior = new DirtyFlag();
    private final DirtyFlag     dirtyGeometry = new DirtyFlag();
    private final DirtyFlag     dirtyLimits   = new DirtyFlag();
    private final DirtyFlag     dirtyLook     = new DirtyFlag();
    private final DirtyFlag     dirtyValue    = new DirtyFlag();
    private volatile double     high          = Double.NaN;
    private volatile double     hihi          = Double.NaN;
    private volatile double     lolo          = Double.NaN;
    private volatile double     low           = Double.NaN;
    private volatile double     max           = 100.0;
    private volatile double     min           = 0.0;
    private final AtomicBoolean updatingValue = new AtomicBoolean(false);

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        Object value;

        if ( dirtyBehavior.checkAndClear() ) {

            value = model_widget.propAnimated().getValue();

            if ( !Objects.equals(value, jfx_node.isAnimated()) ) {
                jfx_node.setAnimated((boolean) value);
            }

            value = model_widget.propAnimationDuration().getValue();

            if ( !Objects.equals(value, jfx_node.getAnimationDuration()) ) {
                jfx_node.setAnimationDuration((long) value);
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
            jfx_node.setMaxValue(max);
            jfx_node.setMinValue(min);
            jfx_node.setSectionsVisible(areZonesVisible());
            jfx_node.setSections(createZones());
        }

        if ( dirtyValue.checkAndClear() && updatingValue.compareAndSet(false, true) ) {
            try {

                final VType vtype = model_widget.runtimePropValue().getValue();
                double newval = VTypeUtil.getValueNumber(vtype).doubleValue();

                if ( !Double.isNaN(newval) ) {

                    if ( newval < min ) {
                        newval = min;
                    } else if ( newval > max ) {
                        newval = max;
                    }

                    jfx_node.setValue(newval);

                } else {
//  TODO: CR: do something!!!
                }

            } finally {
                updatingValue.set(false);
            }

        }

    }

    /**
     * Change the skin type, resetting some of the gauge parameters.
     *
     * @param skinType The new skin to be set.
     */
    protected void changeSkin ( final Gauge.SkinType skinType ) {

        jfx_node.setSkinType(skinType);
        jfx_node.setPrefWidth(model_widget.propWidth().getValue());
        jfx_node.setPrefHeight(model_widget.propHeight().getValue());
        jfx_node.setAutoScale(true);
        jfx_node.setCheckAreasForValue(false);
        jfx_node.setCheckSectionsForValue(false);
        jfx_node.setCheckThreshold(false);
        jfx_node.setHighlightAreas(false);
        jfx_node.setLedVisible(false);
        jfx_node.setTickLabelLocation(TickLabelLocation.INSIDE);
    }

    protected Gauge createJFXNode ( Gauge.SkinType skin ) throws Exception {

        updateLimits();

        Gauge gauge = GaugeBuilder.create()
                .skinType(skin)
                .prefHeight(model_widget.propHeight().getValue())
                .prefWidth(model_widget.propWidth().getValue())
                //--------------------------------------------------------
                //  Previous properties must be set first.
                //--------------------------------------------------------
                .animated(model_widget.propAnimated().getValue())
                .animationDuration(model_widget.propAnimationDuration().getValue())
                .autoScale(true)
                .checkAreasForValue(false)
                .checkSectionsForValue(false)
                .checkThreshold(false)
                .highlightAreas(false)
                .ledVisible(false)
                .maxValue(max)
                .minValue(min)
                .sections(createZones())
                .sectionsVisible(areZonesVisible())
                .title(model_widget.propTitle().getValue())
                .titleColor(JFXUtil.convert(model_widget.propTitleColor().getValue()))
                .value(( max + min ) / 2.0)
                .build();

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

    /**
     * Creates a new zone with the given parameters.
     *
     * @param start The zone's starting value.
     * @param end   The zone's ending value.
     * @param name  The zone's name.
     * @param color The zone's color.
     * @return A {@link Section} representing the created zone.
     */
    protected Section createZone ( double start, double end, String name, Color color ) {
        return new Section(start, end, name, color);
    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propAnimated().addUntypedPropertyListener(this::behaviorChanged);
        model_widget.propAnimationDuration().addUntypedPropertyListener(this::behaviorChanged);

        model_widget.propVisible().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propX().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propY().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propWidth().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::geometryChanged);

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

    /**
     * Updates, if required, the limits and zones.
     *
     * @return {@code true} is something changed and and UI update is required.
     */
    protected boolean updateLimits ( ) {

        boolean somethingChanged = false;

        //  Model's values.
        double new_min = model_widget.propMinimum().getValue();
        double new_max = model_widget.propMaximum().getValue();
        double new_lolo = model_widget.propLevelLoLo().getValue();
        double new_low = model_widget.propLevelLow().getValue();
        double new_high = model_widget.propLevelHight().getValue();
        double new_hihi = model_widget.propLevelHiHi().getValue();

        if ( model_widget.propLimitsFromPV().getValue() ) {

            //  Try to get display range from PV.
            final Display display_info = ValueUtil.displayOf(model_widget.runtimePropValue().getValue());

            if ( display_info != null ) {
                new_min = display_info.getLowerCtrlLimit();
                new_max = display_info.getUpperCtrlLimit();
                new_lolo = display_info.getLowerAlarmLimit();
                new_low = display_info.getLowerWarningLimit();
                new_high = display_info.getUpperWarningLimit();
                new_hihi = display_info.getUpperAlarmLimit();
            }

        }

        if ( !model_widget.propShowLoLo().getValue() ) {
            new_lolo = Double.NaN;
        }
        if ( !model_widget.propShowLow().getValue() ) {
            new_low = Double.NaN;
        }
        if ( !model_widget.propShowHigh().getValue() ) {
            new_high = Double.NaN;
        }
        if ( !model_widget.propShowHiHi().getValue() ) {
            new_hihi = Double.NaN;
        }

        //  If invalid limits, fall back to 0..100 range.
        if ( !( Double.isNaN(new_min) || Double.isNaN(new_max) || new_min < new_max ) ) {
            new_min = 0.0;
            new_max = 100.0;
        }

        if ( Double.compare(min, new_min) != 0 ) {
            min = new_min;
            somethingChanged = true;
        }
        if ( Double.compare(max, new_max) != 0 ) {
            max = new_max;
            somethingChanged = true;
        }
        if ( Double.compare(lolo, new_lolo) != 0 ) {
            lolo = new_lolo;
            somethingChanged = true;
        }
        if ( Double.compare(low, new_low) != 0 ) {
            low = new_low;
            somethingChanged = true;
        }
        if ( Double.compare(high, new_high) != 0 ) {
            high = new_high;
            somethingChanged = true;
        }
        if ( Double.compare(hihi, new_hihi) != 0 ) {
            hihi = new_hihi;
            somethingChanged = true;
        }

        return somethingChanged;

    }

    protected void valueChanged ( final WidgetProperty<? extends VType> property, final VType old_value, final VType new_value ) {

        if ( model_widget.propLimitsFromPV().getValue() ) {
            limitsChanged(null, null, null);
        }

        dirtyValue.mark();
        toolkit.scheduleUpdate(this);

    }

    private boolean areZonesVisible ( ) {
        return model_widget.propShowLoLo().getValue()
            || model_widget.propShowLow().getValue()
            || model_widget.propShowHigh().getValue()
            || model_widget.propShowHiHi().getValue();
    }

    private void behaviorChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyBehavior.mark();
        toolkit.scheduleUpdate(this);
    }

    private Section[] createZones ( ) {

        boolean loloNaN = Double.isNaN(lolo);
        boolean hihiNaN = Double.isNaN(hihi);
        List<Section> sections = new ArrayList<>(4);

        if ( !loloNaN ) {
            sections.add(createZone(min, lolo, "LoLo", MAJOR_COLOR));
        }

        if ( !Double.isNaN(low) ) {
            sections.add(createZone(loloNaN ? min : lolo, low, "Low", MINOR_COLOR));
        }

        if ( !Double.isNaN(high) ) {
            sections.add(createZone(high, hihiNaN ? max : hihi, "High", MINOR_COLOR));
        }

        if ( !hihiNaN ) {
            sections.add(createZone(hihi, max, "HiHi", MAJOR_COLOR));
        }

        return sections.toArray(new Section[sections.size()]);

    }

    private void geometryChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyGeometry.mark();
        toolkit.scheduleUpdate(this);
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
