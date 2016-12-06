/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.csstudio.display.builder.model.ModelPlugin.logger;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import org.csstudio.display.builder.model.persist.ModelReader;

/** Widget 'class' support
 *
 *  <p>Maintains 'classes' for each widget type,
 *  for example a 'TITLE' class for 'label' widgets.
 *  A class consists of properties and values
 *  which can then be applied to a widget.
 *
 *  <p>Each property of a widget can be configured
 *  to _not_ use the class settings.
 *
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

    /** Map:
     *  widget type to classes-for-type,
     *  class name to properties,
     *  property name to property
     */
    // TreeMap  -> sorted; may help when dumping, debugging
    private final Map<String,
                      Map<String,
                          Map<String, WidgetProperty<?>>>> widget_types = new TreeMap<>();

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

        // Note:
        // Properties held by this class still point to their widgets,
        // which point to the display model,
        // so it's kept in memory.
        // Alternative: Just keep the property's values resp. specifications
    }

    /** @param widget Widget to register, using its class and properties */
    private void registerClass(final Widget widget)
    {
        final String type = widget.getType();
        final String widget_class = widget.getWidgetClass();
        final Map<String, Map<String, WidgetProperty<?>>> widget_classes = widget_types.computeIfAbsent(type, t -> new TreeMap<>());
        final Map<String, WidgetProperty<?>> class_properties = widget_classes.computeIfAbsent(widget_class, c -> new TreeMap<>());
        for (WidgetProperty<?> property : widget.getProperties())
            class_properties.put(property.getName(), property);
    }

    /** Get known widget classes
     *  @param widget_type Widget type
     *  @return Widget class names for that type
     */
    public Set<String> getWidgetClasses(final String widget_type)
    {
        return widget_types.get(widget_type).keySet();
    }

    /** Get class-based properties
     *
     *  @param widget Widget for which to get the class info
     *  @return Properties and values for that widget type and class, or <code>null</code>
     */
    private Map<String, WidgetProperty<?>> getClassSettings(final Widget widget)
    {
        final Map<String, Map<String, WidgetProperty<?>>> widget_classes = widget_types.get(widget.getType());
        if (widget_classes == null)
        {
            logger.log(Level.WARNING, "No class support for unknown widget type " + widget);
            return null;
        }

        final Map<String, WidgetProperty<?>> result = widget_classes.get(widget.getWidgetClass());
        if (result == null)
            logger.log(Level.WARNING, "Undefined widget type " + widget.getType() +
                                      " and class " + widget.getWidgetClass());
        return result;
    }

    /** Apply class-based property values to widget
     *
     *  @param widget Widget to update based on its current 'class'
     *  @throws Exception if property cannot be updated because of error in class-based value
     */
    public void apply(final Widget widget) throws Exception
    {
        final Map<String, WidgetProperty<?>> class_settings = getClassSettings(widget);
        if (class_settings == null)
            return;
        for (WidgetProperty<?> prop : widget.getProperties())
        {
            if (prop instanceof RuntimeWidgetProperty  ||  !prop.isUsingWidgetClass())
                continue;

            final WidgetProperty<?> class_setting = class_settings.get(prop.getName());
            if (class_setting == null)
                continue;

            if (prop instanceof MacroizedWidgetProperty)
                ((MacroizedWidgetProperty<?>)prop).setSpecification(((MacroizedWidgetProperty<?>)class_setting).getSpecification());
            else // This could throw an exception
                prop.setValueFromObject(class_setting.getValue());
        }
    }

    /** @return Debug representation */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();

        for (String type : widget_types.keySet())
        {
            buf.append(type).append(":\n");

            final Map<String, Map<String, WidgetProperty<?>>> widget_classes = widget_types.get(type);
            for (String clazz : widget_classes.keySet())
                buf.append("   ").append(clazz).append("\n");
        }

        return buf.toString();
    }
}
