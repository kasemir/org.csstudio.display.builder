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
import java.util.function.Consumer;

import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.persist.WidgetFontService;
import org.csstudio.display.builder.model.properties.NamedWidgetFont;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.csstudio.display.builder.model.properties.WidgetFontStyle;
import org.csstudio.javafx.PopOver;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 23 May 2018
 */
public class WidgetFontPopOverController implements Initializable {

    @FXML private GridPane root;

    @FXML private TextField searchField;

    @FXML private ListView<NamedWidgetFont> fontNames;
    @FXML private ListView<String> families;

    @FXML private ComboBox<WidgetFontStyle> styles;
    @FXML private ComboBox<String> sizes;

    @FXML private TextField preview;

    @FXML private Button cancelButton;
    @FXML private Button defaultButton;
    @FXML private Button okButton;

    private WidgetFont           defaultFont = null;
    private Consumer<WidgetFont> fontChangeConsumer;
    private PopOver              popOver;

    /*
     * ---- font property -----------------------------------------------------
     */
    private final ObjectProperty<WidgetFont> font = new SimpleObjectProperty<WidgetFont>(this, "font", WidgetFontService.get(NamedWidgetFonts.DEFAULT)) {
        @Override
        protected void invalidated() {

            WidgetFont fnt = get();

            if ( fnt == null ) {
                set(WidgetFontService.get(NamedWidgetFonts.DEFAULT));
            }

        }
    };

    ObjectProperty<WidgetFont> fontProperty() {
        return font;
    }

    WidgetFont getFont() {
        return font.get();
    }

    void setFont( WidgetFont font ) {
        this.font.set(font);
    }

    /*
     * -------------------------------------------------------------------------
     */
    @Override
    public void initialize ( URL location, ResourceBundle resources ) {
    }

    @FXML
    void cancelPressed ( ActionEvent event ) {
        if ( popOver != null ) {
            popOver.hide();
        }
    }

    @FXML
    void defaultPressed(ActionEvent event) {

        if ( fontChangeConsumer != null ) {
            fontChangeConsumer.accept(defaultFont);
        }

        cancelPressed(event);

    }

    @FXML
    void okPressed ( ActionEvent event ) {

        if ( fontChangeConsumer != null ) {
            fontChangeConsumer.accept(getFont());
        }

        cancelPressed(event);

    }

}
