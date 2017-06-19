/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx.widgets;


import org.csstudio.display.builder.model.widgets.SymbolWidget;
import org.csstudio.display.builder.representation.javafx.widgets.controls.Symbol;


/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 19 Jun 2017
 */
public class SymbolRepresentation extends RegionBaseRepresentation<Symbol, SymbolWidget> {

    @Override
    protected Symbol createJFXNode ( ) throws Exception {

        Symbol symbol = new Symbol();

        return symbol;

    }

}
