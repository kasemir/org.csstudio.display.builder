/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.sandbox;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.csstudio.display.builder.representation.javafx.JFXRepresentation;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/** Navigation Tab demo
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class NavigationTabsDemo extends Application
{
    @Override
    public void start(final Stage stage)
    {
        final NavigationTabs nav_tabs = new NavigationTabs();

        final List<String> tabs = IntStream.range(1, 10).mapToObj(i -> "Step" + i).collect(Collectors.toList());
        nav_tabs.setTabs(tabs);
        nav_tabs.setButtonSize(80, 40);
        nav_tabs.setButtonSpacing(5);

        // nav_tabs.setDirection(Direction.HORIZONTAL);

        nav_tabs.setContent(new Rectangle(20, 40, 200, 100));

        nav_tabs.addListener(index ->
        {
            final Label label = new Label("You selected tab " + (index + 1));
            label.setLayoutX(index * 10);
            label.setLayoutY(index * 15);
            nav_tabs.setContent(label);
        });


        final Scene scene = new Scene(nav_tabs);
        // Enable scene debugging?
        //   ScenicView.show(scene);

        JFXRepresentation.setSceneStyle(scene);
        stage.setTitle("Navigation Tab Demo");
        stage.setScene(scene);
        stage.setWidth(800);
        stage.setHeight(600);
        stage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}