/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx;


import org.csstudio.display.builder.model.properties.NamedWidgetColor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;


/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 29 Nov 2017
 */
public class WidgetColorPopOver {

    @FXML
    private ListView<NamedWidgetColor> color_names;

    @FXML
    private ColorPicker picker;

    @FXML
    private Slider red_slider;
    @FXML
    private Slider green_slider;
    @FXML
    private Slider blue_slider;
    @FXML
    private Slider alpha_slider;

    @FXML
    private Spinner<Integer> red_spinner;
    @FXML
    private Spinner<Integer> green_spinner;
    @FXML
    private Spinner<Integer> blue_spinner;
    @FXML
    private Spinner<Integer> alpha_spinner;

    @FXML
    private Button cancel_button;
    @FXML
    private Button ok_button;

    @FXML
    void cancelPressed ( ActionEvent event ) {

    }

    @FXML
    void okPressed ( ActionEvent event ) {

    }

}
