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
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propY;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimePropValue;

import java.util.List;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.diirt.vtype.VType;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Base for {@link LEDWidget} and {@link MultiStateLEDWidget}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class BaseLEDWidget extends VisibleWidget
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

    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<VType> value;

    /** Widget constructor.
     *  @param type Widget type
     */
    public BaseLEDWidget(final String type)
    {
        super(type);
    }

    @Override
    public Version getVersion()
    {   // Legacy used 1.0.0
        return new Version(2, 0, 0);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(pv_name = propPVName.createProperty(this, ""));
        properties.add(propBorderAlarmSensitive.createProperty(this, true));
        properties.add(value = runtimePropValue.createProperty(this, null));

        // Initial size
        propWidth().setValue(20);
        propHeight().setValue(20);
    }

    /** @return 'pv_name' property */
    public WidgetProperty<String> propPVName()
    {
        return pv_name;
    }

    /** @return Runtime 'value' property */
    public WidgetProperty<VType> runtimePropValue()
    {
        return value;
    }
}
