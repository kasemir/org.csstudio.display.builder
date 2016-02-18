/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.sandbox;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
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
    public void start(Stage primaryStage)
    {
        final TabPane tabs = new TabPane();
        // Where tabs are shown
        // tabs.setSide(Side.LEFT);
        // Debug: Show tab area
        tabs.setStyle("-fx-background-color: mediumaquamarine;");

        final int N = 3;
        final Pane[] content = new Pane[N];
        for (int i=0; i<N; ++i)
        {
            content[i] = new Pane();
            final Tab tab = new Tab("Tab " + (i+1), content[i]);
            tab.setClosable(false); // !!
            tabs.getTabs().add(tab);
        }

        for (int i=0; i<N; ++i)
        {
            final Rectangle rect = new Rectangle(i*100, 100, 10+i*100, 20+i*80);
            rect.setFill(Color.BLUE);
            content[i].getChildren().add(rect);
        }

        tabs.getSelectionModel().selectedIndexProperty().addListener((t, o, selected) ->
        {
            System.out.println("Active Tab: " + selected);
//            System.out.println("Active Tab: " + tabs.getSelectionModel().getSelectedIndex());
        });

        final BorderPane pane = new BorderPane(tabs);
        Scene scene = new Scene(pane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}