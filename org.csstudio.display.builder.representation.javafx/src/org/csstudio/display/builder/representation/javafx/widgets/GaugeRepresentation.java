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
import org.csstudio.display.builder.model.widgets.GaugeWidget;
import org.csstudio.display.builder.model.widgets.GaugeWidget.Skin;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import eu.hansolo.medusa.Gauge;
import javafx.scene.paint.Color;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 25 Jan 2017
 */
public class GaugeRepresentation extends BaseGaugeRepresentation<GaugeWidget> {

    private final DirtyFlag  dirtyLook = new DirtyFlag();
    private GaugeWidget.Skin skin      = null;

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        Object value;

        if ( dirtyLook.checkAndClear() ) {

            value = model_widget.propSkin().getValue();

            if ( !Objects.equals(value, skin) ) {

                skin = (Skin) value;

                changeSkin(Gauge.SkinType.valueOf(skin.name()));

            }

            value = JFXUtil.convert(model_widget.propBarBackgroundColor().getValue());

            if ( !Objects.equals(value, jfx_node.getBarBackgroundColor()) ) {
                jfx_node.setBarBackgroundColor((Color) value);
            }

            value = JFXUtil.convert(model_widget.propBarColor().getValue());

            if ( !Objects.equals(value, jfx_node.getBarColor()) ) {
                jfx_node.setBarColor((Color) value);
            }

            value = model_widget.propStartFromZero().getValue();

            if ( !Objects.equals(value, jfx_node.isStartFromZero()) ) {
                jfx_node.setStartFromZero((boolean) value);
            }

        }

    }

    @Override
    protected void changeSkin ( final Gauge.SkinType skinType ) {

        super.changeSkin(skinType);

        jfx_node.setBarBackgroundColor(JFXUtil.convert(model_widget.propBarBackgroundColor().getValue()));
        jfx_node.setBarColor(JFXUtil.convert(model_widget.propBarColor().getValue()));
        jfx_node.setHighlightSections(false);
        jfx_node.setStartFromZero(model_widget.propStartFromZero().getValue());
        jfx_node.setTickLabelColor(Color.WHITE);

    }

    @Override
    protected Gauge createJFXNode ( ) throws Exception {

        Gauge.SkinType skinType = Gauge.SkinType.valueOf(model_widget.propSkin().getValue().name());
        Gauge gauge = super.createJFXNode(skinType);

        gauge.setBarBackgroundColor(JFXUtil.convert(model_widget.propBarBackgroundColor().getValue()));
        gauge.setBarColor(JFXUtil.convert(model_widget.propBarColor().getValue()));
        gauge.setHighlightSections(false);
        gauge.setStartFromZero(model_widget.propStartFromZero().getValue());
        gauge.setTickLabelColor(Color.WHITE);

        return gauge;

    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propSkin().addUntypedPropertyListener(this::lookChanged);
        model_widget.propBarBackgroundColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propBarColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propStartFromZero().addUntypedPropertyListener(this::lookChanged);

    }

    private void lookChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyLook.mark();
        toolkit.scheduleUpdate(this);
    }

}
