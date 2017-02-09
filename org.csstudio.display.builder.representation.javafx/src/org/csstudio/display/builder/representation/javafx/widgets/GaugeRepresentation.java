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

import eu.hansolo.medusa.Gauge;


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

        }

    }

    @Override
    protected Gauge createJFXNode ( ) throws Exception {

        Gauge.SkinType skinType = Gauge.SkinType.valueOf(model_widget.propSkin().getValue().name());
        Gauge gauge = super.createJFXNode(skinType);

        gauge.setHighlightSections(false);

        return gauge;

    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propSkin().addUntypedPropertyListener(this::lookChanged);

    }

    private void lookChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyLook.mark();
        toolkit.scheduleUpdate(this);
    }

}
