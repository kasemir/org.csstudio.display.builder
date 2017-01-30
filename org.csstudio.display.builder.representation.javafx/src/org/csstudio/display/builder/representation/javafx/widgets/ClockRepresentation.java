/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx.widgets;


import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.ClockWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.Clock.ClockSkinType;
import eu.hansolo.medusa.ClockBuilder;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 18 Jan 2017
 */
@SuppressWarnings("nls")
public class ClockRepresentation extends RegionBaseRepresentation<Clock, ClockWidget> {

    private final DirtyFlag dirtyBehavior = new DirtyFlag();
    private final DirtyFlag dirtyGeometry = new DirtyFlag();
    private final DirtyFlag dirtyLook = new DirtyFlag();

    @Override
    public void updateChanges ( ) {

        Object value;

        if ( dirtyBehavior.checkAndClear() ) {

            value = model_widget.propDiscreteHours().getValue();

            if ( !Objects.equals(value, jfx_node.isDiscreteHours()) ) {
                jfx_node.setDiscreteHours((boolean) value);
            }

            value = model_widget.propDiscreteMinutes().getValue();

            if ( !Objects.equals(value, jfx_node.isDiscreteMinutes()) ) {
                jfx_node.setDiscreteMinutes((boolean) value);
            }

            value = model_widget.propDiscreteSeconds().getValue();

            if ( !Objects.equals(value, jfx_node.isDiscreteSeconds()) ) {
                jfx_node.setDiscreteSeconds((boolean) value);
            }

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

            value = ClockSkinType.valueOf(model_widget.propSkin().getValue().name());

            if ( !Objects.equals(value, jfx_node.getSkinType()) ) {
                jfx_node.setSkinType((ClockSkinType) value);
            }

            value = model_widget.propTransparent().getValue() ? Color.TRANSPARENT : JFXUtil.convert(model_widget.propBackgroundColor().getValue());

            if ( !Objects.equals(value, jfx_node.getBackgroundPaint()) ) {
                jfx_node.setBackgroundPaint((Paint) value);
            }

            value = JFXUtil.convert(model_widget.propBorderColor().getValue());

            if ( !Objects.equals(value, jfx_node.getBorderPaint()) ) {
                jfx_node.setBorderPaint((Paint) value);
            }

            value = model_widget.propBorderWidth().getValue();

            if ( !Objects.equals(value, jfx_node.getBorderWidth()) ) {
                jfx_node.setBorderWidth((double) value);
            }

            value = JFXUtil.convert(model_widget.propDateColor().getValue());

            if ( !Objects.equals(value, jfx_node.getDateColor()) ) {
                jfx_node.setDateColor((Color) value);
            }

            value = model_widget.propDateVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isDateVisible()) ) {
                jfx_node.setDateVisible((boolean) value);
            }

            value = JFXUtil.convert(model_widget.propHourColor().getValue());

            if ( !Objects.equals(value, jfx_node.getHourColor()) ) {
                jfx_node.setHourColor((Color) value);
            }

            value = JFXUtil.convert(model_widget.propHourTickMarkColor().getValue());

            if ( !Objects.equals(value, jfx_node.getHourTickMarkColor()) ) {
                jfx_node.setHourTickMarkColor((Color) value);
            }

