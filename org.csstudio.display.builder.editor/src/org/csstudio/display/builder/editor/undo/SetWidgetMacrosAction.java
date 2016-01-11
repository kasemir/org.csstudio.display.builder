/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.display.builder.editor.undo;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.properties.MacrosWidgetProperty;
import org.csstudio.display.builder.util.undo.UndoableAction;

/** Action to update widget macros
 *  @author Kay Kasemir
 */
public class SetWidgetMacrosAction extends UndoableAction
{
    private final MacrosWidgetProperty property;
    private final Macros orig_macros;
    private final Macros macros;

    public SetWidgetMacrosAction(final MacrosWidgetProperty property,
                                 final Macros macros)
    {
        super(Messages.SetWidgetMacros);
        this.property = property;
        this.orig_macros = property.getValue();
        this.macros = macros;
    }

    @Override
    public void run()
    {
        property.setValue(macros);
    }

    @Override
    public void undo()
    {
        property.setValue(orig_macros);
    }
}
