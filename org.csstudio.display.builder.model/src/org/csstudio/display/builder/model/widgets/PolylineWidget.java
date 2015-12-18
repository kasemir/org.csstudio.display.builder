/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayLineColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayLineWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newIntegerPropertyDescriptor;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.BaseWidget;
import org.csstudio.display.builder.model.StructuredWidgetProperty;
import org.csstudio.display.builder.model.StructuredWidgetProperty.Descriptor;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Widget that displays a static line of points
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PolylineWidget extends BaseWidget
{
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
        public void configureFromXML(final Widget widget, final Element widget_xml) throws Exception
        {
            final Document doc = widget_xml.getOwnerDocument();

            // Legacy coordinates were absolute within display and used <point x="48" y="102" />
            // New coords. are relative to widget in format <point><x>48</x><y>102</y></point>
            final int x0 = Integer.parseInt(XMLUtil.getChildString(widget_xml, "x").orElse("0"));
            final int y0 = Integer.parseInt(XMLUtil.getChildString(widget_xml, "y").orElse("0"));
            Element xml = XMLUtil.getChildElement(widget_xml, "points");
            if (xml != null)
            {
                for (Element p_xml : XMLUtil.getChildElements(xml, "point"))
                {   // Fetch legacy x, y attributes
                    final int x = Integer.parseInt(p_xml.getAttribute("x"));
                    final int y = Integer.parseInt(p_xml.getAttribute("y"));

                    // Add as child elements with value relative to widget location
                    Element val_node = doc.createElement("x");
                    val_node.appendChild(doc.createTextNode(Integer.toString(x - x0)));
                    p_xml.appendChild(val_node);

                    val_node = doc.createElement("y");
                    val_node.appendChild(doc.createTextNode(Integer.toString(y - y0)));
                    p_xml.appendChild(val_node);
                }
            }

            // Legacy used background color for the line
            xml = XMLUtil.getChildElement(widget_xml, "background_color");
            if (xml != null)
            {
                Element line = doc.createElement(displayLineColor.getName());
                final Element c = XMLUtil.getChildElement(xml, "color");
                line.appendChild(c.cloneNode(true));
                widget_xml.appendChild(line);
            }

            // Parse updated XML
            super.configureFromXML(widget, widget_xml);
        }
    };

    /** X and Y coordinate */
    private static final WidgetPropertyDescriptor<Integer>
        pointX = newIntegerPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "x", "X Coordinate"),
        pointY = newIntegerPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "y", "Y Coordinate");

    /** "Point" structure with X and Y */
    public static class PointWidgetProperty extends StructuredWidgetProperty
    {
        public PointWidgetProperty(final StructuredWidgetProperty.Descriptor point_descriptor,
                                   final Widget widget)
        {
            super(point_descriptor, widget,
                  Arrays.asList(pointX.createProperty(widget, 0),
                                pointY.createProperty(widget, 0)));
        }
        public WidgetProperty<Integer> x()  { return getElement(0); }
        public WidgetProperty<Integer> y()  { return getElement(1); }
    };

    private final static StructuredWidgetProperty.Descriptor behaviorPoint =
        new Descriptor(WidgetPropertyCategory.DISPLAY, "point", "Point");

    /** Array of 'points' */
    private static final ArrayWidgetProperty.Descriptor<PointWidgetProperty> displayPoints =
        new ArrayWidgetProperty.Descriptor<>(WidgetPropertyCategory.DISPLAY, "points", "Points",
                                             (widget, index) -> new PointWidgetProperty(behaviorPoint, widget));


    private WidgetProperty<WidgetColor> line_color;
    private WidgetProperty<Integer> line_width;
    private ArrayWidgetProperty<PointWidgetProperty> points;

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
        properties.add(points = displayPoints.createProperty(this, Arrays.asList(new PointWidgetProperty(behaviorPoint, this))));
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

    public ArrayWidgetProperty<PointWidgetProperty> displayPoints()
    {
        return points;
    }
}
