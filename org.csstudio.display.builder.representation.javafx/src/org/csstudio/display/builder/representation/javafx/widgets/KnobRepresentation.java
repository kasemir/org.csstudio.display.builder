/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx.widgets;


import org.csstudio.display.builder.model.widgets.KnobWidget;

import se.europeanspallationsource.javafx.control.knobs.Knob;


/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 21 Aug 2017
 */
public class KnobRepresentation extends BaseKnobRepresentation<Knob, KnobWidget> {

    @Override
    protected Knob createKnob ( ) {
        return new Knob();
    }

}
