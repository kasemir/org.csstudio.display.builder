/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.display.builder.editor.undo;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.properties.FontWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.csstudio.display.builder.model.undo.UndoableAction;

/** Action to update widget font
 *  @author Kay Kasemir
 */
public class SetWidgetFontAction extends UndoableAction
{
    private final FontWidgetProperty property;
    private final WidgetFont orig_font;
    private final WidgetFont font;

    public SetWidgetFontAction(final FontWidgetProperty property,
                                 final WidgetFont font)
    {
        super(Messages.SetWidgetFont);
        this.property = property;
        this.orig_font = property.getValue();
        this.font = font;
    }

    @Override
    public void run()
    {
        property.setValue(font);
    }

    @Override
    public void undo()
    {
        property.setValue(orig_font);
    }
}
