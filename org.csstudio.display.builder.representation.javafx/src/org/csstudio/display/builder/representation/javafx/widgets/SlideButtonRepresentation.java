/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx.widgets;


import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.logging.Level;

import org.controlsfx.control.ToggleSwitch;
import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.widgets.SlideButtonWidget;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;


/**
 * JavaFX representation of the SlideButton model.
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 21 Aug 2018
 */
public class SlideButtonRepresentation extends RegionBaseRepresentation<HBox, SlideButtonWidget> {

    /*
     * This code come as close as possible from BoolButtonRepresentation.
     */

    private final DirtyFlag dirty_representation = new DirtyFlag();
    private final DirtyFlag dirty_enablement     = new DirtyFlag();
    private final DirtyFlag dirty_value          = new DirtyFlag();

    /**
     * State: 0 or 1
     */
    private volatile int     on_state = 1;
    private volatile int     use_bit  = 0;
    private volatile Integer rt_value = 0;

    private volatile ToggleSwitch button;
    private volatile Label        label;

    private volatile String   background;
    private volatile Color    foreground;
    private volatile Color[]  state_colors;
    private volatile Color    value_color;
    private volatile String[] state_labels;
    private volatile String   value_label;

    @Override
    public HBox createJFXNode ( ) throws Exception {

        button = new ToggleSwitch();

        button.setMinSize(37, 20);
        button.setPrefSize(37, 20);
        button.setGraphicTextGap(0);
        button.getStylesheets().add(getClass().getResource("slidebutton.css").toExternalForm());
        button.setMnemonicParsing(false);
        button.selectedProperty().addListener( ( p, o, n ) -> handleSlide());

        label = new Label();

        label.setMaxWidth(Double.MAX_VALUE);
        label.setMnemonicParsing(false);
        HBox.setHgrow(label, Priority.ALWAYS);

        return new HBox(6, button, label);

    }

    private void confirm ( ) {

        final boolean prompt;

        switch ( model_widget.propConfirmDialog().getValue() ) {
            case BOTH:
                prompt = true;
                break;
            case PUSH:
                prompt = on_state == 0;
                break;
            case RELEASE:
                prompt = on_state == 1;
                break;
            case NONE:
            default:
                prompt = false;
        }

        if ( prompt ) {

            final String message = model_widget.propConfirmMessage().getValue();
            final String password = model_widget.propPassword().getValue();

            if ( password.length() > 0 ) {
                if ( toolkit.showPasswordDialog(model_widget, message, password) == null ) {
                    return;
                }
            } else if ( !toolkit.showConfirmationDialog(model_widget, message) ) {
                return;
            }

        }

        final int new_val = ( rt_value ^ ( ( use_bit < 0 ) ? 1 : ( 1 << use_bit ) ) );

        toolkit.fireWrite(model_widget, new_val);

    }

    private void handleSlide ( ) {
        logger.log(Level.FINE, "{0} slided", model_widget);
        Platform.runLater(this::confirm);
    }

}
