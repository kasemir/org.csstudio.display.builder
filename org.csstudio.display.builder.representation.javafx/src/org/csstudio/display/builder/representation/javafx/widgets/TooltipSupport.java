/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.logging.Level;

import org.csstudio.display.builder.model.MacroizedWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.macros.MacroValueProvider;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;

/** Support for tooltips
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TooltipSupport
{
    /** Attach tool tip
     *  @param node Node that should have the tool tip
     *  @param tooltip_property Tool tip to show
     */
    public static void attach(final Node node, final WidgetProperty<String> tooltip_property)
    {
        // Suppress tool tip if _initial_ text is empty.
        // In case a script changes the tool tip at runtime,
        // tool tip must have some initial non-empty value.
        // This was done for optimization:
        // Avoid listener and code to remove/add tooltip at runtime.
        if (tooltip_property.getValue().isEmpty())
            return;

        final Tooltip tooltip = new Tooltip();
        tooltip.setWrapText(true);
        // Evaluate the macros in tool tip specification each time
        // the tool tip is about to show
        tooltip.setOnShowing(event ->
        {
            final String spec = ((MacroizedWidgetProperty<?>)tooltip_property).getSpecification();
            final Widget widget = tooltip_property.getWidget();
            final MacroValueProvider macros = widget.getMacrosOrProperties();
            String expanded;
            try
            {
                expanded = MacroHandler.replace(macros, spec);
                tooltip.setText(expanded);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot evaluate tooltip of " + widget, ex);
                tooltip.setText(spec);
            }
        });

        // Unfortunately, no control over the timing: When does it show, for how long?
        // http://stackoverflow.com/questions/26854301/control-javafx-tooltip-delay
        // Java 9 will offer API, https://bugs.openjdk.java.net/browse/JDK-8090477
        Tooltip.install(node, tooltip);
    }
}
