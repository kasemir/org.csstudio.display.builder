/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/** Java FX Font Demo
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class FontDemoJavaFX extends Application
{
    public static void main(final String[] args)
    {
        launch(args);
    }

    @Override
    public void start(final Stage stage)
    {
        final Label label = new Label("Example Test XOXO pq__ 1234567890 (JFX)");
        final Font font = Font.font("Liberation Mono", 40.0);
        System.out.println(font);
        label.setFont(font);

        final Pane root = new Pane(label);

        final Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}