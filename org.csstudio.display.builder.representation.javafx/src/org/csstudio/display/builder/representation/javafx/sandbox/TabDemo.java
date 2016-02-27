/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.sandbox;

import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/** Tabs..
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TabDemo extends Application
{
    @Override
    public void start(final Stage stage)
    {
        // TabPane with some tabs, fixed size
        final TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: red;");
        for (int i=0; i<3; ++i)
        {
            final Rectangle rect = new Rectangle(i*100, 100, 10+i*100, 20+i*80);
            rect.setFill(Color.BLUE);
            final Pane content = new Pane(rect);
            final Tab tab = new Tab("Tab " + (i+1), content);
            tab.setClosable(false);
            tabs.getTabs().add(tab);
        }
        tabs.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        tabs.setPrefSize(400, 300);

        // https://pixelduke.wordpress.com/2012/09/16/zooming-inside-a-scrollpane :
        // To allow zooming of widgets in 'model_parent',
        // it needs to be wrapped in another 'scroll_content' Group.
        // Otherwise scroll bars would enable/disable based on layout bounds,
        // regardless of zoom.
        final Group model_parent = new Group(tabs);
        model_parent.setScaleX(0.8);
        model_parent.setScaleY(0.8);
        final Group scroll_content = new Group(model_parent);
        final ScrollPane scroll = new ScrollPane(scroll_content);
        final Scene scene = new Scene(scroll);
        stage.setScene(scene);
        stage.show();

        // Unfortunately, the setup of ScrollPane -> Group -> Group -> TabPane
        // breaks the rendering of the TabPane.
        // While the red background shows the area occupied by TabPane,
        // the actual Tabs are missing..
        System.out.println("See anything?");
        new Thread(() ->
        {
            try
            {   TimeUnit.SECONDS.sleep(4); }
            catch (Exception e) {}
            Platform.runLater(() ->
            {   // .. until TabPane 'side' or tabMinWidth or .. properties
                // are twiddled to force a refresh
                tabs.setSide(Side.BOTTOM);
                tabs.setSide(Side.TOP);
                System.out.println("See it now?");
           });
        }).start();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}