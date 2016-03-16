/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFont;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetMacros;
import static org.csstudio.display.builder.model.properties.InsetsWidgetProperty.runtimeInsets;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;

/** A Group Widget contains child widgets.
 *
 *  <p>In the editor, moving the group will move all the widgets inside the group.
 *  Groups are also a convenient way to copy and paste a collection of widgets.
 *
 *  <p>Model Widgets within the group use coordinates relative to the group,
 *  i.e. a child at (x, y) = (0, 0) would be in the left upper corner of the group
 *  and <em>not</em> in the left upper corner of the display.
 *
 *  <p>At runtime, the group may add a labeled border to visually frame
 *  its child widgets, which further offsets the child widgets by the width of
 *  the border.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class GroupWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("group", WidgetCategory.STRUCTURE,
            "Group",
            "platform:/plugin/org.csstudio.display.builder.model/icons/group.png",
            "Group of widgets",
            Arrays.asList("org.csstudio.opibuilder.widgets.groupingContainer"))
    {
        @Override
        public Widget createWidget()
        {
            return new GroupWidget();
        }
    };

    private volatile WidgetProperty<Macros> macros;
    private volatile ChildrenProperty children;
    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<WidgetFont> font;
    private volatile WidgetProperty<int[]> insets;

    public GroupWidget()
    {
        super(WIDGET_DESCRIPTOR.getType(), 400, 300);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(macros = widgetMacros.createProperty(this, new Macros()));
        properties.add(children = new ChildrenProperty(this));
        properties.add(background = displayBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.BACKGROUND)));
        properties.add(font = displayFont.createProperty(this, NamedWidgetFonts.DEFAULT));
        properties.add(insets = runtimeInsets.createProperty(this, new int[] { 0, 0 }));

        // Initial size
        positionWidth().setValue(300);
        positionHeight().setValue(200);
    }

    /** @return Widget 'macros' */
    public WidgetProperty<Macros> widgetMacros()
    {
        return macros;
    }

    /** @return Runtime 'children' */
    public ChildrenProperty runtimeChildren()
    {
        return children;
    }

    /** @return Display 'background_color' */
    public WidgetProperty<WidgetColor> displayBackgroundColor()
    {
        return background;
    }

    /** Group widget extends parent macros
     *  @return {@link Macros}
     */
    @Override
    public Macros getEffectiveMacros()
    {
        final Macros base = super.getEffectiveMacros();
        final Macros my_macros = widgetMacros().getValue();
        return Macros.merge(base, my_macros);
    }

    /** @return Display 'font' */
    public WidgetProperty<WidgetFont> displayFont()
    {
        return font;
    }

    /** @return Runtime 'insets' */
    public WidgetProperty<int[]> runtimeInsets()
    {
        return insets;
    }
}
