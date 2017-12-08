/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.sandbox;

import org.csstudio.javafx.PopOver;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/** {@link PopOver} demo
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PopOverDemo extends Application
{
    @Override
    public void start(final Stage stage)
    {
        final BorderPane content1 = new BorderPane(new TextField("Center"), new Label("Top"), new Label("Right"), new Label("Bottom"), new Label("Left"));
        final PopOver popover1 = new PopOver(content1);

        final Button toggle_popup1 = new Button("Popup 1");
        toggle_popup1.setOnAction(event ->
        {
            if (popover1.isShowing())
                popover1.hide();
            else
                popover1.show(toggle_popup1);

        });

        final BorderPane content2 = new BorderPane(new TextField("Center"), new Label("Top"), new Label("Right"), new Label("Bottom"), new Label("Left"));
        final PopOver popover2 = new PopOver(content2);

        final Button toggle_popup2 = new Button("Popup 2");
        toggle_popup2.setOnAction(event ->
        {
            if (popover2.isShowing())
                popover2.hide();
            else
                popover2.show(toggle_popup2);

        });

        final HBox layout = new HBox(10, toggle_popup1, toggle_popup2);
        stage.setScene(new Scene(layout, 400, 300));
        stage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
