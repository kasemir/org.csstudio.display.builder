/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.controlsfx.control.PopOver;
import org.csstudio.display.builder.model.properties.NamedWidgetColor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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

    public static PopOver getPopOver ( String property ) throws IOException {

        URL fxml = WidgetColorPopOver.class.getResource("WidgetColorPopOver.fxml");
        InputStream iStream = WidgetColorPopOver.class.getResourceAsStream("messages.properties");
        ResourceBundle bundle = new PropertyResourceBundle(iStream);
        FXMLLoader fxmlLoader = new FXMLLoader(fxml, bundle);
        PopOver popOver = new PopOver();

        popOver.setContentNode((Node) fxmlLoader.load());
        popOver.setDetachable(false);
        popOver.setDetached(false);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.setHeaderAlwaysVisible(true);
        popOver.setTitle(MessageFormat.format(Messages.WidgetColorPopup_Title, property));
        popOver.setAnimated(true);
        popOver.setAutoHide(true);
        popOver.setCloseButtonEnabled(true);


        return popOver;

    }

    @FXML private ListView<NamedWidgetColor> color_names;

    @FXML private ColorPicker picker;

    @FXML private Slider red_slider;
    @FXML private Slider green_slider;
    @FXML private Slider blue_slider;
    @FXML private Slider alpha_slider;

    @FXML private Spinner<Integer> red_spinner;
    @FXML private Spinner<Integer> green_spinner;
    @FXML private Spinner<Integer> blue_spinner;
    @FXML private Spinner<Integer> alpha_spinner;

    @FXML private Button cancel_button;
    @FXML private Button ok_button;

    @FXML
    void cancelPressed( ActionEvent event ) {

    }

    @FXML
    void okPressed( ActionEvent event ) {

    }

}
