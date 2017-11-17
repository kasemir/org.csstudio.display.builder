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

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.LinearMeterWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelLocation;
import javafx.geometry.Orientation;
import javafx.scene.paint.Color;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 25 Jan 2017
 */
public class LinearMeterRepresentation extends BaseMeterRepresentation<LinearMeterWidget> {

    private final DirtyFlag               dirtyLimits    = new DirtyFlag();
    private final DirtyFlag               dirtyLook      = new DirtyFlag();
    private LinearMeterWidget.Orientation orientation    = null;
    private volatile boolean              updatingAreas  = false;
    private volatile boolean              zonesHighlight = true;

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        Object value;

        if ( dirtyLook.checkAndClear() ) {

            value = model_widget.propOrientation().getValue();

            if ( !Objects.equals(value, orientation) ) {

                orientation = (LinearMeterWidget.Orientation) value;

                jfx_node.setOrientation(Orientation.valueOf(orientation.name()));

            }

            value = JFXUtil.convert(model_widget.propBarColor().getValue());

            if ( !Objects.equals(value, jfx_node.getBarColor()) ) {
                jfx_node.setBarColor((Color) value);
            }

            value = model_widget.propFlatBar().getValue();

            if ( !Objects.equals(value, !jfx_node.isBarEffectEnabled()) ) {
                jfx_node.setBarEffectEnabled(!( (boolean) value ));
            }

        }

        if ( dirtyLimits.checkAndClear() ) {
            jfx_node.setAreas(createAreas());
            jfx_node.setHighlightSections(zonesHighlight);
            jfx_node.setSections(createZones());
        }

    }

    @Override
    protected void changeSkin ( final Gauge.SkinType skinType ) {

        super.changeSkin(skinType);

        jfx_node.setAreaIconsVisible(false);
        jfx_node.setAreaTextVisible(false);
        jfx_node.setAreas(createAreas());
        jfx_node.setAreasVisible(false);
        jfx_node.setBarColor(JFXUtil.convert(model_widget.propBarColor().getValue()));
        jfx_node.setBarEffectEnabled(!model_widget.propFlatBar().getValue());
        jfx_node.setHighlightSections(zonesHighlight);
        jfx_node.setOrientation(Orientation.valueOf(orientation.name()));
        jfx_node.setTickLabelLocation(TickLabelLocation.INSIDE);

    }

    @Override
    protected Gauge createJFXNode ( ) throws Exception {

        try {

            orientation = model_widget.propOrientation().getValue();

            Gauge gauge = super.createJFXNode();

            gauge.setAreaIconsVisible(false);
            gauge.setAreaTextVisible(false);
            gauge.setOrientation(Orientation.valueOf(orientation.name()));
            gauge.setTickLabelLocation(TickLabelLocation.INSIDE);

            return gauge;

        } finally {
            dirtyLimits.mark();
            dirtyLook.mark();
            toolkit.scheduleUpdate(this);
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

        if ( updatingAreas ) {
            return super.createZone(start, end, name, color);
        } else {
            return createZone(zonesHighlight, start, end, name, color);
        }

    }

    @Override
    protected Gauge.SkinType getSkin() {
        return Gauge.SkinType.LINEAR;
    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propBarColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propFlatBar().addUntypedPropertyListener(this::lookChanged);
        model_widget.propOrientation().addUntypedPropertyListener(this::lookChanged);

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

    private List<Section> createAreas ( ) {

        updatingAreas = true;

        try {
            return createZones();
        } finally {
            updatingAreas = false;
        }

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
