/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.undo;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.WidgetClassSupport;
import org.csstudio.display.builder.model.persist.WidgetClassesService;
import org.csstudio.display.builder.model.properties.WidgetClassProperty;
import org.csstudio.display.builder.util.undo.UndoableAction;
import org.eclipse.osgi.util.NLS;

/** Action to update widget class
 *  @author Kay Kasemir
 */
public class SetWidgetClassAction extends UndoableAction
{
    private final WidgetClassProperty widget_property;
    private final String orig_value, value;

    public SetWidgetClassAction(final WidgetClassProperty widget_property,
                                final String value)
    {
        super(NLS.bind(Messages.SetPropertyFmt, widget_property.getDescription()));
        this.widget_property = widget_property;
        this.orig_value = widget_property.getValue();
        this.value = value;
    }

    @Override
    public void run()
    {
        setClass(value);
    }

    @Override
    public void undo()
    {
        setClass(orig_value);
    }

    private void setClass(final String widget_class)
    {
        widget_property.setValue(widget_class);
        final WidgetClassSupport class_support = WidgetClassesService.getWidgetClasses();
        if (class_support != null)
            class_support.apply(widget_property.getWidget());
    }
}
