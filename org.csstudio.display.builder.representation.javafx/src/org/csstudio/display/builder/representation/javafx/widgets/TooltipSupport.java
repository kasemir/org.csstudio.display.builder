/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.WidgetProperty;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;

/** Support for tooltips
 *  @author Kay Kasemir
 */
public class TooltipSupport
{
    public static void attach(final Node node, final WidgetProperty<String> toolkit_property)
    {
        // TODO Suppress tooltip if text is empty

        final Tooltip tooltip = new Tooltip();
        tooltip.setWrapText(true);
        tooltip.setOnShowing(event ->
        {
            final String text = toolkit_property.getValue();
            tooltip.setText(text);
        });

        Tooltip.install(node, tooltip);
    }
}
