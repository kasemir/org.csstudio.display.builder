/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
import static org.csstudio.display.builder.model.widgets.plots.PlotWidgetProperties.displayToolbar;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.RuntimeWidgetProperty;
import org.csstudio.display.builder.model.StructuredWidgetProperty;
import org.csstudio.display.builder.model.StructuredWidgetProperty.Descriptor;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.diirt.vtype.VType;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that displays a string table
 *
 *  <p>The 'value' can be either a VTable
 *  or a <code>List&lt;List&lt;String>></code>.
 *  The latter includes 2-D string arrays written
 *  by Jython scripts.
 *
 *  TODO setData(VTable)
 *  TODO setData(List<List<String>>)
 *  TODO Some API for script to setCellText(row, column)
 *  TODO Some API for script to setCellBackground(row, column)
 *  TODO Some API for script to setCellColor(row, column)
 *  TODO Track selected row, col via listener or PV
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TableWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("table", WidgetCategory.MONITOR,
            "Table",
            "platform:/plugin/org.csstudio.display.builder.model/icons/table.gif",
            "A table",
            Arrays.asList("org.csstudio.opibuilder.widgets.table"))
    {
        @Override
        public Widget createWidget()
        {
            return new TableWidget();
        }
    };

    private final static WidgetPropertyDescriptor<Boolean> displayEdit =
        CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "editable", "Editable");

    private final static StructuredWidgetProperty.Descriptor displayColumn =
        new Descriptor(WidgetPropertyCategory.DISPLAY, "column", "Column");

    /** Structure for column configuration */
    public static class ColumnProperty extends StructuredWidgetProperty
    {
        public ColumnProperty(final Widget widget, final String name)
        {
            super(displayColumn, widget,
                  Arrays.asList(CommonWidgetProperties.widgetName.createProperty(widget, name),
                                CommonWidgetProperties.positionWidth.createProperty(widget, 0),
                                displayEdit.createProperty(widget, true)));
        }

        public WidgetProperty<String> name()        { return getElement(0); }
        public WidgetProperty<Integer> width()      { return getElement(1); }
        public WidgetProperty<Boolean> editable()   { return getElement(2); }
    };

    /** 'columns' array */
    public static final ArrayWidgetProperty.Descriptor<ColumnProperty> displayColumns =
        new ArrayWidgetProperty.Descriptor<>(WidgetPropertyCategory.DISPLAY, "columns", "Columns",
                                             (widget, index) ->
                                             new ColumnProperty(widget, "Column " + (index + 1)));


    private static final WidgetPropertyDescriptor<Object> runtimeValue =
        new WidgetPropertyDescriptor<Object>(WidgetPropertyCategory.RUNTIME, "value", Messages.WidgetProperties_Value)
        {
            @Override
            public WidgetProperty<Object> createProperty(final Widget widget, final Object value)
            {
                return new RuntimeWidgetProperty<Object>(this, widget, value)
                {
                    @Override
                    public void setValueFromObject(final Object value) throws Exception
                    {
                        if (value instanceof VType)
                            setValue(value);
                        else if (value instanceof List)
                            setValue(value);
                        else if (value == null)
                            setValue(null);
                        else
                            throw new Exception("Need VType or List<List<String>, got " + value);
                    }
                };
            }
        };


    private static class CustomConfigurator extends WidgetConfigurator
    {
        public CustomConfigurator(final Version xml_version)
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
                configureLegacyColumns((TableWidget) widget, xml);

            return true;
        }
    };

    /** Configure column information from legacy XML
     *  @param widget
     *  @param xml
     *  @throws Exception
     */
    private static void configureLegacyColumns(final TableWidget widget, final Element xml) throws Exception
    {
        // Legacy column information:
        // <column_headers>
        //   <row>  <col>Name</col>  <col>width</col>  <col>Edit: No?</col>  <col>TEXT|CHECKBOX|DROPDOWN</col>
        final Element el = XMLUtil.getChildElement(xml, "column_headers");
        if (el == null)
            return;
        int col = -1;
        final ArrayWidgetProperty<ColumnProperty> columns = widget.displayColumns();
        for (Element row : XMLUtil.getChildElements(el, "row"))
        {
            ++col;
            while (columns.size() <= col)
                columns.addElement();
            final ColumnProperty column = columns.getElement(col);

            int idx = -1;
            for (Element item : XMLUtil.getChildElements(row, "col"))
            {
                switch (++idx)
                {
                case 0: column.name().setValue(XMLUtil.getString(item));
                        break;
                case 1: final String text = XMLUtil.getString(item);
                        if (text.length() > 0)
                            try
                            {
                                column.width().setValue(Integer.parseInt(text));
                            }
                            catch (NumberFormatException ex)
                            {
                                throw new Exception("Error in legacy table column width", ex);
                            }
                        break;
                case 2: if ("No".equalsIgnoreCase(XMLUtil.getString(item)))
                            column.editable().setValue(false);
                        break;
                case 3: // Ignore editor type
                        break;
                }
            }
        }
    }

    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<Boolean> show_toolbar;
    private volatile ArrayWidgetProperty<ColumnProperty> columns;
    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<Object> value;

    public TableWidget()
    {
        super(WIDGET_DESCRIPTOR.getType(), 500, 300);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(displayBorderAlarmSensitive.createProperty(this, true));
        properties.add(background = displayBackgroundColor.createProperty(this, new WidgetColor(30, 144, 255)));
        properties.add(show_toolbar = displayToolbar.createProperty(this,false));
        properties.add(columns = displayColumns.createProperty(this, Arrays.asList(  new ColumnProperty(this, "Column 1") )));
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
        properties.add(value = runtimeValue.createProperty(this, null));
    }

    @Override
    public WidgetConfigurator getConfigurator(final Version persisted_version) throws Exception
    {
        return new CustomConfigurator(persisted_version);
    }

    /** @return Display 'background_color' */
    public WidgetProperty<WidgetColor> displayBackgroundColor()
    {
        return background;
    }

    /** @return Display 'show_toolbar' */
    public WidgetProperty<Boolean> displayToolbar()
    {
        return show_toolbar;
    }

    /** @return Display 'columns' */
    public ArrayWidgetProperty<ColumnProperty> displayColumns()
    {
        return columns;
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return pv_name;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<Object> runtimeValue()
    {
        return value;
    }
}
