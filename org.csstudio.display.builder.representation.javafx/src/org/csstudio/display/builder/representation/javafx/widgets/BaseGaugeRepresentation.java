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
import org.csstudio.display.builder.model.util.FormatOptionHandler;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.BaseGaugeWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.javafx.Styles;
import org.diirt.vtype.Display;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueUtil;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Section;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 8 Feb 2017
 */
public abstract class BaseGaugeRepresentation<W extends BaseGaugeWidget> extends RegionBaseRepresentation<Gauge, W> {

    private static final Color MINOR_COLOR = JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_MINOR));
    private static final Color MAJOR_COLOR = JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_MAJOR));

    private final DirtyFlag     dirtyContent  = new DirtyFlag();
    private final DirtyFlag     dirtyGeometry = new DirtyFlag();
    private final DirtyFlag     dirtyLimits   = new DirtyFlag();
    private final DirtyFlag     dirtyLook     = new DirtyFlag();
    private final DirtyFlag     dirtyStyle    = new DirtyFlag();
    private final DirtyFlag     dirtyValue    = new DirtyFlag();
    private volatile boolean    enabled       = true;
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

            value = JFXUtil.convert(model_widget.propBackgroundColor().getValue());

            if ( model_widget.propTransparent().getValue() ) {
                value = ((Color) value).deriveColor(0, 1, 1, 0);
            }

            if ( !Objects.equals(value, jfx_node.getBackgroundPaint()) ) {
                jfx_node.setBackgroundPaint((Paint) value);
            }

            value = model_widget.propTitle().getValue();

            if ( !Objects.equals(value, jfx_node.getTitle()) ) {
                jfx_node.setTitle((String) value);
            }

            value = JFXUtil.convert(model_widget.propTitleColor().getValue());

            if ( !Objects.equals(value, jfx_node.getTitleColor()) ) {
                jfx_node.setTitleColor((Color) value);
            }

            value = model_widget.propUnit().getValue();

            if ( !Objects.equals(value, jfx_node.getUnit()) ) {
                jfx_node.setUnit((String) value);
            }

            value = JFXUtil.convert(model_widget.propUnitColor().getValue());

            if ( !Objects.equals(value, jfx_node.getUnitColor()) ) {
                jfx_node.setUnitColor((Color) value);
            }

            value = JFXUtil.convert(model_widget.propValueColor().getValue());

            if ( !Objects.equals(value, jfx_node.getValueColor()) ) {
                jfx_node.setValueColor((Color) value);
            }

            value = model_widget.propValueVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isValueVisible()) ) {
                jfx_node.setValueVisible((boolean) value);
            }

        }

        if ( dirtyContent.checkAndClear() ) {

            value = FormatOptionHandler.actualPrecision(model_widget.runtimePropValue().getValue(), model_widget.propPrecision().getValue());

            if ( !Objects.equals(value, jfx_node.getDecimals()) ) {
                jfx_node.setDecimals((int) value);
            }

        }

        if ( dirtyLimits.checkAndClear() ) {
            jfx_node.setMaxValue(max);
            jfx_node.setMinValue(min);
            jfx_node.setSectionsVisible(areZonesVisible());
            jfx_node.setSections(createZones());
        }

        if ( dirtyStyle.checkAndClear() ) {

            value = model_widget.propEnabled().getValue();

            if ( !Objects.equals(value, enabled) ) {

                enabled = (boolean) value;

                Styles.update(jfx_node, Styles.NOT_ENABLED, !enabled);

            }

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

    protected final boolean areZonesVisible ( ) {
        return model_widget.propShowLoLo().getValue()
            || model_widget.propShowLow().getValue()
            || model_widget.propShowHigh().getValue()
            || model_widget.propShowHiHi().getValue();
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

        jfx_node.setAnimated(false);
        jfx_node.setAutoScale(true);
        jfx_node.setBackgroundPaint(model_widget.propTransparent().getValue() ? Color.TRANSPARENT : JFXUtil.convert(model_widget.propBackgroundColor().getValue()));
        jfx_node.setCheckAreasForValue(false);
        jfx_node.setCheckSectionsForValue(false);
        jfx_node.setCheckThreshold(false);
        jfx_node.setDecimals(FormatOptionHandler.actualPrecision(model_widget.runtimePropValue().getValue(), model_widget.propPrecision().getValue()));
        jfx_node.setHighlightAreas(false);
        jfx_node.setInnerShadowEnabled(false);
        jfx_node.setInteractive(false);
        jfx_node.setLedVisible(false);
        jfx_node.setReturnToZero(false);
        jfx_node.setSectionIconsVisible(false);
        jfx_node.setSectionTextVisible(false);
        jfx_node.setTitle(model_widget.propTitle().getValue());
        jfx_node.setTitleColor(JFXUtil.convert(model_widget.propTitleColor().getValue()));
        jfx_node.setUnit(model_widget.propUnit().getValue());
        jfx_node.setUnitColor(JFXUtil.convert(model_widget.propUnitColor().getValue()));
        jfx_node.setValueColor(JFXUtil.convert(model_widget.propValueColor().getValue()));
        jfx_node.setValueVisible(model_widget.propValueVisible().getValue());

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
                .animated(false)
                .autoScale(true)
                .backgroundPaint(model_widget.propTransparent().getValue() ? Color.TRANSPARENT : JFXUtil.convert(model_widget.propBackgroundColor().getValue()))
                .checkAreasForValue(false)
                .checkSectionsForValue(false)
                .checkThreshold(false)
                .decimals(FormatOptionHandler.actualPrecision(model_widget.runtimePropValue().getValue(), model_widget.propPrecision().getValue()))
                .highlightAreas(false)
                .innerShadowEnabled(false)
                .interactive(false)
                .ledVisible(false)
                .maxValue(max)
                .minValue(min)
                .returnToZero(false)
                .sectionIconsVisible(false)
                .sectionTextVisible(false)
                .sections(createZones())
                .sectionsVisible(areZonesVisible())
                .title(model_widget.propTitle().getValue())
                .titleColor(JFXUtil.convert(model_widget.propTitleColor().getValue()))
                .unit(model_widget.propUnit().getValue())
                .unitColor(JFXUtil.convert(model_widget.propUnitColor().getValue()))
                .value(( max + min ) / 2.0)
                .valueColor(JFXUtil.convert(model_widget.propValueColor().getValue()))
                .valueVisible(model_widget.propValueVisible().getValue())
                .build();

        enabled = model_widget.propEnabled().getValue();

        Styles.update(gauge, Styles.NOT_ENABLED, !enabled);

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

    /**
     * Creates the zones.
     *
     * @return An array of {@link Section}s.
     */
    protected final Section[] createZones ( ) {

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

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propPrecision().addUntypedPropertyListener(this::contentChanged);
        model_widget.propPVName().addPropertyListener(this::contentChanged);

        model_widget.propVisible().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propX().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propY().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propWidth().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::geometryChanged);

        model_widget.propBackgroundColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTitle().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTitleColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTransparent().addUntypedPropertyListener(this::lookChanged);
        model_widget.propUnit().addUntypedPropertyListener(this::lookChanged);
        model_widget.propUnitColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propValueColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propValueVisible().addUntypedPropertyListener(this::lookChanged);

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

        model_widget.propEnabled().addUntypedPropertyListener(this::styleChanged);

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
        double newMin = model_widget.propMinimum().getValue();
        double newMax = model_widget.propMaximum().getValue();
        double newLoLo = model_widget.propLevelLoLo().getValue();
        double newLow = model_widget.propLevelLow().getValue();
        double newHigh = model_widget.propLevelHight().getValue();
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

    private void contentChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyContent.mark();
        toolkit.scheduleUpdate(this);
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

    private void styleChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyStyle.mark();
        toolkit.scheduleUpdate(this);
    }

    private void valueChanged ( final WidgetProperty<? extends VType> property, final VType old_value, final VType new_value ) {

        if ( model_widget.propLimitsFromPV().getValue() ) {
            limitsChanged(null, null, null);
        }

        if ( model_widget.propPrecision().getValue() == -1 ) {
            contentChanged(null, null, null);
        }

        dirtyValue.mark();
        toolkit.scheduleUpdate(this);

    }

}
