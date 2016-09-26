/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propHeight;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propY;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Base for {@link LEDWidget} and {@link MultiStateLEDWidget}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class BaseLEDWidget extends PVWidget
{
    /** Helper for configurator to handle legacy LED sizing */
    protected static void handle_legacy_position(final Widget widget, final Version xml_version, final Element xml)
                throws Exception
    {
        if (xml_version.getMajor() < 2)
        {   // Border was included in the size,
            // so with the same nominal size an "alarm sensitive" LED
            // was smaller than a non-a.s. LED */
            if (widget.getProperty(propBorderAlarmSensitive).getValue())
            {
                final int border = Integer.parseInt(XMLUtil.getChildString(xml, "border_width").orElse("1"));
                // In principle, border goes around the widget,
                // so X, Y get adjusted by 1*border
                // and Width, Height by 2*border.
                // But when comparing older files, border was added to X, Y twice
                // as well as size?!
                WidgetProperty<Integer> prop = widget.getProperty(propX);
                prop.setValue(prop.getValue() + 2*border);
                prop = widget.getProperty(propY);
                prop.setValue(prop.getValue() + 2*border);
                prop = widget.getProperty(propWidth);
                prop.setValue(prop.getValue() - 2*border);
                prop = widget.getProperty(propHeight);
                prop.setValue(prop.getValue() - 2*border);
            }
        }
    }

    protected volatile WidgetProperty<WidgetFont> font;
    protected volatile WidgetProperty<WidgetColor> foreground;

    /** Widget constructor.
     *  @param type Widget type
     */
    public BaseLEDWidget(final String type)
    {
        super(type, 20, 20);
    }

    @Override
    public Version getVersion()
    {   // Legacy used 1.0.0
        return new Version(2, 0, 0);
    }

    // Note: _NOT_ defining the  common font, foreground properties
    //       so that derived widgets can control their order within
    //       lists of properties.
    // protected void defineProperties(final List<WidgetProperty<?>> properties)

    /** @return 'font' property */
    public WidgetProperty<WidgetFont> propFont()
    {
        return font;
    }

    /** @return 'foreground' property*/
    public WidgetProperty<WidgetColor> propForegroundColor()
    {
        return foreground;
    }
}
