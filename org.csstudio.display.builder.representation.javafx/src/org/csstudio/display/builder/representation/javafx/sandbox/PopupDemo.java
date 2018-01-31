/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.sandbox;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Stage;

/** Basic {@link Popup} demo
 *
 *  <p>Button that toggles popup.
 *  Popup follows when window is moved.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PopupDemo extends Application
{
    @Override
    public void start(final Stage stage)
    {
        final Popup popup = new Popup();

        final BorderPane content = new BorderPane(new Label("Center"), new Label("Top"), new Label("Right"), new Label("Bottom"), new Label("Left"));
        final Rectangle background = new Rectangle(100, 100, Color.LIGHTGRAY);

        final InvalidationListener resize_background = p ->
        {
            background.setWidth(content.getWidth());
            background.setHeight(content.getHeight());
        };
        content.widthProperty().addListener(resize_background);
        content.heightProperty().addListener(resize_background);

        final StackPane stack = new StackPane(background, content);
        popup.getContent().addAll(stack);

        final Button toggle_popup = new Button("Popup");
        final ChangeListener<Number> track_moves = (p, old, current) ->
        {
            // Position popup just below button
            final Bounds bounds = toggle_popup.localToScreen(toggle_popup.getBoundsInLocal());
            popup.setX(bounds.getMinX());
            popup.setY(bounds.getMaxY());
        };
        toggle_popup.setOnAction(event ->
        {
            if (popup.isShowing())
            {
                popup.hide();
                toggle_popup.getScene().getWindow().xProperty().removeListener(track_moves);
                toggle_popup.getScene().getWindow().yProperty().removeListener(track_moves);
            }
            else
            {
                toggle_popup.getScene().getWindow().xProperty().addListener(track_moves);
                toggle_popup.getScene().getWindow().yProperty().addListener(track_moves);
                track_moves.changed(null, null, null);
                popup.show(stage);
            }
        });

        final BorderPane layout = new BorderPane(toggle_popup);
        stage.setScene(new Scene(layout));
        stage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
