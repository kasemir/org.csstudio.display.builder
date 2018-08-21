/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBit;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propConfirmDialogOptions;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propConfirmMessage;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propEnabled;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propFont;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propForegroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propLabelsFromPV;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propOffColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propOffLabel;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propOnColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propOnLabel;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propPassword;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.persist.WidgetFontService;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.ConfirmDialog;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/**
 * Widget that provides button for making a binary change.
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 21 Aug 2018
 */
public class SlideButtonWidget extends WritablePVWidget {

    /**
     * Widget descriptor
     */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("slide_button", WidgetCategory.CONTROL,
            "Slide Button",
            "platform:/plugin/org.csstudio.display.builder.model/icons/slide_button.png",
            "Slide button that can toggle one bit of a PV value between 1 and 0",
            Arrays.asList( "org.csstudio.opibuilder.widgets.BoolSwitch"))
    {

        @Override
        public Widget createWidget ( ) {
            return new SlideButtonWidget();
        }
    };

    /**
     * Handle legacy widget configuration.
     */
    private static class CustomConfigurator extends WidgetConfigurator {

        public CustomConfigurator ( Version xml_version ) {
            super(xml_version);
        }

        @Override
        public boolean configureFromXML ( final ModelReader model_reader, final Widget widget, final Element xml )
            throws Exception
        {

            if ( !super.configureFromXML(model_reader, widget, xml) )
                return false;

            final SlideButtonWidget button = (SlideButtonWidget) widget;

            // If legacy widgets was configured to not use labels, clear them
            XMLUtil.getChildBoolean(xml, "show_boolean_label").ifPresent(show -> {
                if ( !show ) {
                    button.propOffLabel().setValue("");
                    button.propOnLabel().setValue("");
                }
            });

            return true;

        }

    };

    private volatile WidgetProperty<WidgetColor>   background;
    private volatile WidgetProperty<Integer>       bit;
    private volatile WidgetProperty<ConfirmDialog> confirm_dialog;
    private volatile WidgetProperty<String>        confirm_message;
    private volatile WidgetProperty<Boolean>       enabled;
    private volatile WidgetProperty<WidgetFont>    font;
    private volatile WidgetProperty<WidgetColor>   foreground;
    private volatile WidgetProperty<Boolean>       labels_from_pv;
    private volatile WidgetProperty<WidgetColor>   off_color;
    private volatile WidgetProperty<String>        off_label;
    private volatile WidgetProperty<WidgetColor>   on_color;
    private volatile WidgetProperty<String>        on_label;
    private volatile WidgetProperty<String>        password;

    public SlideButtonWidget () {
        super(WIDGET_DESCRIPTOR.getType(), 100, 30);
    }

    @Override
    public WidgetConfigurator getConfigurator ( Version persisted_version )
        throws Exception
    {
        return new CustomConfigurator(persisted_version);
    }

    /**
     * @return 'background_color' property.
     */
    public WidgetProperty<WidgetColor> propBackgroundColor ( ) {
        return background;
    }

    /**
     * @return 'bit' property.
     */
    public WidgetProperty<Integer> propBit ( ) {
        return bit;
    }

    /**
     * @return 'confirm_dialog' property.
     */
    public WidgetProperty<ConfirmDialog> propConfirmDialog ( ) {
        return confirm_dialog;
    }

    /**
     * @return 'confirm_message' property.
     */
    public WidgetProperty<String> propConfirmMessage ( ) {
        return confirm_message;
    }

    /**
     * @return 'enabled' property.
     */
    public WidgetProperty<Boolean> propEnabled ( ) {
        return enabled;
    }

    /**
     * @return 'font' property.
     */
    public WidgetProperty<WidgetFont> propFont ( ) {
        return font;
    }

    /**
     * @return 'foreground_color' property.
     */
    public WidgetProperty<WidgetColor> propForegroundColor ( ) {
        return foreground;
    }

    /**
     * @return 'labels_from_pv' property.
     */
    public WidgetProperty<Boolean> propLabelsFromPV ( ) {
        return labels_from_pv;
    }

    /**
     * @return 'off_color' property.
     */
    public WidgetProperty<WidgetColor> propOffColor ( ) {
        return off_color;
    }

    /**
     * @return 'off_label' property.
     */
    public WidgetProperty<String> propOffLabel ( ) {
        return off_label;
    }

    /**
     * @return 'on_color' property.
     */
    public WidgetProperty<WidgetColor> propOnColor ( ) {
        return on_color;
    }

    /**
     * @return 'on_label' property.
     */
    public WidgetProperty<String> propOnLabel ( ) {
        return on_label;
    }

    /**
     * @return 'password' property.
     */
    public WidgetProperty<String> propPassword ( ) {
        return password;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        properties.add(bit = propBit.createProperty(this, 0));
        properties.add(off_label = propOffLabel.createProperty(this, "Off"));
        properties.add(off_color = propOffColor.createProperty(this, new WidgetColor(60, 100, 60)));
        properties.add(on_label = propOnLabel.createProperty(this, "On"));
        properties.add(on_color = propOnColor.createProperty(this, new WidgetColor(60, 255, 60)));
        properties.add(font = propFont.createProperty(this, WidgetFontService.get(NamedWidgetFonts.DEFAULT)));
        properties.add(foreground = propForegroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.TEXT)));
        properties.add(background = propBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.BUTTON_BACKGROUND)));
        properties.add(labels_from_pv = propLabelsFromPV.createProperty(this, false));
        properties.add(enabled = propEnabled.createProperty(this, true));
        properties.add(confirm_dialog = propConfirmDialogOptions.createProperty(this, ConfirmDialog.NONE));
        properties.add(confirm_message = propConfirmMessage.createProperty(this, "Are your sure you want to do this?"));
        properties.add(password = propPassword.createProperty(this, ""));

    }

}
