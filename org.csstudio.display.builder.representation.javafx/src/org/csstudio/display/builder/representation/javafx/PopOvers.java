/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx;


import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.controlsfx.control.PopOver;
import org.csstudio.display.builder.model.properties.ColorWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;


/**
 * A factory class for popover editors.
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 30 Nov 2017
 */
public class PopOvers {

    public static Optional<WidgetColor> editColor ( ColorWidgetProperty property, Pane field ) {

        Optional<WidgetColor> result = Optional.empty();

        try {

            URL fxml = PopOvers.class.getResource("WidgetColorPopOver.fxml");
            InputStream iStream = PopOvers.class.getResourceAsStream("messages.properties");
            ResourceBundle bundle = new PropertyResourceBundle(iStream);
            FXMLLoader fxmlLoader = new FXMLLoader(fxml, bundle);
            Node content = (Node) fxmlLoader.load();
            PopOver popOver = new PopOver();

            popOver.setContentNode(content);
            popOver.setDetachable(false);
            popOver.setDetached(false);
            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
            popOver.setHeaderAlwaysVisible(true);
            popOver.setTitle(MessageFormat.format(Messages.WidgetColorPopup_Title, property.getDescription()));
            popOver.setAnimated(true);
            popOver.setAutoHide(true);
            popOver.setCloseButtonEnabled(true);


            Node node = field.getChildren().get(1);
            Bounds bound = node.localToScreen(node.getBoundsInLocal());
            Point2D bottomAnchor = new Point2D(( bound.getMinX() + bound.getMaxX() ) / 2.0, bound.getMaxY());

            content.

//            Screen.getScreens().stream().filter(screen -> screen.getVisualBounds().contains(bottomAnchor)).forEach(screen -> );



            popOver.show(field.getChildren().get(1));














//            WidgetColorPopOver.getPopOver(widget_property.getDescription()).show(jfx_node.getChildren().get(1));
//            WidgetColorPopOver.getPopOver(widget_property.getDescription()).show(jfx_node);
        } catch ( IOException ex ) {
            // TODO Auto-generated catch block
            logger.log(Level.WARNING, "Unable to edit color.", ex);
        }

        return result;

    }

    private PopOvers ( ) {
    }

}
