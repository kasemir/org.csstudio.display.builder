/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.actions;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.representation.javafx.Messages;

import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/** Menu item that copies PV name of widget to clipboard
 *  @author Kay Kasemir
 */
public class CopyPVNameToClipboard extends MenuItem
{
    public CopyPVNameToClipboard(final Widget model_widget)
    {
        super(Messages.CopyPVName);
        setOnAction((event) ->
        {
            final Clipboard clip = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(model_widget.getProperty(behaviorPVName).getValue());
            clip.setContent(content);
        });
    }
}
