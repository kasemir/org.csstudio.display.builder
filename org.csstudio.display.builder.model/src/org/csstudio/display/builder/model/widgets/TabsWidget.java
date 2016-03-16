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

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.StructuredWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.eclipse.osgi.util.NLS;

/** A Widget that arranges child widgets in 'tabs'.
 *
 *  <p>The widget has several tabs described by
 *  the {@link TabItemProperty}, each of which
 *  holds a list of child widgets.
 *
 *  <p>The 'parent' of those widgets is this Widget.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TabsWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("tabs", WidgetCategory.STRUCTURE,
            Messages.TabsWidget_Name,
            "platform:/plugin/org.csstudio.display.builder.model/icons/tabs.png",
            Messages.TabsWidget_Description,
            Arrays.asList("org.csstudio.opibuilder.widgets.tab"))
    {
        @Override
        public Widget createWidget()
        {
            return new TabsWidget();
        }
    };

    // TODO Custom WidgetConfigurator to load legacy file

    // Property that describes one tab item
    private final static StructuredWidgetProperty.Descriptor displayTabItem =
            new StructuredWidgetProperty.Descriptor(WidgetPropertyCategory.DISPLAY, "tab", Messages.Tab_Item);

    public static class TabItemProperty extends StructuredWidgetProperty
    {
        protected TabItemProperty(final Widget widget, final int index)
        {
            super(displayTabItem, widget,
                  Arrays.asList(CommonWidgetProperties.widgetName.createProperty(widget, createTabText(index)),
                                new ChildrenProperty(widget)));
        }

        public WidgetProperty<String> name()
        {
            return getElement(0);
        }

        public ChildrenProperty children()
        {
            final WidgetProperty<List<Widget>> c = getElement(1);
            return (ChildrenProperty)c;
        }
    };

    private static final ArrayWidgetProperty.Descriptor<TabItemProperty> displayTabs =
            new ArrayWidgetProperty.Descriptor<>(WidgetPropertyCategory.DISPLAY, "tabs", Messages.TabsWidget_Name,
                    (widget, index) -> new TabItemProperty(widget, index));

    private static final WidgetPropertyDescriptor<Integer> activeTab =
            CommonWidgetProperties.newIntegerPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "active_tab", Messages.ActiveTab);

    private volatile WidgetProperty<Macros> macros;
    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<WidgetFont> font;
    private volatile ArrayWidgetProperty<TabItemProperty> tabs;
    private volatile WidgetProperty<Integer> active;
    private volatile WidgetProperty<int[]> insets;

    public TabsWidget()
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
        properties.add(tabs = displayTabs.createProperty(this, Arrays.asList(new TabItemProperty(this, 0),
                                                                             new TabItemProperty(this, 1))));
        properties.add(active = activeTab.createProperty(this, 0));
        properties.add(insets = runtimeInsets.createProperty(this, new int[] { 0, 0 }));

        // Initial size
        positionWidth().setValue(300);
        positionHeight().setValue(200);
    }

    private static String createTabText(final int index)
    {
        return NLS.bind(Messages.TabsWidget_TabNameFmt, index + 1);
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
    public ArrayWidgetProperty<TabItemProperty> displayTabs()
    {
        return tabs;
    }

    /** @return Display 'active_tab' */
    public WidgetProperty<Integer> displayActiveTab()
    {
        return active;
    }

    /** @return Runtime 'insets' */
    public WidgetProperty<int[]> runtimeInsets()
    {
        return insets;
    }
}
