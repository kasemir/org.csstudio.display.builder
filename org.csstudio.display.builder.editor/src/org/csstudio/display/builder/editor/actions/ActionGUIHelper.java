/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.actions;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/** Description of an action
 *
 *  Wraps the functionality, i.e. icon, tool tip, and what to execute,
 *  for use in a Java FX Button or Eclipse Action.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ActionGUIHelper
{
    public static Button createButton(final ActionDescription action)
    {
        final Button button = new Button();
        try
        {
            button.setGraphic(new ImageView(new Image(action.getIconStream())));
        }
        catch (final Exception ex)
        {
            Logger.getLogger(ActionGUIHelper.class.getName())
                  .log(Level.WARNING, "Cannot load action icon", ex);
        }
        button.setTooltip(new Tooltip(action.getToolTip()));
        button.setOnAction(event -> action.run(true));
        return button;
    }

    public static ToggleButton createToggleButton(final ActionDescription action)
    {
        final ToggleButton button = new ToggleButton();
        try
        {
            button.setGraphic(new ImageView(new Image(action.getIconStream())));
        }
        catch (final Exception ex)
        {
            Logger.getLogger(ActionGUIHelper.class.getName())
                  .log(Level.WARNING, "Cannot load action icon", ex);
        }
        button.setTooltip(new Tooltip(action.getToolTip()));
        button.selectedProperty()
              .addListener((final ObservableValue<? extends Boolean> observable,
                            final Boolean old_value, final Boolean enabled) ->
        {
            action.run(enabled);
        });
        return button;
    }
}
