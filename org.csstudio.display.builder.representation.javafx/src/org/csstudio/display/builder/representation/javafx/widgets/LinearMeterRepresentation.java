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
import org.csstudio.display.builder.model.widgets.LinearMeterWidget;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelLocation;
import javafx.geometry.Orientation;
import javafx.scene.paint.Color;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 25 Jan 2017
 */
public class LinearMeterRepresentation extends BaseGaugeRepresentation<LinearMeterWidget> {

    private volatile boolean              barHighlight   = true;
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

        }

        if ( dirtyLimits.checkAndClear() ) {
            jfx_node.setAreas(createAreas());
            jfx_node.setAreasVisible(barHighlight && areZonesVisible());
            jfx_node.setHighlightSections(zonesHighlight);
        }

    }

    @Override
    protected Gauge createJFXNode ( ) throws Exception {

        orientation = model_widget.propOrientation().getValue();

        Gauge gauge = super.createJFXNode(Gauge.SkinType.LINEAR);

        gauge.setAreas(createAreas());
        gauge.setAreasVisible(barHighlight);
        gauge.setHighlightSections(zonesHighlight);
        gauge.setOrientation(Orientation.valueOf(orientation.name()));
        gauge.setTickLabelLocation(TickLabelLocation.INSIDE);

        gauge.highlightAreasProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propHighlightBar().getValue()) ) {
                model_widget.propHighlightBar().setValue(n);
            }
        });
        gauge.highlightSectionsProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, model_widget.propHighlightZones().getValue()) ) {
                model_widget.propHighlightZones().setValue(n);
            }
        });
        gauge.orientationProperty().addListener( ( s, o, n ) -> {
            if ( !Objects.equals(n, Orientation.valueOf(model_widget.propOrientation().getValue().name())) ) {
                model_widget.propOrientation().setValue(LinearMeterWidget.Orientation.valueOf(n.name()));
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
    @Override
    protected Section createZone ( double start, double end, String name, Color color ) {

        if ( updatingAreas ) {
            return super.createZone(start, end, name, color);
        } else if ( zonesHighlight ) {

            Section s = new Section(start, end, color.deriveColor(0.0, 1.0, 1.0, 0.2), color);

            s.setText(name);

            return s;

        } else {
            return super.createZone(start, end, name, color);
        }

    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

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

        boolean newBarHighlight = model_widget.propHighlightBar().getValue();

        if ( barHighlight != newBarHighlight ) {
            barHighlight = newBarHighlight;
            somethingChanged = true;
        }

        return somethingChanged;

    }

    private Section[] createAreas ( ) {

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
