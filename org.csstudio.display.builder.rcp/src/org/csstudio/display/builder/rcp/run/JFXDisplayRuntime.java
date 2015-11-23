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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

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

            try
            {
            	new RCP_JFXRepresentation().openNewWindow();
            }
            catch (Exception ex)
            {
            	ex.printStackTrace();
            }
        });
        
        final MenuItem item = new MenuItem("JFX Item");
        
        final ContextMenu menu = new ContextMenu(item);
        test.setContextMenu(menu);
        
        parent.getChildren().add(test);
    }
}
