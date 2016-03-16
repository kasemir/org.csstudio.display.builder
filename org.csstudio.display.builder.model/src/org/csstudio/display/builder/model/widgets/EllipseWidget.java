/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayLineColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayLineWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayTransparent;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;

/** Widget that displays a static ellipse
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EllipseWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("ellipse", WidgetCategory.GRAPHIC,
            "Ellipse",
            "platform:/plugin/org.csstudio.display.builder.model/icons/ellipse.png",
            "An ellipse",
            Arrays.asList("org.csstudio.opibuilder.widgets.Ellipse"))
    {
        @Override
        public Widget createWidget()
        {
            return new EllipseWidget();
        }
    };

    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<Boolean> transparent;
    private volatile WidgetProperty<WidgetColor> line_color;
    private volatile WidgetProperty<Integer> line_width;

    public EllipseWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(background = displayBackgroundColor.createProperty(this, new WidgetColor(30, 144, 255)));
        properties.add(transparent = displayTransparent.createProperty(this, false));
        properties.add(line_color = displayLineColor.createProperty(this, new WidgetColor(0, 0, 255)));
        properties.add(line_width = displayLineWidth.createProperty(this, 3));
    }

    /** @return Display 'background_color' */
    public WidgetProperty<WidgetColor> displayBackgroundColor()
    {
        return background;
    }

    /** @return Display 'transparent' */
    public WidgetProperty<Boolean> displayTransparent()
    {
        return transparent;
    }

    /** @return Display 'line_color' */
    public WidgetProperty<WidgetColor> displayLineColor()
    {
        return line_color;
    }

    /** @return Display 'line_width' */
    public WidgetProperty<Integer> displayLineWidth()
    {
        return line_width;
    }
}
