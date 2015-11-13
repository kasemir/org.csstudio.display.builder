/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import javafx.scene.Group;
import javafx.scene.control.Button;

/** Display builder runtime with JFX representation
 *  @author Kay Kasemir
 */
public class JFXDisplayRuntime
{


    public void representModel(final Group parent, final Object model) // TODO DisplayModel
    {
        final Button test = new Button("Test");
        test.setOnAction((event) ->
        {
            System.out.println("Pressed!");

            new RCP_JFXRepresentation().openNewWindow();
        });

        parent.getChildren().add(test);
    }
}
