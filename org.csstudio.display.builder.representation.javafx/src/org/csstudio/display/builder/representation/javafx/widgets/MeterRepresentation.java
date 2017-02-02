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
import eu.hansolo.medusa.GaugeBuilder;
import javafx.geometry.Orientation;
import javafx.scene.paint.Color;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 25 Jan 2017
 */
public class MeterRepresentation extends RegionBaseRepresentation<Gauge, MeterWidget> {

    private final DirtyFlag dirtyBehavior = new DirtyFlag();
    private final DirtyFlag dirtyGeometry = new DirtyFlag();
    private final DirtyFlag dirtyLook = new DirtyFlag();

    @Override
    public void updateChanges ( ) {

        Object value;

        if ( dirtyBehavior.checkAndClear() ) {

            value = model_widget.propAnimated().getValue();

            if ( !Objects.equals(value, jfx_node.isAnimated()) ) {
                jfx_node.setAnimated((boolean) value);
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

            final MeterWidget.Skin skin = model_widget.propSkin().getValue();
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

            if ( !Objects.equals(skinType, jfx_node.getSkinType()) ) {

                jfx_node.setSkinType(skinType);

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

    }

    @Override
    protected Gauge createJFXNode ( ) throws Exception {

        final MeterWidget.Skin skin = model_widget.propSkin().getValue();
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
                                  .title(model_widget.propTitle().getValue())
                                  .titleColor(JFXUtil.convert(model_widget.propTitleColor().getValue()))
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

        if ( skin == MeterWidget.Skin.THREE_QUARTERS ) {
            gauge.setAngleRange(270);
            gauge.setStartAngle(0);
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

        model_widget.propVisible().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propX().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propY().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propWidth().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::geometryChanged);

        model_widget.propSkin().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTitle().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTitleColor().addUntypedPropertyListener(this::lookChanged);

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
