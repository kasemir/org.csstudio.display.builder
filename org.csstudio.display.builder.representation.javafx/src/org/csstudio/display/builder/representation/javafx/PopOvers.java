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
import java.util.Map;
import java.util.Optional;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.controlsfx.control.PopOver;
import org.csstudio.display.builder.model.properties.ColorWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;


/**
 * A factory class for popover editors.
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 30 Nov 2017
 */
public class PopOvers {

    private static final Map<Node, PopOver> POP_OVERS = new WeakHashMap<>();

    public static void editColor ( ColorWidgetProperty property, Node field, final Consumer<WidgetColor> consumer ) {

        PopOver popOver = POP_OVERS.get(field);

        if ( popOver != null && popOver.isShowing() ) {
            popOver.hide();
            POP_OVERS.remove(field);
        } else {
            try {

                URL fxml = PopOvers.class.getResource("WidgetColorPopOver.fxml");
                InputStream iStream = PopOvers.class.getResourceAsStream("messages.properties");
                ResourceBundle bundle = new PropertyResourceBundle(iStream);
                FXMLLoader fxmlLoader = new FXMLLoader(fxml, bundle);
                Node content = (Node) fxmlLoader.load();
                Node target = ( field instanceof Pane ) ? ((Pane) field).getChildren().get(1) : field;

                popOver = new PopOver();

                popOver.setAnimated(true);
                popOver.setArrowLocation(getBestArrowLocation(target));
                popOver.setAutoHide(false);
                popOver.setCloseButtonEnabled(false);
                popOver.setContentNode(content);
                popOver.setDetachable(false);
                popOver.setDetached(false);
                popOver.setHideOnEscape(true);
                popOver.setHeaderAlwaysVisible(true);
                popOver.setTitle(MessageFormat.format(Messages.WidgetColorPopOver_Title, property.getDescription()));

                WidgetColorPopOver controller = fxmlLoader.<WidgetColorPopOver>getController();

                controller.setInitialConditions(popOver, property, consumer);
                POP_OVERS.put(field, popOver);
                popOver.show(target, getBestArrowOffset(target));

            } catch ( IOException ex ) {
                logger.log(Level.WARNING, "Unable to edit color.", ex);
            }
        }

    }

    private static PopOver.ArrowLocation getBestArrowLocation ( Node target ) {

        Bounds bounds = target.localToScreen(target.getBoundsInLocal());
        Point2D center = new Point2D(
            ( bounds.getMinX() + bounds.getMaxX() ) / 2.0,
            ( bounds.getMinY() + bounds.getMaxY() ) / 2.0
        );
        Optional<Screen> screen = Screen.getScreens().stream().filter(s -> s.getVisualBounds().contains(center)).findFirst();

        if ( screen.isPresent() ) {

            Rectangle2D screenBounds = screen.get().getVisualBounds();
            double screenCenterX = ( screenBounds.getMinX() + screenBounds.getMaxX() ) / 2.0;
            double screenCenterY = ( screenBounds.getMinY() + screenBounds.getMaxY() ) / 2.0;

            if ( center.getY() < screenCenterY ) {
                //  More space below the target.
                if ( center.getX() < screenCenterX ) {
                    //  More space on the target's right side.
                    return PopOver.ArrowLocation.TOP_LEFT;
                } else {
                    //  More space on the target's left side.
                    return PopOver.ArrowLocation.TOP_RIGHT;
                }
            } else {
                //  More space above the target.
                if ( center.getX() < screenCenterX ) {
                    //  More space on the target's right side.
                    return PopOver.ArrowLocation.BOTTOM_LEFT;
                } else {
                    //  More space on the target's left side.
                    return PopOver.ArrowLocation.BOTTOM_RIGHT;
                }
            }

        } else {
            return PopOver.ArrowLocation.TOP_CENTER;
        }

    }

    private static double getBestArrowOffset ( Node target ) {

        Bounds bounds = target.localToScreen(target.getBoundsInLocal());
        Point2D center = new Point2D(
            ( bounds.getMinX() + bounds.getMaxX() ) / 2.0,
            ( bounds.getMinY() + bounds.getMaxY() ) / 2.0
        );
        Optional<Screen> screen = Screen.getScreens().stream().filter(s -> s.getVisualBounds().contains(center)).findFirst();

        if ( screen.isPresent() ) {

            Rectangle2D screenBounds = screen.get().getVisualBounds();
            double screenCenterY = ( screenBounds.getMinY() + screenBounds.getMaxY() ) / 2.0;

            if ( center.getY() < screenCenterY ) {
                //  More space below the target.
                return 1.5;
            } else {
                //  More space above the target.
                return -2.5;
            }

        } else {
            return 1.5;
        }

    }

    private PopOvers ( ) {
    }

}
