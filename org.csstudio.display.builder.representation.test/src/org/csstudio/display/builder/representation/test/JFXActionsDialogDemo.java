/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.test;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo.Target;
import org.csstudio.display.builder.model.properties.WritePVActionInfo;
import org.csstudio.display.builder.representation.javafx.ActionsDialog;
import org.csstudio.display.builder.representation.javafx.MacrosDialog;

import javafx.application.Application;
import javafx.stage.Stage;

/** Demo of {@link MacrosDialog}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class JFXActionsDialogDemo  extends Application
{
    public static void main(final String[] args)
    {
        launch(args);
    }

    @Override
    public void start(final Stage stage)
    {
        final Macros macros = new Macros();
        macros.add("S", "Test");
        macros.add("N", "17");
        final List<ActionInfo> actions = Arrays.asList(
                new OpenDisplayActionInfo("Related Display", "../opi/some_file.opi", macros, Target.TAB),
                new WritePVActionInfo("Reset", "Test:CS:Reset", "1"));
        final ActionsDialog dialog = new ActionsDialog(actions);
        System.out.println(dialog.showAndWait());
    }
}
