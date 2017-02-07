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
import org.csstudio.display.builder.model.widgets.BasicClockWidget;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.ClockBuilder;

/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 7 Feb 2017
 */
public abstract class BasicClockRepresentation<W extends BasicClockWidget> extends RegionBaseRepresentation<Clock, W> {

    private final DirtyFlag dirtyBehavior = new DirtyFlag();
    private final DirtyFlag dirtyGeometry = new DirtyFlag();
    private final DirtyFlag dirtyLook     = new DirtyFlag();

    @Override
    public void updateChanges ( ) {

        Object value;

        if ( dirtyBehavior.checkAndClear() ) {

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

            value = model_widget.propDateVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isDateVisible()) ) {
                jfx_node.setDateVisible((boolean) value);
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

            value = model_widget.propSecondVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isSecondsVisible()) ) {
                jfx_node.setSecondsVisible((boolean) value);
            }

            value = model_widget.propShadowsEnabled().getValue();

            if ( !Objects.equals(value, jfx_node.getShadowsEnabled()) ) {
                jfx_node.setShadowsEnabled((boolean) value);
            }

            value = model_widget.propTitle().getValue();

            if ( !Objects.equals(value, jfx_node.getTitle()) ) {
                jfx_node.setTitle((String) value);
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
                                  .prefHeight(model_widget.propHeight().getValue())
                                  .prefWidth(model_widget.propWidth().getValue())
                                  //--------------------------------------------------------
                                  //  Previous properties must be set first.
                                  //--------------------------------------------------------
                                  .dateVisible(model_widget.propDateVisible().getValue())
                                  .running(model_widget.propRunning().getValue())
                                  .secondsVisible(model_widget.propSecondVisible().getValue())
                                  .shadowsEnabled(model_widget.propShadowsEnabled().getValue())
                                  .title(model_widget.propTitle().getValue())
                                  .titleVisible(model_widget.propTitleVisible().getValue())
                                  .build();

        String locale = model_widget.propLocale().getValue();

        if ( locale != null ) {
            try {
                clock.setLocale(Locale.forLanguageTag(locale.toString()));
            } catch ( Exception ex ) {
                logger.log(Level.WARNING, "Unable to convert \"{0}\" to a Local instance [{1}].", new Object[] { locale.toString(), ex.getMessage()});
            }
        }

        clock.dateVisibleProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propDateVisible().getValue()) ) {
                model_widget.propDateVisible().setValue(n);
            }
        });
        clock.layoutXProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propX().getValue()) ) {
                model_widget.propX().setValue(n.intValue());
            }
        });
        clock.layoutYProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propY().getValue()) ) {
                model_widget.propY().setValue(n.intValue());
            }
        });
        clock.localeProperty().addListener( ( s, o, n ) -> {
            if ( n != null && !Objects.equals(n.toLanguageTag(), model_widget.propLocale().getValue()) ) {
                model_widget.propLocale().setValue(n.toLanguageTag());
            }
        });
        clock.prefHeightProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propHeight().getValue()) ) {
                model_widget.propHeight().setValue(n.intValue());
            }
        });
        clock.prefWidthProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propWidth().getValue()) ) {
                model_widget.propWidth().setValue(n.intValue());
            }
        });
        clock.runningProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propRunning().getValue()) ) {
                model_widget.propRunning().setValue(n);
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
        clock.titleProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propTitle().getValue()) ) {
                model_widget.propTitle().setValue(n);
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

        model_widget.propDateVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propLocale().addUntypedPropertyListener(this::lookChanged);
        model_widget.propSecondVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propShadowsEnabled().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTitle().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTitleVisible().addUntypedPropertyListener(this::lookChanged);

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
