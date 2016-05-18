/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFont;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayForegroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayText;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetMacros;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/** Widget that provides button for invoking actions
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ActionButtonWidget extends VisibleWidget
{
    final static Logger logger = Logger.getLogger(ActionButtonWidget.class.getName());

    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
            new WidgetDescriptor("action_button", WidgetCategory.CONTROL,
                    "Action Button",
                    "platform:/plugin/org.csstudio.display.builder.model/icons/action_button.png",
                    "Button that can open related displays or write PVs",
                    Arrays.asList("org.csstudio.opibuilder.widgets.ActionButton", "org.csstudio.opibuilder.widgets.MenuButton"))
    {
        @Override
        public Widget createWidget()
        {
            return new ActionButtonWidget();
        }
    };

    /** Custom configurator to read legacy *.opi files */
    private static class ActionButtonConfigurator extends WidgetConfigurator
    {
        public ActionButtonConfigurator(final Version xml_version)
        {
            super(xml_version);
        }

        @Override
        public boolean configureFromXML(final ModelReader model_reader, final Widget widget, final Element xml)
                throws Exception
        {
            final String typeId = xml.getAttribute("typeId");
            final boolean is_menu = typeId.equals("org.csstudio.opibuilder.widgets.MenuButton");

            if (is_menu)
            {
                //Legacy Menu Buttons with actions from PV should be processed as combo boxes, not action buttons
                final Element frompv_el = XMLUtil.getChildElement(xml, "actions_from_pv");
                if ((frompv_el == null) || (XMLUtil.getString(frompv_el).equals("true")))
                    return false;

                //Menu buttons used "label" instead of text
                final Element label_el = XMLUtil.getChildElement(xml, "label");

                if (label_el != null)
                {
                    final Document doc = xml.getOwnerDocument();
                    Element the_text = doc.createElement(displayText.getName());

                    if (label_el.getFirstChild() != null)
                    {
                        the_text.appendChild(label_el.getFirstChild().cloneNode(true));
                    }
                    else
                    {
                        Text the_label = doc.createTextNode("Menu Button Label");
                        the_text.appendChild(the_label);
                    }
                    xml.appendChild(the_text);
                }
            }

            super.configureFromXML(model_reader, widget, xml);
            return true;
        }
    }

    @Override
    public WidgetConfigurator getConfigurator(final Version persisted_version)
            throws Exception
    {
        return new ActionButtonConfigurator(persisted_version);
    }

    private volatile WidgetProperty<Macros> macros;
    private volatile WidgetProperty<String> text;
    private volatile WidgetProperty<WidgetFont> font;
    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<WidgetColor> foreground;

    public ActionButtonWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(behaviorPVName.createProperty(this, ""));
        properties.add(macros = widgetMacros.createProperty(this, new Macros()));
        properties.add(text = displayText.createProperty(this, "$(actions)"));
        properties.add(font = displayFont.createProperty(this, NamedWidgetFonts.DEFAULT));
        properties.add(background = displayBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.BUTTON_BACKGROUND)));
        properties.add(foreground = displayForegroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.TEXT)));
    }

    /** @return Widget 'macros' */
    public WidgetProperty<Macros> widgetMacros()
    {
        return macros;
    }

    /** @return Display 'text' */
    public WidgetProperty<String> displayText()
    {
        return text;
    }

    /** @return Display 'font' */
    public WidgetProperty<WidgetFont> displayFont()
    {
        return font;
    }

    /** @return Display 'background_color' */
    public WidgetProperty<WidgetColor> displayBackgroundColor()
    {
        return background;
    }

    /** @return Display 'foreground_color' */
    public WidgetProperty<WidgetColor> displayForegroundColor()
    {
        return foreground;
    }


    /** Action button widget extends parent macros
     *  @return {@link Macros}
     */
    @Override
    public Macros getEffectiveMacros()
    {
        final Macros base = super.getEffectiveMacros();
        final Macros my_macros = widgetMacros().getValue();
        return Macros.merge(base, my_macros);
    }
}
