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
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayForegroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetMacros;
import static org.csstudio.display.builder.model.properties.InsetsWidgetProperty.runtimeInsets;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.Messages;
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
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

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
            Messages.GroupWidget_Name,
            "platform:/plugin/org.csstudio.display.builder.model/icons/group.png",
            Messages.GroupWidget_Description,
            Arrays.asList("org.csstudio.opibuilder.widgets.groupingContainer"))
    {
        @Override
        public Widget createWidget()
        {
            return new GroupWidget();
        }
    };

    /** Group widget style */
    public enum Style
    {
        GROUP(Messages.Style_Group),
        TITLE(Messages.Style_Title),
        LINE(Messages.Style_Line),
        NONE(Messages.Style_None);

        private final String name;

        private Style(final String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    /** Display 'style' */
    private static final WidgetPropertyDescriptor<Style> displayStyle =
        new WidgetPropertyDescriptor<Style>(
            WidgetPropertyCategory.DISPLAY, "style", Messages.Style)
    {
        @Override
        public EnumWidgetProperty<Style> createProperty(final Widget widget,
                                                        final Style default_value)
        {
            return new EnumWidgetProperty<Style>(this, widget, default_value);
        }
    };

    /** Custom WidgetConfigurator to load legacy file */
    private static class GroupWidgetConfigurator extends WidgetConfigurator
    {
        public GroupWidgetConfigurator(final Version xml_version)
        {
            super(xml_version);
        }

        @Override
        public boolean configureFromXML(final ModelReader model_reader, final Widget widget,
                                        final Element xml) throws Exception
        {
            if (! super.configureFromXML(model_reader, widget, xml))
                return false;

            if (xml_version.getMajor() < 2)
            {
                final GroupWidget group_widget = (GroupWidget) widget;
                // Translate border styles
                final int old_spec = XMLUtil.getChildInteger(xml, "border_style").orElse(13);
                if (old_spec == 0  ||  old_spec == 15)     // NONE, Empty
                    group_widget.style.setValue(Style.NONE);
                else if (old_spec >= 1  && old_spec <= 11) // Line, dash, raised, ..
                    group_widget.style.setValue(Style.LINE);
                else if (old_spec == 12)                   // Title Bar
                    group_widget.style.setValue(Style.TITLE);
                // else leave at default 13 = GROUP_BOX

                // Legacy had 'border_color'.
                // It wasn't used by Group Box style, which had built-in gray,
                // but was used by the label and other lines
                // -> Use as 'foreground_color'
                final Element text = XMLUtil.getChildElement(xml, "border_color");
                if (text != null)
                    group_widget.foreground.readFromXML(model_reader, text);
            }

            return true;
        }
    }

    private volatile WidgetProperty<Macros> macros;
    private volatile ChildrenProperty children;
    private volatile WidgetProperty<Style> style;
    private volatile WidgetProperty<WidgetColor> foreground;
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
        properties.add(style = displayStyle.createProperty(this, Style.GROUP));
        properties.add(foreground = displayForegroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.TEXT)));
        properties.add(background = displayBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.BACKGROUND)));
        properties.add(font = displayFont.createProperty(this, NamedWidgetFonts.DEFAULT));
        properties.add(insets = runtimeInsets.createProperty(this, new int[] { 0, 0 }));

        // Initial size
        positionWidth().setValue(300);
        positionHeight().setValue(200);
    }

    @Override
    public WidgetConfigurator getConfigurator(final Version persisted_version)
            throws Exception
    {
        return new GroupWidgetConfigurator(persisted_version);
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

    /** @return Display 'style' */
    public WidgetProperty<Style> displayStyle()
    {
        return style;
    }

    /** @return Display 'foreground_color' */
    public WidgetProperty<WidgetColor> displayForegroundColor()
    {
        return foreground;
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
