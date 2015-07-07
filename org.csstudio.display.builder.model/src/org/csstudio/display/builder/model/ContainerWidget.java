/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeInsets;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Base class for widget that contains other widgets.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ContainerWidget extends BaseWidget
{
    // 'children' is a property to allow notifications,
    // but setting its value or creating additional property instances
    // is not supported.
    //
    // All access must be via the ContainerWidget.add/removeChild() methods.
    private static final WidgetPropertyDescriptor<List<Widget>> CHILD_PROPERTY_DESCRIPTOR =
            new WidgetPropertyDescriptor<List<Widget>>(
                    WidgetPropertyCategory.RUNTIME, "children", "Child widgets")
    {
        @Override
        public WidgetProperty<List<Widget>> createProperty(final Widget widget,
                final List<Widget> ignored)
        {
            throw new UnsupportedOperationException();
        }
    };

    private static class ChildrenWidgetsProperty extends RuntimeWidgetProperty<List<Widget>>
    {
        public ChildrenWidgetsProperty(final Widget widget)
        {
            super(CHILD_PROPERTY_DESCRIPTOR, widget, new CopyOnWriteArrayList<>());
        }

        @Override
        public void setValueFromObject(final Object value) throws Exception
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setValue(final List<Widget> value)
        {
            throw new UnsupportedOperationException();
        }
    }

    /** Child Widgets */
    protected final ChildrenWidgetsProperty children;

    private WidgetProperty<int[]> insets;

    /** Widget constructor.
     *  @param type Widget type
     */
    public ContainerWidget(final String type)
    {
    	super(type);
    	children = new ChildrenWidgetsProperty(this);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(insets = runtimeInsets.createProperty(this, new int[] { 0, 0 }));
    }

    /** @return Child widgets in Widget tree */
    public List<Widget> getChildren()
    {
        return Collections.unmodifiableList(children.getValue());
    }

    /** @param child Widget to add as child in widget tree */
    public void addChild(final Widget child)
    {
        final List<Widget> list = children.getValue();
        synchronized (list)
        {
            if (list.contains(child))
                throw new IllegalArgumentException(this +
                        " already has child widget " + child);
            list.add(child);
        }
        child.setParent(this);
        firePropertyChange(children, null, child);
    }

    /** @param child Widget to remove as child from widget tree */
    public void removeChild(final Widget child)
    {
        final List<Widget> list = children.getValue();
        list.remove(child);
        child.setParent(null);
        firePropertyChange(children, child, null);
    }

    public WidgetProperty<int[]> insets()
    {
        return insets;
    }
}
