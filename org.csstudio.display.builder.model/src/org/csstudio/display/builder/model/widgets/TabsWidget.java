/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.ModelPlugin.logger;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayDirection;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFont;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetMacros;
import static org.csstudio.display.builder.model.properties.InsetsWidgetProperty.runtimeInsets;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.StructuredWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.Direction;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

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

    // Property that describes one tab item
    private final static StructuredWidgetProperty.Descriptor displayTabItem =
            new StructuredWidgetProperty.Descriptor(WidgetPropertyCategory.DISPLAY, "tab", Messages.Tab_Item);

    /** Name, children of one tab */
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

    private static final WidgetPropertyDescriptor<Integer> tabHeight =
            CommonWidgetProperties.newIntegerPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "tab_height", Messages.Tab_Height);

    private static final WidgetPropertyDescriptor<Integer> activeTab =
            CommonWidgetProperties.newIntegerPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "active_tab", Messages.ActiveTab);


    /** Custom WidgetConfigurator to load legacy file */
    private static class TabsWidgetConfigurator extends WidgetConfigurator
    {
        public TabsWidgetConfigurator(final Version xml_version)
        {
            super(xml_version);
        }

        @Override
        public boolean configureFromXML(final ModelReader model_reader, final Widget widget,
                                        final Element xml) throws Exception
        {
            if (! super.configureFromXML(model_reader, widget, xml))
                return false;

            final Optional<Integer> count_info = XMLUtil.getChildInteger(xml, "tab_count");
            if (xml_version.getMajor() < 2  &&  count_info.isPresent())
            {   // Legacy org.csstudio.opibuilder.widgets.tab used <tab_count>,
                // Create matching number of tabs
                final int count = count_info.get();
                final TabsWidget tabs_widget = (TabsWidget)widget;
                final ArrayWidgetProperty<TabItemProperty> tabs = tabs_widget.displayTabs();
                while (count < tabs.size())
                    tabs.removeElement();
                while (count > tabs.size())
                    tabs.addElement();

                // Basics that apply to all tabs
                Optional<String> text = XMLUtil.getChildString(xml, "minimum_tab_height");
                if (text.isPresent())
                    tabs_widget.displayTabHeight().setValue(Integer.parseInt(text.get()));

                text = XMLUtil.getChildString(xml, "horizontal_tabs");
                if (text.isPresent() && text.get().equals("false"))
                    tabs_widget.displayDirection().setValue(Direction.VERTICAL);

                // Configure each tab from <tab_0_title>, <tab_1_title>, ...
                for (int i=0; i<count; ++i)
                {
                    text = XMLUtil.getChildString(xml, "tab_" + i + "_title");
                    if (text.isPresent())
                        tabs.getValue().get(i).name().setValue(text.get());
                }

                // Tab content was in sequence of
                // <widget typeId="org.csstudio.opibuilder.widgets.groupingContainer">
                // where detail was ignored except for the children of each group.
                int i = 0;
                for (Element content_xml : XMLUtil.getChildElements(xml, "widget"))
                {
                    if (! content_xml.getAttribute("typeId").contains("group"))
                    {
                        logger.log(Level.WARNING, "Legacy 'tab' widget misses content of tab " + i);
                        break;
                    }
                    model_reader.readWidgets(tabs.getValue().get(i).children(), content_xml);
                    ++i;
                }
            }
            return true;
        }
    }

    private volatile WidgetProperty<Macros> macros;
    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<WidgetFont> font;
    private volatile WidgetProperty<Integer> active;
    private volatile ArrayWidgetProperty<TabItemProperty> tabs;
    private volatile WidgetProperty<Direction> direction;
    private volatile WidgetProperty<Integer> tab_height;
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
        properties.add(active = activeTab.createProperty(this, 0));
        properties.add(tabs = displayTabs.createProperty(this, Arrays.asList(new TabItemProperty(this, 0),
                                                                             new TabItemProperty(this, 1))));
        properties.add(direction = displayDirection.createProperty(this, Direction.HORIZONTAL));
        properties.add(tab_height = tabHeight.createProperty(this, 30));
        properties.add(insets = runtimeInsets.createProperty(this, new int[] { 0, 0 }));

        // Initial size
        positionWidth().setValue(300);
        positionHeight().setValue(200);
    }

    private static String createTabText(final int index)
    {
        return NLS.bind(Messages.TabsWidget_TabNameFmt, index + 1);
    }

    @Override
    public WidgetConfigurator getConfigurator(final Version persisted_version)
            throws Exception
    {
        return new TabsWidgetConfigurator(persisted_version);
    }

    /** @return Widget 'macros' */
    public WidgetProperty<Macros> widgetMacros()
    {
        return macros;
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

    /** @return Display 'background_color' */
    public WidgetProperty<WidgetColor> displayBackgroundColor()
    {
        return background;
    }

    /** @return Display 'font' */
    public WidgetProperty<WidgetFont> displayFont()
    {
        return font;
    }

    /** @return Display 'active_tab' */
    public WidgetProperty<Integer> displayActiveTab()
    {
        return active;
    }

    /** @return Display 'tabs' */
    public ArrayWidgetProperty<TabItemProperty> displayTabs()
    {
        return tabs;
    }

    /** @return Display 'direction' */
    public WidgetProperty<Direction> displayDirection()
    {
        return direction;
    }

    /** @return Display 'tab_height' */
    public WidgetProperty<Integer> displayTabHeight()
    {
        return tab_height;
    }

    /** @return Runtime 'insets' */
    public WidgetProperty<int[]> runtimeInsets()
    {
        return insets;
    }
}
