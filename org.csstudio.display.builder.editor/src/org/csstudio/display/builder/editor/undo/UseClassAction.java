/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.undo;

import org.csstudio.display.builder.model.MacroizedWidgetProperty;
import org.csstudio.display.builder.model.WidgetClassSupport;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.WidgetClassesService;
import org.csstudio.display.builder.util.undo.UndoableAction;

/** Action to update 'use_class' of property
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class UseClassAction extends UndoableAction
{
    private final WidgetProperty<?> property;
    private final boolean use_class;
    private Object orig_value;

    public UseClassAction(final WidgetProperty<?> widget_property, final boolean use_class)
    {
        super(widget_property.getName() + (use_class ? " - use widget class" : " - ignore widget class"));
        this.property = widget_property;
        this.use_class = use_class;
    }

    @Override
    public void run()
    {
        // Save a custom value for un-do
        if (use_class)
        {
            if (property instanceof MacroizedWidgetProperty)
                orig_value = ((MacroizedWidgetProperty<?>)property).getSpecification();
            else
                orig_value = property.getValue();
        }
        update(use_class);
    }

    @Override
    public void undo()
    {
        update(! use_class);
        // Restore custom value
        if (use_class  &&  orig_value != null)
        {
            if (property instanceof MacroizedWidgetProperty)
                ((MacroizedWidgetProperty<?>)property).setSpecification((String) orig_value);
            else
                try
                {
                    property.setValueFromObject(orig_value);
                }
                catch (Exception ex)
                {
                    // Ignore
                }
        }
    }

    private void update(final boolean use_class)
    {
        property.useWidgetClass(use_class);
        if (use_class)
        {
            final WidgetClassSupport classes = WidgetClassesService.getWidgetClasses();
            if (classes != null)
                classes.apply(property);
        }
    }
}
