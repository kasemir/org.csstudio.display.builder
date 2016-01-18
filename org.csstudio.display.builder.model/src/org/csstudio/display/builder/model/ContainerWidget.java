/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeInsets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Base class for widget that contains other widgets.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ContainerWidget extends Widget
{
    /** Reserved ContainerWidget user data key for storing toolkit parent item */
    public static final String USER_DATA_TOOLKIT_PARENT = "_toolkit_parent";

    /** 'children' is a property to allow notifications,
     *  but setting its value or creating additional property instances
     *  is not supported.
     *
     *  <p>All access must be via the ContainerWidget.add/removeChild() methods.
     *
     *  <p>Notifications are sent with a list of elements added or removed,
     *  <u>not</u> the complete old resp. new value.
     */
    private static final WidgetPropertyDescriptor<List<Widget>> CHILDREN_PROPERTY_DESCRIPTOR =
            new WidgetPropertyDescriptor<List<Widget>>(
                    WidgetPropertyCategory.RUNTIME, "children", "Child widgets")
    {
        @Override
        public WidgetProperty<List<Widget>> createProperty(final Widget widget,
                final List<Widget> ignored)
        {
            throw new UnsupportedOperationException("Only created by ContainerWidget");
        }
    };

    private class ChildrenWidgetsProperty extends RuntimeWidgetProperty<List<Widget>>
    {
        public ChildrenWidgetsProperty(final Widget widget)
        {
            super(CHILDREN_PROPERTY_DESCRIPTOR, widget, new CopyOnWriteArrayList<>());
        }

        @Override
        public void setValueFromObject(final Object value) throws Exception
        {
            throw new UnsupportedOperationException("Use ContainerWidget#addChild()/removeChild()");
        }

        @Override
        public void setValue(final List<Widget> value)
        {
            throw new UnsupportedOperationException("Use ContainerWidget#addChild()/removeChild()");
        }
    }

    /** Child Widgets
     *
     *  <p>Uses CopyOnWriteArrayList list for thread safe
     *  get and iterate
     *
     *  SYNC on CopyOnWriteArrayList instance for atomic get-and-set
     */
    protected ChildrenWidgetsProperty children;

    private WidgetProperty<int[]> insets;

    /** Widget constructor.
     *  @param type Widget type
     *  @param default_width Default width
     *  @param default_height .. and height
     */
    public ContainerWidget(final String type, final int default_width, final int default_height)
    {
    	super(type, default_width, default_height);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(insets = runtimeInsets.createProperty(this, new int[] { 0, 0 }));
        properties.add(children = new ChildrenWidgetsProperty(this));
    }

    /** @return Child widgets in Widget tree */
    public List<Widget> getChildren()
    {
        return Collections.unmodifiableList(children.getValue());
    }

    /** Locate a child widget by name
     *
     *  <p>Recurses through all child widgets,
     *  including groups and sub-groups.
     *
     *  @param name Name of widget
     *  @return First widget with given name or <code>null</code>
     */
    public Widget getChildByName(final String name)
    {
        // Could back this with a Map<String, Widget>,
        // but note that there can be duplicates:
        // ContainerWidget.addChild(WidgetNamedFred);
        // ContainerWidget.addChild(AnotherWidgetNamedFred);
        // ContainerWidget.removeChild(AnotherWidgetNamedFred);
        // -> Must still find the first WidgetNamedFred,
        //    and thus need  Map<String, List<Widget>>
        // Update that map in addChild() and removeChild()
        for (final Widget child : children.getValue())
        {
            if (child.getName().equals(name))
                return child;
            if (child instanceof ContainerWidget)
            {
                final Widget maybe = ((ContainerWidget) child).getChildByName(name);
                if (maybe != null)
                    return maybe;
            }
        }
        return null;
    }

    /** @param child Widget to add as child in widget tree */
    public void addChild(final Widget child)
    {
        final List<Widget> list = children.getValue();
        synchronized (list)
        {   // Atomically check-then-add
            if (list.contains(child))
                throw new IllegalArgumentException(this +
                        " already has child widget " + child);
            list.add(child);
        }
        child.setParent(this);
        children.firePropertyChange(null, Arrays.asList(child));
    }

    /** @param child Widget to remove as child from widget tree */
    public void removeChild(final Widget child)
    {
        final List<Widget> list = children.getValue();
        if (! list.remove(child))
            throw new IllegalArgumentException("Widget hierarchy error: " + child + " is not known to " + this);
        child.setParent(null);
        children.firePropertyChange(Arrays.asList(child), null);
    }

    public WidgetProperty<int[]> runtimeInsets()
    {
        return insets;
    }

    /** @see #CHILDREN_PROPERTY_DESCRIPTOR */
    public WidgetProperty<List<Widget>> runtimeChildren()
    {
        return children;
    }
}