            value = model_widget.propHourTickMarkVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isHourTickMarksVisible()) ) {
                jfx_node.setHourTickMarksVisible((boolean) value);
            }

            value = JFXUtil.convert(model_widget.propKnobColor().getValue());

            if ( !Objects.equals(value, jfx_node.getKnobColor()) ) {
                jfx_node.setKnobColor((Color) value);
            }

            value = model_widget.propLocale().getValue();

            if ( value != null ) {

                Locale l = Locale.getDefault();

                try {
                    l = Locale.forLanguageTag(value.toString());
                } catch ( Exception ex ) {
                    logger.log(Level.WARNING, "Unable to convert \"{0}\" to a Local instance [{1}].", new Object[] { value.toString(), ex.getMessage()});
                }

                if ( !l.equals(jfx_node.getLocale()) ) {
                    jfx_node.setLocale(l);
                }

            }

            value = JFXUtil.convert(model_widget.propMinuteColor().getValue());

            if ( !Objects.equals(value, jfx_node.getMinuteColor()) ) {
                jfx_node.setMinuteColor((Color) value);
            }

            value = JFXUtil.convert(model_widget.propMinuteTickMarkColor().getValue());

            if ( !Objects.equals(value, jfx_node.getMinuteTickMarkColor()) ) {
                jfx_node.setMinuteTickMarkColor((Color) value);
            }

            value = model_widget.propMinuteTickMarkVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isMinuteTickMarksVisible()) ) {
                jfx_node.setMinuteTickMarksVisible((boolean) value);
            }

            value = JFXUtil.convert(model_widget.propSecondColor().getValue());

            if ( !Objects.equals(value, jfx_node.getSecondColor()) ) {
                jfx_node.setSecondColor((Color) value);
            }

            value = model_widget.propSecondVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isSecondsVisible()) ) {
                jfx_node.setSecondsVisible((boolean) value);
            }

            value = model_widget.propShadowsEnabled().getValue();

            if ( !Objects.equals(value, jfx_node.getShadowsEnabled()) ) {
                jfx_node.setShadowsEnabled((boolean) value);
            }

            value = JFXUtil.convert(model_widget.propTextColor().getValue());

            if ( !Objects.equals(value, jfx_node.getTextColor()) ) {
                jfx_node.setTextColor((Color) value);
            }

            value = model_widget.propTextVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isTextVisible()) ) {
                jfx_node.setTextVisible((boolean) value);
            }

            value = JFXUtil.convert(model_widget.propTickLabelColor().getValue());

            if ( !Objects.equals(value, jfx_node.getTickLabelColor()) ) {
                jfx_node.setTickLabelColor((Color) value);
            }

            value = model_widget.propTickLabelVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isTickLabelsVisible()) ) {
                jfx_node.setTickLabelsVisible((boolean) value);
            }

            value = model_widget.propTitle().getValue();

            if ( !Objects.equals(value, jfx_node.getTitle()) ) {
                jfx_node.setTitle((String) value);
            }

            value = JFXUtil.convert(model_widget.propTitleColor().getValue());

            if ( !Objects.equals(value, jfx_node.getTitleColor()) ) {
                jfx_node.setTitleColor((Color) value);
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
                                  .skinType(ClockSkinType.valueOf(model_widget.propSkin().getValue().name()))
                                  .prefHeight(model_widget.propHeight().getValue())
                                  .prefWidth(model_widget.propWidth().getValue())
                                  //--------------------------------------------------------
                                  //  Previous properties must be set first.
                                  //--------------------------------------------------------
                                  .backgroundPaint(model_widget.propTransparent().getValue() ? Color.TRANSPARENT : JFXUtil.convert(model_widget.propBackgroundColor().getValue()))
                                  .borderPaint(JFXUtil.convert(model_widget.propBorderColor().getValue()))
                                  .borderWidth(model_widget.propBorderWidth().getValue())
                                  .dateColor(JFXUtil.convert(model_widget.propDateColor().getValue()))
                                  .dateVisible(model_widget.propDateVisible().getValue())
                                  .discreteHours(model_widget.propDiscreteHours().getValue())
                                  .discreteMinutes(model_widget.propDiscreteMinutes().getValue())
                                  .discreteSeconds(model_widget.propDiscreteSeconds().getValue())
                                  .hourColor(JFXUtil.convert(model_widget.propHourColor().getValue()))
                                  .hourTickMarkColor(JFXUtil.convert(model_widget.propHourTickMarkColor().getValue()))
                                  .hourTickMarksVisible(model_widget.propHourTickMarkVisible().getValue())
                                  .knobColor(JFXUtil.convert(model_widget.propKnobColor().getValue()))
                                  .minuteColor(JFXUtil.convert(model_widget.propMinuteColor().getValue()))
                                  .minuteTickMarkColor(JFXUtil.convert(model_widget.propMinuteTickMarkColor().getValue()))
                                  .minuteTickMarksVisible(model_widget.propMinuteTickMarkVisible().getValue())
                                  .running(model_widget.propRunning().getValue())
                                  .secondColor(JFXUtil.convert(model_widget.propSecondColor().getValue()))
                                  .secondsVisible(model_widget.propSecondVisible().getValue())
                                  .shadowsEnabled(model_widget.propShadowsEnabled().getValue())
                                  .textColor(JFXUtil.convert(model_widget.propTextColor().getValue()))
                                  .textVisible(model_widget.propTextVisible().getValue())
                                  .tickLabelColor(JFXUtil.convert(model_widget.propTickLabelColor().getValue()))
                                  .tickLabelsVisible(model_widget.propTickLabelVisible().getValue())
                                  .title(model_widget.propTitle().getValue())
                                  .titleColor(JFXUtil.convert(model_widget.propTitleColor().getValue()))
                                  .titleVisible(model_widget.propTitleVisible().getValue())
                                  .build();

        String locale = model_widget.propLocale().getValue();

        if ( locale != null ) {
            try {
                jfx_node.setLocale(Locale.forLanguageTag(locale.toString()));
            } catch ( Exception ex ) {
                logger.log(Level.WARNING, "Unable to convert \"{0}\" to a Local instance [{1}].", new Object[] { locale.toString(), ex.getMessage()});
            }
        }

        clock.backgroundPaintProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, JFXUtil.convert(model_widget.propBackgroundColor().getValue())) ) {
                model_widget.propBackgroundColor().setValue(JFXUtil.convert((Color) n));
            }
        });
        clock.borderPaintProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, JFXUtil.convert(model_widget.propBorderColor().getValue())) ) {
                model_widget.propBorderColor().setValue(JFXUtil.convert((Color) n));
            }
        });
        clock.borderWidthProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propBorderWidth().getValue()) ) {
                model_widget.propBorderWidth().setValue(n.doubleValue());
            }
        });
        clock.dateColorProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, JFXUtil.convert(model_widget.propDateColor().getValue())) ) {
                model_widget.propDateColor().setValue(JFXUtil.convert(n));
            }
        });
        clock.dateVisibleProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propDateVisible().getValue()) ) {
                model_widget.propDateVisible().setValue(n);
            }
        });
        clock.discreteHoursProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propDiscreteHours().getValue()) ) {
                model_widget.propDiscreteHours().setValue(n);
            }
        });
        clock.discreteMinutesProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propDiscreteMinutes().getValue()) ) {
                model_widget.propDiscreteMinutes().setValue(n);
            }
        });
        clock.discreteSecondsProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propDiscreteSeconds().getValue()) ) {
                model_widget.propDiscreteSeconds().setValue(n);
            }
        });
        clock.hourColorProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, JFXUtil.convert(model_widget.propHourColor().getValue())) ) {
                model_widget.propHourColor().setValue(JFXUtil.convert(n));
            }
        });
        clock.hourTickMarkColorProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, JFXUtil.convert(model_widget.propHourTickMarkColor().getValue())) ) {
                model_widget.propHourTickMarkColor().setValue(JFXUtil.convert(n));
            }
        });
        clock.hourTickMarksVisibleProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propHourTickMarkVisible().getValue()) ) {
                model_widget.propHourTickMarkVisible().setValue(n);
            }
        });
        clock.knobColorProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, JFXUtil.convert(model_widget.propKnobColor().getValue())) ) {
                model_widget.propKnobColor().setValue(JFXUtil.convert(n));
            }
        });
        clock.localeProperty().addListener( ( s, o, n ) -> {
            if ( n != null && !Objects.equals(n.toLanguageTag(), model_widget.propLocale().getValue()) ) {
                model_widget.propLocale().setValue(n.toLanguageTag());
            }
        });
        clock.minuteColorProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, JFXUtil.convert(model_widget.propMinuteColor().getValue())) ) {
                model_widget.propMinuteColor().setValue(JFXUtil.convert(n));
            }
        });
        clock.minuteTickMarkColorProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, JFXUtil.convert(model_widget.propMinuteTickMarkColor().getValue())) ) {
                model_widget.propMinuteTickMarkColor().setValue(JFXUtil.convert(n));
            }
        });
        clock.minuteTickMarksVisibleProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propMinuteTickMarkVisible().getValue()) ) {
                model_widget.propMinuteTickMarkVisible().setValue(n);
            }
        });
        clock.runningProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propRunning().getValue()) ) {
                model_widget.propRunning().setValue(n);
            }
        });
        clock.secondColorProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, JFXUtil.convert(model_widget.propSecondColor().getValue())) ) {
                model_widget.propSecondColor().setValue(JFXUtil.convert(n));
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
        clock.textColorProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, JFXUtil.convert(model_widget.propTextColor().getValue())) ) {
                model_widget.propTextColor().setValue(JFXUtil.convert(n));
            }
        });
        clock.textVisibleProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propTextVisible().getValue()) ) {
                model_widget.propTextVisible().setValue(n);
            }
        });
        clock.tickLabelColorProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, JFXUtil.convert(model_widget.propTickLabelColor().getValue())) ) {
                model_widget.propTickLabelColor().setValue(JFXUtil.convert(n));
            }
        });
        clock.tickLabelsVisibleProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propTickLabelVisible().getValue()) ) {
                model_widget.propTickLabelVisible().setValue(n);
            }
        });
        clock.titleProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propTitle().getValue()) ) {
                model_widget.propTitle().setValue(n);
            }
        });
        clock.titleColorProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, JFXUtil.convert(model_widget.propTitleColor().getValue())) ) {
                model_widget.propTitleColor().setValue(JFXUtil.convert(n));
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

        model_widget.propBackgroundColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propBorderColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propBorderWidth().addUntypedPropertyListener(this::lookChanged);
        model_widget.propDateColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propDateVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propHourColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propHourTickMarkColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propHourTickMarkVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propKnobColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propLocale().addUntypedPropertyListener(this::lookChanged);
        model_widget.propMinuteColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propMinuteTickMarkColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propMinuteTickMarkVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propSecondColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propSecondVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propShadowsEnabled().addUntypedPropertyListener(this::lookChanged);
        model_widget.propSkin().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTextColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTextVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTickLabelColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTickLabelVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTitle().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTitleColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTitleVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTransparent().addUntypedPropertyListener(this::lookChanged);

        model_widget.propDiscreteHours().addUntypedPropertyListener(this::behaviorChanged);
        model_widget.propDiscreteMinutes().addUntypedPropertyListener(this::behaviorChanged);
        model_widget.propDiscreteSeconds().addUntypedPropertyListener(this::behaviorChanged);
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
