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

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.RuntimeWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.WidgetColor;

/** Widget that displays a string table
 *
 *  <p>The 'value' can be either a VTable
 *  or a <code>List&lt;List&lt;String>></code>
 *
 *  TODO setData(VTable)
 *  TODO setData(String[][])
 *  TODO setData(Collection<Collection<String>>)
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
                        System.out.println("Table received value " + value);
//                        if (value instanceof VType)
//                            setValue(value);
//                        else if (value instanceof List)
//                            setValue(value);
//                        else
//                            throw new Exception("Need VType or List<List<String>, got " + value);
                    }
                };
            }
        };

    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<Boolean> show_toolbar;
    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<Object> value;
    // TODO Headers

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
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
        properties.add(value = runtimeValue.createProperty(this, null));
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

}
