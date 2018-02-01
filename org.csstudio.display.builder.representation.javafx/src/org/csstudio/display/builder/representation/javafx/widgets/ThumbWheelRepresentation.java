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
import java.util.concurrent.atomic.AtomicBoolean;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.ThumbWheelWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.javafx.Styles;
import org.diirt.vtype.Display;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueUtil;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import se.europeanspallationsource.javafx.control.thumbwheel.ThumbWheel;


/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 13 Dec 2017
 */
public class ThumbWheelRepresentation extends RegionBaseRepresentation<ThumbWheel, ThumbWheelWidget> {

    protected static final Color ALARM_MAJOR_COLOR = JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_MAJOR));
    protected static final Color ALARM_MINOR_COLOR = JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_MINOR));

    private final DirtyFlag     dirtyContent  = new DirtyFlag();
    private final DirtyFlag     dirtyGeometry = new DirtyFlag();
    private final DirtyFlag     dirtyLimits   = new DirtyFlag();
    private final DirtyFlag     dirtyLook     = new DirtyFlag();
    private final DirtyFlag     dirtyStyle    = new DirtyFlag();
    private final DirtyFlag     dirtyValue    = new DirtyFlag();
    private volatile double     max           = 100.0;
    private volatile double     min           = 0.0;
    private final AtomicBoolean updatingValue = new AtomicBoolean(false);

    @SuppressWarnings( "unchecked" )
    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        Object value;

        if ( dirtyContent.checkAndClear() ) {

            value = model_widget.propDecimalDigits().getValue();

            if ( !Objects.equals(value, jfx_node.getDecimalDigits()) ) {
                jfx_node.setDecimalDigits((int) value);
            }

            value = model_widget.propIntegerDigits().getValue();

            if ( !Objects.equals(value, jfx_node.getIntegerDigits()) ) {
                jfx_node.setIntegerDigits((int) value);
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

            value = JFXUtil.convert(model_widget.propBackgroundColor().getValue());

            if ( !Objects.equals(value, jfx_node.getBackgroundColor()) ) {
                jfx_node.setBackgroundColor((Color) value);
            }

            value = JFXUtil.convert(model_widget.propButtonsColor().getValue());

            if ( !Objects.equals(value, jfx_node.getDecrementButtonsColor()) ) {
                jfx_node.setDecrementButtonsColor((Color) value);
            }
            if ( !Objects.equals(value, jfx_node.getIncrementButtonsColor()) ) {
                jfx_node.setIncrementButtonsColor((Color) value);
            }

            value = JFXUtil.convert(model_widget.propFont().getValue());

            if ( !Objects.equals(value, jfx_node.getFont()) ) {
                jfx_node.setFont((Font) value);
            }

            value = JFXUtil.convert(model_widget.propForegroundColor().getValue());

            if ( !Objects.equals(value, jfx_node.getForegroundColor()) ) {
                jfx_node.setForegroundColor((Color) value);
            }

            value = JFXUtil.convert(model_widget.propInvalidColor().getValue());

            if ( !Objects.equals(value, jfx_node.getInvalidColor()) ) {
                jfx_node.setInvalidColor((Color) value);
            }

        }

        if ( dirtyLimits.checkAndClear() ) {

            if ( !Objects.equals(max, jfx_node.getMaxValue()) ) {
                jfx_node.setMaxValue(max);
            }

            if ( !Objects.equals(min, jfx_node.getMinValue()) ) {
                jfx_node.setMinValue(min);
            }

        }

        if ( dirtyStyle.checkAndClear() ) {

            Styles.update(jfx_node, Styles.NOT_ENABLED, !model_widget.propEnabled().getValue());

            value = model_widget.propScrollEnabled().getValue();

            if ( !Objects.equals(value, jfx_node.isScrollEnabled()) ) {
                jfx_node.setScrollEnabled((boolean) value);
            }

        }

        if ( dirtyValue.checkAndClear() && updatingValue.compareAndSet(false, true) ) {
            try {

                double newVal = VTypeUtil.getValueNumber(model_widget.runtimePropValue().getValue()).doubleValue();

                if ( !Double.isNaN(newVal) ) {
                    jfx_node.setValue(clamp(newVal, min, max));
                } else {
                    //  TODO: CR: do something!!!
                }

            } finally {
                updatingValue.set(false);
            }
        }

    }

    @Override
    protected ThumbWheel createJFXNode ( ) throws Exception {

        updateLimits();

        ThumbWheel thumbwheel = new ThumbWheel();

        thumbwheel.setGraphicVisible(true);
        thumbwheel.setSpinnerShaped(true);

        if (! toolkit.isEditMode())
            thumbwheel.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ( !updatingValue.get() )
                    toolkit.fireWrite(model_widget, newValue);
            });

        toolkit.schedule( ( ) -> {
            jfx_node.setPrefWidth(model_widget.propWidth().getValue());
            jfx_node.setPrefHeight(model_widget.propHeight().getValue());
        }, 111, TimeUnit.MILLISECONDS);

        dirtyContent.mark();
        dirtyGeometry.mark();
        dirtyLimits.mark();
        dirtyLook.mark();
        dirtyStyle.mark();
        dirtyValue.mark();
        toolkit.scheduleUpdate(this);

        return thumbwheel;

    }

    @Override
    protected boolean isFilteringEditModeClicks()
    {
        return true;
    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propDecimalDigits().addUntypedPropertyListener(this::contentChanged);
        model_widget.propIntegerDigits().addUntypedPropertyListener(this::contentChanged);
        model_widget.propPVName().addPropertyListener(this::contentChanged);

        model_widget.propVisible().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propX().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propY().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propWidth().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::geometryChanged);

        model_widget.propBackgroundColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propButtonsColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propFont().addUntypedPropertyListener(this::lookChanged);
        model_widget.propForegroundColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propInvalidColor().addUntypedPropertyListener(this::lookChanged);

        model_widget.propLimitsFromPV().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propMaximum().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propMinimum().addUntypedPropertyListener(this::limitsChanged);

        model_widget.propEnabled().addUntypedPropertyListener(this::styleChanged);
        model_widget.propScrollEnabled().addUntypedPropertyListener(this::styleChanged);

        if ( toolkit.isEditMode() ) {
            dirtyValue.checkAndClear();
        } else {
            model_widget.runtimePropValue().addPropertyListener(this::valueChanged);
        }

    }

    private double clamp ( double value, double minValue, double maxValue ) {
        return ( value < minValue ) ? minValue : ( value > maxValue ) ? maxValue : value;
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
        updateLimits();
        dirtyLimits.mark();
        toolkit.scheduleUpdate(this);
    }

    private void lookChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyLook.mark();
        toolkit.scheduleUpdate(this);
    }

    private void styleChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyStyle.mark();
        toolkit.scheduleUpdate(this);
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

        if ( model_widget.propLimitsFromPV().getValue() ) {

            //  Try to get display range from PV.
            final Display display_info = ValueUtil.displayOf(model_widget.runtimePropValue().getValue());

            if ( display_info != null ) {
                newMin = display_info.getLowerCtrlLimit();
                newMax = display_info.getUpperCtrlLimit();
            }

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

        return somethingChanged;

    }

    private void valueChanged ( final WidgetProperty<? extends VType> property, final VType old_value, final VType new_value ) {

        if ( model_widget.propLimitsFromPV().getValue() ) {
            limitsChanged(null, null, null);
        }

        dirtyValue.mark();
        toolkit.scheduleUpdate(this);

    }

}
