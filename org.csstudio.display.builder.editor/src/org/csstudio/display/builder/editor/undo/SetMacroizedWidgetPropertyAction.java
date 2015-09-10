/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.undo;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.MacroizedWidgetProperty;
import org.csstudio.display.builder.model.undo.UndoableAction;
import org.eclipse.osgi.util.NLS;

/** Action to update widget property
 *  @author Kay Kasemir
 */
public class SetMacroizedWidgetPropertyAction extends UndoableAction
{
    private final MacroizedWidgetProperty<?> widget_property;
    private final String orig_text;
    private final String text;

    public SetMacroizedWidgetPropertyAction(final MacroizedWidgetProperty<?> widget_property,
                                      final String text)
    {
        super(NLS.bind(Messages.SetPropertyFmt, widget_property.getDescription()));
        this.widget_property = widget_property;
        this.orig_text = widget_property.getSpecification();
        this.text = text;
    }

    @Override
    public void run()
    {
        widget_property.setSpecification(text);
    }

    @Override
    public void undo()
    {
        widget_property.setSpecification(orig_text);
    }
}
