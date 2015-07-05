/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.io.FileInputStream;
import java.util.logging.LogManager;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@SuppressWarnings("nls")
public class EditorDemo extends Application
{
    private final String display_file = "../org.csstudio.display.builder.runtime.test/examples/main.opi";
    private EditorGUI editor;

    /** JavaFX main
     *  @throws Exception
     */
    public static void main(final String[] args) throws Exception
    {
        LogManager.getLogManager().readConfiguration(new FileInputStream("../org.csstudio.display.builder.runtime.test/examples/logging.properties"));

        launch(args);
    }

    /** JavaFX Start */
    @Override
    public void start(final Stage stage)
    {
        editor = new EditorGUI(stage);
        // Load model in background
        editor.loadModel(display_file);
        stage.setOnCloseRequest((WindowEvent event) -> editor.handleClose());
    }
}
