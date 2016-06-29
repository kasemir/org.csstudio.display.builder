/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.sandbox;

import java.util.Arrays;
import java.util.List;

import org.csstudio.javafx.StringTable;
import org.csstudio.javafx.StringTableListener;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/** Table demo
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TableDemo extends Application
{
    public static void main(final String[] args)
    {
        launch(args);
    }

    @Override
    public void start(final Stage stage)
    {
        // Example data
        final List<String> headers = Arrays.asList("Left", "Middle", "Right");
        final List<List<String>> data = Arrays.asList(
                Arrays.asList("One", "Two" ),
                Arrays.asList("Uno", "Due", "Tres"));

        // Table
        final StringTable table = new StringTable(true);
        table.setHeaders(headers);
        table.setData(data);

        table.setListener(new StringTableListener()
        {
            @Override
            public void dataChanged(StringTable table)
            {
                System.out.println("Data has changed");
            }
        });

        // Example scene
        final Label label = new Label("Demo:");
        final Button new_data = new Button("New Data");
        new_data.setOnAction(event ->
        {
            table.setHeaders(Arrays.asList("A", "B"));
            table.setData(Arrays.asList(
                    Arrays.asList("A 1", "A 2"),
                    Arrays.asList("B 1", "B 2")));
        });

        final BorderPane layout = new BorderPane();
        layout.setTop(label);
        layout.setCenter(table);
        layout.setRight(new_data);

        final Scene scene = new Scene(layout, 800, 700);
        stage.setScene(scene);
        stage.setTitle("Table Demo");
        stage.setOnCloseRequest(event ->
        {   // Fetch data from table view
            System.out.println(table.getHeaders());
            for (List<String> row : table.getData())
                System.out.println(row);

            System.out.println("Original data:");
            for (List<String> row : data)
                System.out.println(row);
        });
        stage.show();
    }
}
