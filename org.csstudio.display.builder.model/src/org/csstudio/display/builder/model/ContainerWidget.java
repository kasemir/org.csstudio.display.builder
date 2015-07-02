/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

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
    /** Child Widgets
     *
     *  <p>Fundamentally thread safe.
     *  Still lock on 'children' for get-and-update operations.
     */
    protected final List<Widget> children = new CopyOnWriteArrayList<>();

    /** Widget constructor.
     *  @param type Widget type
     */
    public ContainerWidget(final String type)
    {
    	super(type);
    }

	/** @return Child widgets in Widget tree */
    public List<Widget> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    /** @param child Widget to add as child in widget tree */
    public void addChild(final Widget child)
    {
        if (children.contains(child))
            throw new IllegalArgumentException(this +
                    " already has child widget " + child);
        children.add(child);
        child.setParent(this);
    }

    /** @param child Widget to remove as child from widget tree */
    public void removeChild(final Widget child)
    {
        children.remove(child);
        child.setParent(null);
    }
}
