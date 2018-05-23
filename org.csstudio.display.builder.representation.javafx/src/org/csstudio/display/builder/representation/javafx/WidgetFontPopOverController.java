/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 23 May 2018
 */
public class WidgetFontPopOverController implements Initializable {

    @FXML private GridPane root;

    @FXML private TextField searchField;

    @Override
    public void initialize ( URL location, ResourceBundle resources ) {
    }

}
