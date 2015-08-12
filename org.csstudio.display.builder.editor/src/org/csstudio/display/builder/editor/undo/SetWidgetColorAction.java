/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.display.builder.editor.undo;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.properties.ColorWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;

/** Action to update widget color
 *  @author Kay Kasemir
 */
public class SetWidgetColorAction extends UndoableAction
{
    private final ColorWidgetProperty property;
    private final WidgetColor orig_color;
    private final WidgetColor color;

    public SetWidgetColorAction(final ColorWidgetProperty property,
                                final WidgetColor color)
    {
        super(Messages.SetWidgetColor);
        this.property = property;
        this.orig_color = property.getValue();
        this.color = color;
    }

    @Override
    public void run()
    {
        property.setValue(color);
    }

    @Override
    public void undo()
    {
        property.setValue(orig_color);
    }
}
