/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionHeight;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionY;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

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
public class BaseLEDWidget extends Widget
{
    /** Helper for configurator to handle legacy LED sizing */
    protected static void handle_legacy_position(final Widget widget, final Version xml_version, final Element xml)
                throws Exception
    {
        if (xml_version.getMajor() < 2)
        {   // Border was included in the size,
            // so with the same nominal size an "alarm sensitive" LED
            // was smaller than a non-a.s. LED */
            if (widget.getProperty(displayBorderAlarmSensitive).getValue())
            {
                final int border = Integer.parseInt(XMLUtil.getChildString(xml, "border_width").orElse("1"));
                // In principle, border goes around the widget,
                // so X, Y get adjusted by 1*border
                // and Width, Height by 2*border.
                // But when comparing older files, border was added to X, Y twice
                // as well as size?!
                WidgetProperty<Integer> prop = widget.getProperty(positionX);
                prop.setValue(prop.getValue() + 2*border);
                prop = widget.getProperty(positionY);
                prop.setValue(prop.getValue() + 2*border);
                prop = widget.getProperty(positionWidth);
                prop.setValue(prop.getValue() - 2*border);
                prop = widget.getProperty(positionHeight);
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
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
        properties.add(displayBorderAlarmSensitive.createProperty(this, true));
        properties.add(value = runtimeValue.createProperty(this, null));

        // Initial size
        positionWidth().setValue(20);
        positionHeight().setValue(20);
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return pv_name;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
    }
}
