/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.bobwidget;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propFile;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propLineColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propLineWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propLineStyle;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties.WidgetLineStyle;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.widgets.VisibleWidget;

/** Model for persisting data browser widget configuration.
 *
 *  For the OPI, it holds the Data Browser config file name.
 *  For the Data Browser, it holds the {@link DataBrowserModel}.
 *
 *  @author Jaka Bobnar - Original selection value PV support
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DataBrowserWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
            new WidgetDescriptor("databrowser", WidgetCategory.PLOT,
                    "Data Browser",
                    "platform:/plugin/org.csstudio.trends.databrowser3.bobwidget/icons/databrowser.png",
                    "Embedded Data Brower",
                    Arrays.asList("org.csstudio.trends.databrowser.opiwidget"))
    {
        @Override
        public Widget createWidget()
        {
            return new DataBrowserWidget();
        }
    };

    public static final WidgetPropertyDescriptor<Boolean> propShowToolbar =
            CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_toolbar", Messages.PlotWidget_ShowToolbar);

    private volatile WidgetProperty<Boolean> show_toolbar;
    private volatile WidgetProperty<String> filename;

    //TODO: more properties: show/hide legend, show/hide title, title text... others?

    //TODO: configure these from border_color, border_width
    private volatile WidgetProperty<WidgetColor> line_color;
    private volatile WidgetProperty<Integer> line_width;
    private volatile WidgetProperty<WidgetLineStyle> line_style;

    public DataBrowserWidget()
    {
        super(WIDGET_DESCRIPTOR.getType(), 200, 200);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(filename = propFile.createProperty(this, ""));
        properties.add(show_toolbar = propShowToolbar.createProperty(this, true));
        properties.add(line_width = propLineWidth.createProperty(this, 2));
        properties.add(line_color = propLineColor.createProperty(this, new WidgetColor(0, 0, 255)));
        properties.add(line_style = propLineStyle.createProperty(this, WidgetLineStyle.SOLID));
    }

    /** @return 'text' property */
    public WidgetProperty<String> propFile()
    {
        return filename;
    }

    /** @return 'show_toolbar' property */
    public WidgetProperty<Boolean> propShowToolbar()
    {
        return show_toolbar;
    }

    /** @return 'show_toolbar' property */
    public WidgetProperty<WidgetColor> propLineColor()
    {
        return line_color;
    }

    /** @return 'show_toolbar' property */
    public WidgetProperty<Integer> propLineWidth()
    {
        return line_width;
    }

    /** @return 'show_toolbar' property */
    public WidgetProperty<WidgetLineStyle> propLineStyle()
    {
        return line_style;
    }

    @Override
    public String toString()
    {
        return "DataBrowserWidgetModel: " + filename;
    }
}
