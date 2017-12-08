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
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.javafx.PopOver;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Region;


/**
 * A factory class for popover editors.
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 30 Nov 2017
 */
public class PopOvers {

    private static final Map<Region, PopOver> POP_OVERS = new WeakHashMap<>();

    public static void editColor (
        final String propertyName,
        final WidgetColor originalWidgetColor,
        final WidgetColor defaultWidgetColor,
        final Region owner,
        final Consumer<WidgetColor> colorChangeConsumer
    ) {
        PopOver popOver = POP_OVERS.remove(owner);

        if (popOver != null)
        {   // Toggle existing popover
            if (popOver.isShowing())
                popOver.hide();
            else
                popOver.show(owner);
            return;
        }

        // Create popover
        try
        {
            URL fxml = PopOvers.class.getResource("WidgetColorPopOver.fxml");
            InputStream iStream = PopOvers.class.getResourceAsStream("messages.properties");
            ResourceBundle bundle = new PropertyResourceBundle(iStream);
            FXMLLoader fxmlLoader = new FXMLLoader(fxml, bundle);
            Node content = (Node) fxmlLoader.load();

            final PopOver fPopOver = new PopOver(content);

            WidgetColorPopOverController controller = fxmlLoader.<WidgetColorPopOverController>getController();

            controller.setInitialConditions(fPopOver, originalWidgetColor, defaultWidgetColor, colorChangeConsumer);
            POP_OVERS.put(owner, fPopOver);
            fPopOver.show(owner);

        }
        catch (IOException ex)
        {
            logger.log(Level.WARNING, "Unable to edit color.", ex);
        }
    }

    private PopOvers ( ) {
    }
}
