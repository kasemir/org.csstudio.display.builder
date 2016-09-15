/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.ModelPlugin.logger;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propFont;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propForegroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propFormat;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propPrecision;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propShowUnits;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimePropValue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.csstudio.display.builder.model.MacroizedWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.FormatOption;
import org.csstudio.display.builder.model.properties.StringWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.diirt.vtype.VType;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that displays a changing text
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TextUpdateWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("textupdate", WidgetCategory.MONITOR,
            "TextUpdate",
            "platform:/plugin/org.csstudio.display.builder.model/icons/textupdate.png",
            "Displays current value of PV as text",
            Arrays.asList("org.csstudio.opibuilder.widgets.TextUpdate"))
    {
        @Override
        public Widget createWidget()
        {
            return new TextUpdateWidget();
        }
    };

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
            if (xml_version.getMajor() < 2)
            {
                TextUpdateWidget text_widget = (TextUpdateWidget)widget;
                TextUpdateWidget.readLegacyFormat(xml, text_widget.format, text_widget.precision, text_widget.pv_name);

                // Legacy text update had a "text" property that allowed using
                // it just like a label - no pv_name.
                // Some scripts would even update the 'text' concurrent with a pv_name...
                final Optional<String> text = XMLUtil.getChildString(xml, "text");
                if (text.isPresent()  &&  text.get().length() > 0  &&
                    ((MacroizedWidgetProperty<String>) text_widget.propPVName()).getSpecification().isEmpty())
                    logger.log(Level.WARNING, "Replace with Label: " + text_widget + " has 'text' but no 'pv_name'");
            }
            return true;
        }
    }

    /** Read legacy widget's format
     *  @param xml Widget XML
     *  @param format Format property to update
     *  @param precision Precision property to update
     *  @param pv_name PV name property to update
     */
    // package-level access for TextEntryWidget
    static void readLegacyFormat(final Element xml, final WidgetProperty<FormatOption> format,
                                 final WidgetProperty<Integer> precision,
                                 final WidgetProperty<String> pv_name)
    {
        Element element = XMLUtil.getChildElement(xml, "format_type");
        if (element != null)
        {
            final int legacy_format = Integer.parseInt(XMLUtil.getString(element));
            switch (legacy_format)
            {
            case 1: // DECIMAL
                format.setValue(FormatOption.DECIMAL);
                break;
            case 2: // EXP
                format.setValue(FormatOption.EXPONENTIAL);
                break;
            case 3: // HEX (32)
                format.setValue(FormatOption.HEX);
                precision.setValue(8);
                break;
            case 4: // STRING
                format.setValue(FormatOption.STRING);
                break;
            case 5: // HEX64
                format.setValue(FormatOption.HEX);
                precision.setValue(16);
                break;
            case 6: // COMPACT
                format.setValue(FormatOption.COMPACT);
                break;
            case 7: // ENG (since Aug. 2016)
                format.setValue(FormatOption.ENGINEERING);
                break;
            default:
                format.setValue(FormatOption.DEFAULT);
            }
        }

        // If legacy requested precision-from-PV, mark that in precision
        element = XMLUtil.getChildElement(xml, "precision_from_pv");
        if (element != null  &&  Boolean.parseBoolean(XMLUtil.getString(element)))
            precision.setValue(-1);

        // Remove legacy longString attribute from PV,
        // instead use STRING formatting
        String pv = ((StringWidgetProperty)pv_name).getSpecification();
        if (pv.endsWith(" {\"longString\":true}"))
        {
            pv = pv.substring(0, pv.length() - 20);
            ((StringWidgetProperty)pv_name).setSpecification(pv);
            format.setValue(FormatOption.STRING);
        }
    }

    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<WidgetColor> foreground;
    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<WidgetFont> font;
    private volatile WidgetProperty<FormatOption> format;
    private volatile WidgetProperty<Integer> precision;
    private volatile WidgetProperty<Boolean> show_units;
    private volatile WidgetProperty<VType> value;

    public TextUpdateWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
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
        properties.add(pv_name = propPVName.createProperty(this, ""));
        properties.add(propBorderAlarmSensitive.createProperty(this, true));
        properties.add(font = propFont.createProperty(this, NamedWidgetFonts.DEFAULT));
        properties.add(foreground = propForegroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.TEXT)));
        properties.add(background = propBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.READ_BACKGROUND)));
        properties.add(format = propFormat.createProperty(this, FormatOption.DEFAULT));
        properties.add(precision = propPrecision.createProperty(this, -1));
        properties.add(show_units = propShowUnits.createProperty(this, true));
        properties.add(value = runtimePropValue.createProperty(this, null));
    }

    /** @return 'pv_name' property */
    public WidgetProperty<String> propPVName()
    {
        return pv_name;
    }

    /** @return 'foreground_color' property */
    public WidgetProperty<WidgetColor> propForegroundColor()
    {
        return foreground;
    }

    /** @return 'background_color' property */
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

    /** @return Runtime 'value' property */
    public WidgetProperty<VType> runtimePropValue()
    {
        return value;
    }
}
