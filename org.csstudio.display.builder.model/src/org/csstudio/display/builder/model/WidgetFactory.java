/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.csstudio.display.builder.model.widgets.ActionButtonWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.model.widgets.LEDWidget;
import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.csstudio.display.builder.model.widgets.ProgressBarWidget;
import org.csstudio.display.builder.model.widgets.RectangleWidget;
import org.csstudio.display.builder.model.widgets.TextUpdateWidget;

/** Factory that creates widgets based on type
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetFactory
{
    /** Singleton instance */
    private static final WidgetFactory instance = new WidgetFactory();

    /** List of widget types.
     *
     *  <p>Sorted by widget category, then type
     */
    private final SortedSet<WidgetDescriptor> descriptors =
        new TreeSet<>(
            Comparator.comparing(WidgetDescriptor::getCategory)
                      .thenComparing(WidgetDescriptor::getType));

    /** Map of type IDs (current and alternate) to {@link WidgetDescriptor} */
    private final Map<String, WidgetDescriptor> descriptor_by_type = new ConcurrentHashMap<>();

    private WidgetFactory()
    {
        // Prevent instantiation

        // TODO Load available widgets from registry
        addWidgetType(ActionButtonWidget.WIDGET_DESCRIPTOR);
        addWidgetType(EmbeddedDisplayWidget.WIDGET_DESCRIPTOR);
        addWidgetType(GroupWidget.WIDGET_DESCRIPTOR);
        addWidgetType(LabelWidget.WIDGET_DESCRIPTOR);
        addWidgetType(LEDWidget.WIDGET_DESCRIPTOR);
        addWidgetType(ProgressBarWidget.WIDGET_DESCRIPTOR);
        addWidgetType(RectangleWidget.WIDGET_DESCRIPTOR);
        addWidgetType(TextUpdateWidget.WIDGET_DESCRIPTOR);
    }

    /** @return Singleton instance */
    public static WidgetFactory getInstance()
    {
        return instance;
    }

    /** Inform factory about a widget type.
     *
     *  <p>This is meant to be called during plugin initialization,
     *  based on information from the registry.
     *
     *  @param descriptor {@link WidgetDescriptor}
     */
    public void addWidgetType(final WidgetDescriptor descriptor)
    {
        // descriptor_by_type contains type and all aliases,
        // and new type must be unique
        if (descriptor_by_type.containsKey(descriptor.getType()))
                throw new Error(descriptor + " already defined");

        // descriptors sorts by category and type
        descriptors.add(descriptor);

        descriptor_by_type.put(descriptor.getType(), descriptor);
        for (final String alternate : descriptor.getAlternateTypes())
        {
            if (descriptor_by_type.containsKey(alternate))
                throw new Error(alternate + " already defined by " +
                                descriptor_by_type.get(alternate));
            descriptor_by_type.put(alternate, descriptor);
        }
    }

    /** @return Descriptions of all currently known widget types */
    public Set<WidgetDescriptor> getWidgetDescriptions()
    {
        return Collections.unmodifiableSortedSet(descriptors);
    }

    /** Check if type is defined.
     *  @param type Widget type ID
     *  @return WidgetDescriptor
     */
    public Optional<WidgetDescriptor> getWidgetDescriptior(final String type)
    {
        return Optional.ofNullable(descriptor_by_type.get(type));
    }

    /** Create a widget
     *  @param type Type ID of the widget to create
     *  @return {@link Widget}
     *  @throws Exception on error
     */
    public Widget createWidget(final String type) throws Exception
    {
        final WidgetDescriptor descriptor = descriptor_by_type.get(type);
        if (descriptor == null)
            throw new Exception("Unknown widget type '" + type + "'");
        return descriptor.createWidget();
    }
}
