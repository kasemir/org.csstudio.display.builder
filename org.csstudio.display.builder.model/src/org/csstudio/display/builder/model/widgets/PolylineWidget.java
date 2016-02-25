/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayLineColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayLineWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayPoints;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.Points;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Widget that displays a static line of points
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PolylineWidget extends VisibleWidget
{
    // TODO Add Polyline Arrows

    /** Legacy polyline used 1.0.0 */
    private static final Version version = new Version(2, 0, 0);

    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("polyline", WidgetCategory.GRAPHIC,
            "Polyline",
            "platform:/plugin/org.csstudio.display.builder.model/icons/polyline.png",
            "Line with two or more points",
            Arrays.asList("org.csstudio.opibuilder.widgets.polyline"))
    {
        @Override
        public Widget createWidget()
        {
            return new PolylineWidget();
        }
    };

    /** Handle legacy XML format */
    static class LegacyWidgetConfigurator extends WidgetConfigurator
    {
        public LegacyWidgetConfigurator(final Version xml_version)
        {
            super(xml_version);
        }

        @Override
        public boolean configureFromXML(final Widget widget, final Element widget_xml) throws Exception
        {
            PolygonWidget.adjustXMLPoints(widget_xml);
            // Legacy used background color for the line
            Element xml = XMLUtil.getChildElement(widget_xml, "background_color");
            if (xml != null)
            {
                final Document doc = widget_xml.getOwnerDocument();
                Element line = doc.createElement(displayLineColor.getName());
                final Element c = XMLUtil.getChildElement(xml, "color");
                line.appendChild(c.cloneNode(true));
                widget_xml.appendChild(line);
            }

            // Parse updated XML
            return super.configureFromXML(widget, widget_xml);
        }
    };

    private volatile WidgetProperty<WidgetColor> line_color;
    private volatile WidgetProperty<Integer> line_width;
    private volatile WidgetProperty<Points> points;

    public PolylineWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(line_color = displayLineColor.createProperty(this, new WidgetColor(0, 0, 255)));
        properties.add(line_width = displayLineWidth.createProperty(this, 3));
        properties.add(points = displayPoints.createProperty(this, new Points()));
    }

    @Override
    public Version getVersion()
    {
        return version;
    }

    @Override
    public WidgetConfigurator getConfigurator(final Version persisted_version)
            throws Exception
    {
        if (persisted_version.compareTo(version) < 0)
            return new LegacyWidgetConfigurator(persisted_version);
        else
            return super.getConfigurator(persisted_version);
    }

    /** @return Display 'line_color' */
    public WidgetProperty<WidgetColor> displayLineColor()
    {
        return line_color;
    }

    /** @return Display 'line_width' */
    public WidgetProperty<Integer> displayLineWidth()
    {
        return line_width;
    }

    /** @return Display 'points' */
    public WidgetProperty<Points> displayPoints()
    {
        return points;
    }
}
