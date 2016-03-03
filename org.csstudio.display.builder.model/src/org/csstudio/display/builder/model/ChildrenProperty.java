/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.stream.XMLStreamWriter;

import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.w3c.dom.Element;

/** Widget property that holds list of child widgets.
 *
 *  <p>A 'ContainerWidget' is a widget that has this 'children' property.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ChildrenProperty extends RuntimeWidgetProperty<List<Widget>>
{
    /** 'children' is a property to allow notifications,
     *  but setting its value or creating additional property instances
     *  is not supported.
     *
     *  <p>All access must be via the ContainerWidget.add/removeChild() methods.
     *
     *  <p>Notifications are sent with a list of elements added or removed,
     *  <u>not</u> the complete old resp. new value.
     */
    public static final WidgetPropertyDescriptor<List<Widget>> DESCRIPTOR =
            new WidgetPropertyDescriptor<List<Widget>>(
                    WidgetPropertyCategory.RUNTIME, "children", "Child widgets")
    {
        @Override
        public WidgetProperty<List<Widget>> createProperty(final Widget widget,
                final List<Widget> ignored)
        {
            throw new UnsupportedOperationException("Only created by ChildrenProperty constructor");
        }
    };

    /** Check if widget is a 'container' by fetching its children
     *  @param widget Widget
     *  @return {@link ChildrenProperty} or <code>null</code> if widget is not a container
     */
    public static ChildrenProperty getChildren(final Widget widget)
    {
        final Optional<WidgetProperty<List<Widget>>> children = widget.checkProperty(DESCRIPTOR);
        if (children.isPresent())
            return (ChildrenProperty) children.get();
        return null;
    }

    public ChildrenProperty(final Widget widget)
    {
        super(DESCRIPTOR, widget, Collections.emptyList());
        value = new CopyOnWriteArrayList<>();
    }

    @Override
    public List<Widget> getValue()
    {   // Override normal access to value, only provide read-only version of list
        return Collections.unmodifiableList(super.getValue());
    }

    @Override
    public void setValueFromObject(final Object value) throws Exception
    {
        throw new UnsupportedOperationException("Use ChildrenProperty#addChild()/removeChild()");
    }

    @Override
    public void setValue(final List<Widget> value)
    {
        throw new UnsupportedOperationException("Use ChildrenProperty#addChild()/removeChild()");
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
        // addChild(WidgetNamedFred);
        // addChild(AnotherWidgetNamedFred);
        // removeChild(AnotherWidgetNamedFred);
        // -> Must still find the first WidgetNamedFred,
        //    and thus need  Map<String, List<Widget>>
        // Update that map in addChild() and removeChild()
        for (final Widget child : value)
        {
            if (child.getName().equals(name))
                return child;
            final ChildrenProperty grandkids = getChildren(child);
            if (grandkids != null)
            {
                final Widget maybe = grandkids.getChildByName(name);
                if (maybe != null)
                    return maybe;
            }
        }
        return null;
    }

    /** @param index Index where to add child, or -1 to append at end
     *  @param child Widget to add as child
     */
    public void addChild(final int index, final Widget child)
    {
        final List<Widget> list = value;
        synchronized (list)
        {   // Atomically check-then-add
            if (list.contains(child))
                throw new IllegalArgumentException(this +
                        " already has child widget " + child);
            if (index < 0)
                list.add(child);
            else
                list.add(index, child);
        }
        child.setParent(getWidget());
        firePropertyChange(null, Arrays.asList(child));
    }

    /** @param child Widget to add as child */
    public void addChild(final Widget child)
    {
        addChild(-1, child);
    }

    /** @param child Widget to remove as child
     *  @return Index of removed child in list of children
     */
    public int removeChild(final Widget child)
    {
        final List<Widget> list = value;
        final int index;
        synchronized (list)
        {
            index = list.indexOf(child);
            if (index < 0)
                throw new IllegalArgumentException("Widget hierarchy error: " + child + " is not known to " + this);
            list.remove(index);
        }
        child.setParent(null);
        firePropertyChange(Arrays.asList(child), null);
        return index;
    }

    @Override
    public void writeToXML(final ModelWriter model_writer, final XMLStreamWriter writer) throws Exception
    {
        model_writer.writeWidgets(getValue());
    }

    @Override
    public void readFromXML(final ModelReader model_reader, final Element property_xml) throws Exception
    {
        model_reader.readWidgets(this, property_xml);
    }
}
