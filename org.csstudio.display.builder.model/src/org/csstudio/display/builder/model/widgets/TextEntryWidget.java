/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimePropEnabled;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propFont;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propForegroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propFormat;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propPrecision;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propShowUnits;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propWrapWords;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.FormatOption;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that displays a changing text
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TextEntryWidget extends PVWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("textentry", WidgetCategory.CONTROL,
            "Text Entry",
            "platform:/plugin/org.csstudio.display.builder.model/icons/textentry.png",
            "Text field that writes entered values to PV",
            Arrays.asList("org.csstudio.opibuilder.widgets.TextInput"))
    {
        @Override
        public Widget createWidget()
        {
            return new TextEntryWidget();
        }
    };

    public static final WidgetPropertyDescriptor<Boolean> propMultiLine =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "multi_line", Messages.WidgetProperties_MultiLine);

    private static class CustomWidgetConfigurator extends WidgetConfigurator
    {
        public CustomWidgetConfigurator(final Version xml_version)
        {
            super(xml_version);
        }

        @Override
        public boolean configureFromXML(final ModelReader model_reader, final Widget widget,
                                        final Element xml) throws Exception
        {
            if (! super.configureFromXML(model_reader, widget, xml))
                return false;
            if (xml_version.getMajor() < 3)
            {
                TextEntryWidget text_widget = (TextEntryWidget)widget;
                TextUpdateWidget.readLegacyFormat(xml, text_widget.format, text_widget.precision, text_widget.propPVName());

                final Optional<String> text = XMLUtil.getChildString(xml, "multiline_input");
                if (text.isPresent()  &&  Boolean.parseBoolean(text.get()))
                    text_widget.propMultiLine().setValue(true);
            }
            return true;
        }
    }

    private volatile WidgetProperty<Boolean> enabled;
    private volatile WidgetProperty<WidgetColor> foreground;
    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<WidgetFont> font;
    private volatile WidgetProperty<FormatOption> format;
    private volatile WidgetProperty<Integer> precision;
    private volatile WidgetProperty<Boolean> show_units;
    private volatile WidgetProperty<Boolean> wrap_words;
    private volatile WidgetProperty<Boolean> multi_line;

    public TextEntryWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    public Version getVersion()
    {   // Legacy used 2.0.0 for text input
        return new Version(3, 0, 0);
    }

    @Override
    public WidgetConfigurator getConfigurator(Version persisted_version) throws Exception
    {
        return new CustomWidgetConfigurator(persisted_version);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(font = propFont.createProperty(this, NamedWidgetFonts.DEFAULT));
        properties.add(foreground = propForegroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.TEXT)));
        properties.add(background = propBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.WRITE_BACKGROUND)));
        properties.add(format = propFormat.createProperty(this, FormatOption.DEFAULT));
        properties.add(precision = propPrecision.createProperty(this, -1));
        properties.add(show_units = propShowUnits.createProperty(this, true));
        properties.add(enabled = runtimePropEnabled.createProperty(this, true));
        properties.add(wrap_words = propWrapWords.createProperty(this, false));
        properties.add(multi_line = propMultiLine.createProperty(this, false));
    }

    /** @return 'foreground_color' property */
    public WidgetProperty<WidgetColor> propForegroundColor()
    {
        return foreground;
    }

    /** @return 'background_color' property*/
    public WidgetProperty<WidgetColor> propBackgroundColor()
    {
        return background;
    }

    /** @return 'font' property */
    public WidgetProperty<WidgetFont> propFont()
    {
        return font;
    }

    /** @return 'format' property */
    public WidgetProperty<FormatOption> propFormat()
    {
        return format;
    }

    /** @return 'precision' property */
    public WidgetProperty<Integer> propPrecision()
    {
        return precision;
    }

    /** @return 'show_units' property */
    public WidgetProperty<Boolean> propShowUnits()
    {
        return show_units;
    }

    /** @return 'enabled' property */
    public WidgetProperty<Boolean> runtimePropEnabled()
    {
        return enabled;
    }

    /** @return 'wrap_words' property */
    public WidgetProperty<Boolean> propWrapWords()
    {
        return wrap_words;
    }

    /** @return 'multi_line' property */
    public WidgetProperty<Boolean> propMultiLine()
    {
        return multi_line;
    }
}
