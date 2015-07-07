/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import org.csstudio.display.builder.model.MacroizedWidgetProperty;

import javafx.beans.property.StringProperty;

/** Bidirectional binding between a Widget and Java FX Property
 *  @author Kay Kasemir
 */
public class MacroizedWidgetPropertyBinding extends WidgetPropertyBinding<MacroizedWidgetProperty<?>>
{
    public MacroizedWidgetPropertyBinding(final StringProperty jfx_property, final MacroizedWidgetProperty<?> widget_property)
    {
        super(jfx_property, widget_property);
    }

    @Override
    String getWidgetText()
    {
        return widget_property.getSpecification();
    }

    @Override
    void setWidgetText(String text)
    {
        widget_property.setSpecification(text);
    }
}
