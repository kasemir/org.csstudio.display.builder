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
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/** Spinner demo
 *
 *  @author Kay Kasemir
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class SpinnerDemo extends Application
{
    public static void main(final String[] args)
    {
        launch(args);
    }

    @Override
    public void start(final Stage stage)
    {
        final Label label = new Label("Demo:");

        SpinnerValueFactory<Double> svf = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10);
        Spinner<Double> spinner = new Spinner<>();
        spinner.setValueFactory(svf);
        spinner.setEditable(true);
        //TODO: need to figure out how to override Spinner and/or factory methods, to format like TextUpdate
        spinner.setPrefWidth(80);

        spinner.valueProperty().addListener((prop, old, value) ->
        {
            System.out.println("Value: " + value);
        });

        final HBox root = new HBox(label, spinner);

        final Scene scene = new Scene(root, 800, 700);
        stage.setScene(scene);
        stage.setTitle("Spinner Demo");

        stage.show();
    }
}
