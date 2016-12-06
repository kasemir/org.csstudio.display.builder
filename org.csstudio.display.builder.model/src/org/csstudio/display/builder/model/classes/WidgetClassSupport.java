/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.classes;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetFactory;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.ModelReader;

/** Widget 'class' support
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetClassSupport
{
    /** File extension used for widget class definition files */
    // 'bob template file'
    public static final String FILE_EXTENSION = "btf";

    /** Name of the default class
     *
     *  <p>Is always present, using the default value
     *  of each property.
     *  Can be overwritten in widget class file.
     */
    public static final String DEFAULT = "DEFAULT";

    /** Map of widget type to classes-for-type to properties */
    // TreeMap  -> sorted; may help when dumping, debugging
    private final Map<String,
                      Map<String,
                          Set<WidgetProperty<?>>>> widget_types = new TreeMap<>();

    /** Load widget classes
     *
     *  @param stream Stream for a widget class file
     *  @throws Exception on error
     */
    public WidgetClassSupport(final InputStream stream) throws Exception
    {
        final DisplayModel model = new ModelReader(stream).readModel();

        // Register all DEFAULT widgets
        for (WidgetDescriptor descr : WidgetFactory.getInstance().getWidgetDescriptions())
            registerClass(descr.createWidget());

        // Register widgets from class definition file
        for (Widget widget : model.getChildren())
            registerClass(widget);
    }

    /** @param widget Widget to register, using its class and properties */
    private void registerClass(final Widget widget)
    {
        final String type = widget.getType();
        final String widget_class = widget.getWidgetClass();
        final Map<String, Set<WidgetProperty<?>>> widget_classes = widget_types.computeIfAbsent(type, t -> new TreeMap<>());
        widget_classes.put(widget_class, widget.getProperties());
    }

    /** Get known widget classes
     *  @param widget_type Widget type
     *  @return Widget class names for that type
     */
    public Set<String> getWidgetClasses(final String widget_type)
    {
        return widget_types.get(widget_type).keySet();
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();

        for (String type : widget_types.keySet())
        {
            buf.append(type).append(":\n");

            final Map<String, Set<WidgetProperty<?>>> widget_classes = widget_types.get(type);
            for (String clazz : widget_classes.keySet())
                buf.append("   ").append(clazz).append("\n");
        }

        return buf.toString();
    }
}
