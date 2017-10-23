/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx.widgets;


import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.BaseMeterWidget;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.LcdDesign;
import eu.hansolo.medusa.LcdFont;
import eu.hansolo.medusa.Section;
import javafx.scene.paint.Color;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 20 Feb 2017
 */
public abstract class BaseMeterRepresentation<W extends BaseMeterWidget> extends BaseGaugeRepresentation<W> {

    private final DirtyFlag dirtyLook = new DirtyFlag();

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        Object value;

        if ( dirtyLook.checkAndClear() ) {
            jfx_node.setAutoScale(model_widget.propAutoScale().getValue());
            jfx_node.setLcdDesign(LcdDesign.valueOf(model_widget.propLcdDesign().getValue().name()));
            jfx_node.setLcdFont(LcdFont.valueOf(model_widget.propLcdFont().getValue().name()));
            jfx_node.setLcdVisible(model_widget.propLcdVisible().getValue());
            jfx_node.setMajorTickSpace(model_widget.propMajorTickSpace().getValue());
            jfx_node.setMinorTickSpace(model_widget.propMinorTickSpace().getValue());
        }

    }

    @Override
    protected void changeSkin ( final Gauge.SkinType skinType ) {

        super.changeSkin(skinType);

        jfx_node.setAutoScale(model_widget.propAutoScale().getValue());
        jfx_node.setLcdDesign(LcdDesign.valueOf(model_widget.propLcdDesign().getValue().name()));
        jfx_node.setLcdFont(LcdFont.valueOf(model_widget.propLcdFont().getValue().name()));
        jfx_node.setLcdVisible(model_widget.propLcdVisible().getValue());
        jfx_node.setNeedleBehavior(Gauge.NeedleBehavior.STANDARD);
        jfx_node.setMajorTickSpace(model_widget.propMajorTickSpace().getValue());
        jfx_node.setMinorTickSpace(model_widget.propMinorTickSpace().getValue());

    }

    @Override
    protected Gauge createJFXNode ( ) throws Exception {

        Gauge gauge = super.createJFXNode(Gauge.SkinType.LINEAR);

        gauge.setAutoScale(model_widget.propAutoScale().getValue());
        gauge.setLcdDesign(LcdDesign.valueOf(model_widget.propLcdDesign().getValue().name()));
        gauge.setLcdFont(LcdFont.valueOf(model_widget.propLcdFont().getValue().name()));
        gauge.setLcdVisible(model_widget.propLcdVisible().getValue());
        gauge.setNeedleBehavior(Gauge.NeedleBehavior.STANDARD);
        gauge.setMajorTickSpace(model_widget.propMajorTickSpace().getValue());
        gauge.setMinorTickSpace(model_widget.propMinorTickSpace().getValue());

        return gauge;

    }

    /**
     * Creates a new zone with the given parameters.
     *
     * @param zonesHighlight Whether the zone must be highlighted or not.
     * @param start          The zone's starting value.
     * @param end            The zone's ending value.
     * @param name           The zone's name.
     * @param color          The zone's color.
     * @return A {@link Section} representing the created zone.
     */
    protected Section createZone ( boolean zonesHighlight, double start, double end, String name, Color color ) {

        if ( zonesHighlight ) {

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

        model_widget.propAutoScale().addUntypedPropertyListener(this::lookChanged);
        model_widget.propLcdDesign().addUntypedPropertyListener(this::lookChanged);
        model_widget.propLcdFont().addUntypedPropertyListener(this::lookChanged);
        model_widget.propLcdVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propMajorTickSpace().addUntypedPropertyListener(this::lookChanged);
        model_widget.propMinorTickSpace().addUntypedPropertyListener(this::lookChanged);

    }

    private void lookChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyLook.mark();
        toolkit.scheduleUpdate(this);
    }

}
