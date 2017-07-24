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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.SymbolWidget;
import org.csstudio.javafx.Styles;
import org.diirt.vtype.VType;

import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;


/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 19 Jun 2017
 */
public class SymbolRepresentation extends RegionBaseRepresentation<BorderPane, SymbolWidget> {

    private final DirtyFlag     dirtyContent  = new DirtyFlag();
    private final DirtyFlag     dirtyGeometry = new DirtyFlag();
    private final DirtyFlag     dirtyStyle    = new DirtyFlag();
    private final DirtyFlag     dirtyValue    = new DirtyFlag();
    private volatile boolean    enabled       = true;
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

//        if ( dirtyLook.checkAndClear() ) {
//
//            value = JFXUtil.convert(model_widget.propBackgroundColor().getValue());
//
//            if ( model_widget.propTransparent().getValue() ) {
//                value = ((Color) value).deriveColor(0, 1, 1, 0);
//            }
//
//            if ( !Objects.equals(value, jfx_node.getBackgroundPaint()) ) {
//                jfx_node.setBackgroundPaint((Paint) value);
//            }
//
//            value = model_widget.propTitle().getValue();
//
//            if ( !Objects.equals(value, jfx_node.getTitle()) ) {
//                jfx_node.setTitle((String) value);
//            }
//
//            value = JFXUtil.convert(model_widget.propTitleColor().getValue());
//
//            if ( !Objects.equals(value, jfx_node.getTitleColor()) ) {
//                jfx_node.setTitleColor((Color) value);
//            }
//
//            value = model_widget.propUnit().getValue();
//
//            if ( !Objects.equals(value, jfx_node.getUnit()) ) {
//                jfx_node.setUnit((String) value);
//            }
//
//            value = JFXUtil.convert(model_widget.propUnitColor().getValue());
//
//            if ( !Objects.equals(value, jfx_node.getUnitColor()) ) {
//                jfx_node.setUnitColor((Color) value);
//            }
//
//            value = JFXUtil.convert(model_widget.propValueColor().getValue());
//
//            if ( !Objects.equals(value, jfx_node.getValueColor()) ) {
//                jfx_node.setValueColor((Color) value);
//            }
//
//            value = model_widget.propValueVisible().getValue();
//
//            if ( !Objects.equals(value, jfx_node.isValueVisible()) ) {
//                jfx_node.setValueVisible((boolean) value);
//            }
//
//        }
//
//        if ( dirtyContent.checkAndClear() ) {
//
//            value = FormatOptionHandler.actualPrecision(model_widget.runtimePropValue().getValue(), model_widget.propPrecision().getValue());
//
//            if ( !Objects.equals(value, jfx_node.getDecimals()) ) {
//                jfx_node.setDecimals((int) value);
//            }
//
//        }
//
//        if ( dirtyLimits.checkAndClear() ) {
//            jfx_node.setMaxValue(max);
//            jfx_node.setMinValue(min);
//            jfx_node.setSectionsVisible(areZonesVisible());
//            jfx_node.setSections(createZones());
//        }
//
//        if ( dirtyUnit.checkAndClear() ) {
//            jfx_node.setUnit(unit.get());
//        }
//
//        if ( dirtyStyle.checkAndClear() ) {
//
//            value = model_widget.propEnabled().getValue();
//
//            if ( !Objects.equals(value, enabled) ) {
//
//                enabled = (boolean) value;
//
//                Styles.update(jfx_node, Styles.NOT_ENABLED, !enabled);
//
//            }
//
//        }
//
//        if ( dirtyValue.checkAndClear() && updatingValue.compareAndSet(false, true) ) {
//            try {
//
//                final VType vtype = model_widget.runtimePropValue().getValue();
//                double newval = VTypeUtil.getValueNumber(vtype).doubleValue();
//
//                if ( !Double.isNaN(newval) ) {
//
//                    if ( newval < min ) {
//                        newval = min;
//                    } else if ( newval > max ) {
//                        newval = max;
//                    }
//
//                    jfx_node.setValue(newval);
//
//                } else {
////  TODO: CR: do something!!!
//                }
//
//            } finally {
//                updatingValue.set(false);
//            }
//
//        }

    }

    @Override
    protected BorderPane createJFXNode ( ) throws Exception {

        loadSymbols(model_widget.propSymbols().getValue());

        BorderPane symbol = new BorderPane();
        ImageView imageView = new ImageView();

        imageView.setPreserveRatio(model_widget.propPreserveRatio().getValue());
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.fitHeightProperty().bind(symbol.prefHeightProperty());
        imageView.fitWidthProperty().bind(symbol.prefWidthProperty());

        symbol.setCenter(imageView);

        enabled = model_widget.propEnabled().getValue();

        Styles.update(symbol, Styles.NOT_ENABLED, !enabled);

        return symbol;

    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propPVName().addPropertyListener(this::contentChanged);
        model_widget.propSymbols().addPropertyListener(this::imagesChanged);

        model_widget.propVisible().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propX().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propY().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propWidth().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::geometryChanged);

        model_widget.propEnabled().addUntypedPropertyListener(this::styleChanged);
        model_widget.propPreserveRatio().addUntypedPropertyListener(this::styleChanged);

        if ( toolkit.isEditMode() ) {
            dirtyValue.checkAndClear();
        } else {
            model_widget.runtimePropValue().addPropertyListener(this::valueChanged);
        }

    }

    private void loadSymbols ( List<WidgetProperty<String>> fileNames ) {
        // TODO Auto-generated method stub

    }

    private void contentChanged ( final WidgetProperty<?> property, final Object oldValue, final Object newValue ) {
        dirtyContent.mark();
        toolkit.scheduleUpdate(this);
    }

    private void geometryChanged ( final WidgetProperty<?> property, final Object oldValue, final Object newValue ) {
        dirtyGeometry.mark();
        toolkit.scheduleUpdate(this);
    }

    private void imagesChanged ( final WidgetProperty<List<WidgetProperty<String>>> property, final List<WidgetProperty<String>> oldValue, final List<WidgetProperty<String>> newValue ) {

//  Synch it???
        loadSymbols(model_widget.propSymbols().getValue());



        dirtyStyle.mark();
        dirtyContent.mark();
        toolkit.scheduleUpdate(this);

    }

    private void styleChanged ( final WidgetProperty<?> property, final Object oldValue, final Object newValue ) {
        dirtyStyle.mark();
        toolkit.scheduleUpdate(this);
    }

    private void valueChanged ( final WidgetProperty<? extends VType> property, final VType oldValue, final VType newValue ) {
        dirtyValue.mark();
        toolkit.scheduleUpdate(this);
    }

}
