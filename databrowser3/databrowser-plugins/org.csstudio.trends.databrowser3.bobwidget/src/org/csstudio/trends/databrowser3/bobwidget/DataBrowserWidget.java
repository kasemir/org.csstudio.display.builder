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

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties.WidgetLineStyle;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.widgets.VisibleWidget;
import org.csstudio.trends.databrowser3.model.Model;
import org.csstudio.trends.databrowser3.persistence.XMLPersistence;

/** Model for persisting data browser widget configuration.
 *
 *  For the OPI, it holds the Data Browser config file name.
 *  For the Data Browser, it holds the {@link DataBrowserModel}.
 *
 *  @author Jaka Bobnar - Original selection value PV support
 *  @author Kay Kasemir
 *  @author Megan Grodowitz - Databrowser 3 ported from 2
 */
@SuppressWarnings("nls")
public class DataBrowserWidget extends VisibleWidget
{
    /** Model with data to display */
    private volatile Model model = new Model();

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


    public Model getModel() {
        return model;
    }

    public Model cloneModel() {
        final Model model = new Model();
        model.setMacros(this.getMacrosOrProperties());
        try
        {
            final InputStream input = this.getFileInputStream();
            new XMLPersistence().load(model, input);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return model;
    }

    public InputStream getFileInputStream(final String base_path) {
        InputStream stream;
        String file_path;

        try
        {
            file_path = this.getExpandedFilename(base_path);
        }
        catch (Exception e)
        {
            //TODO: change to logging message
            System.out.println("Failure resolving image path from base path: " + base_path);
            e.printStackTrace();
            return null;
        }

        try
        {
            stream = ModelResourceUtil.openResourceStream(file_path);
        }
        catch (Exception e)
        {
            //System.out.println("Failure loading plot file:" + file_path);
            e.printStackTrace();
            return null;
        }

        return stream;
    }

    public InputStream getFileInputStream() {
        return this.getFileInputStream(this.filename.getValue());
    }

    public String getExpandedFilename(String base_path) throws Exception
    {
        // expand macros in the file name
        final String expanded_path = MacroHandler.replace(this.getMacrosOrProperties(), base_path);
        // Resolve new image file relative to the source widget model (not 'top'!)
        // Get the display model from the widget tied to this representation
        final DisplayModel widget_model = this.getDisplayModel();
        // Resolve the path using the parent model file path
        return ModelResourceUtil.resolveResource(widget_model, expanded_path);
    }

    public String getExpandedFilename() throws Exception
    {
        return getExpandedFilename(this.filename.getValue());
    }

    @Override
    public String toString()
    {
        return "DataBrowserWidgetModel: " + filename;
    }
}
