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
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayText;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetMacros;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.properties.StringWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;

/** A Widget that arranges child widgets in 'tabs'.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TabWidget extends Widget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("tab", WidgetCategory.STRUCTURE,
            "Tab",
            "platform:/plugin/org.csstudio.display.builder.model/icons/tab.png",
            "Group of tabs",
            Arrays.asList("org.csstudio.opibuilder.widgets.tab"))
    {
        @Override
        public Widget createWidget()
        {
            return new TabWidget();
        }
    };

    // TODO Custom WidgetConfigurator to load legacy file

    private static final ArrayWidgetProperty.Descriptor<WidgetProperty<String>> displayTabs =
            new ArrayWidgetProperty.Descriptor<>(WidgetPropertyCategory.DISPLAY, "tabs", "Tabs", // TODO Externalize
                    (widget, index) -> ((TabWidget)widget).createTabText(index));

    // XXX Legacy Tab held a 'group' for each tab.
    // Editor only affected the selected tab's group.

    private volatile WidgetProperty<Macros> macros;
    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<WidgetFont> font;
    private volatile ArrayWidgetProperty<WidgetProperty<String>> tabs;

    public TabWidget()
    {
        super(WIDGET_DESCRIPTOR.getType(), 400, 300);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(macros = widgetMacros.createProperty(this, new Macros()));
        properties.add(background = displayBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.BACKGROUND)));
        properties.add(font = displayFont.createProperty(this, NamedWidgetFonts.DEFAULT));
        properties.add(tabs = displayTabs.createProperty(this, Arrays.asList(createTabText(0), createTabText(1))));

        // Initial size
        positionWidth().setValue(300);
        positionHeight().setValue(200);
    }

    private WidgetProperty<String> createTabText(final int index)
    {
        return new StringWidgetProperty(displayText, this, "Tab " + (index + 1));
    }

    /** @return Widget 'macros' */
    public WidgetProperty<Macros> widgetMacros()
    {
        return macros;
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

    /** @return Display 'tabs' */
    public ArrayWidgetProperty<WidgetProperty<String>> displayTabs()
    {
        return tabs;
    }
}
