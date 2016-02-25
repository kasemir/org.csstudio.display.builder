/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorActions;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorScripts;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionHeight;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionY;

import java.util.Collections;
import java.util.List;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.properties.ScriptInfo;

/** Base class for all widgets.
 *
 *  <p>A Widget has properties, supporting read access, subscription
 *  and for most properties also write access.
 *
 *  <p>Widgets are part of a hierarchy.
 *  Their parent is either the {@link DisplayModel} or another
 *  {@link ContainerWidget}.
 *
 *  @author Kay Kasemir
 */
public class BaseWidget extends Widget
{
    private WidgetProperty<Integer> x;
    private WidgetProperty<Integer> y;
    private WidgetProperty<Integer> width;
    private WidgetProperty<Integer> height;
    private WidgetProperty<List<ActionInfo>> actions;
    private WidgetProperty<List<ScriptInfo>> scripts;

    /** Widget constructor.
     *  @param type Widget type
     */
    public BaseWidget(final String type)
    {
        super(type);
    }

    /** Called on construction to define widget's properties.
     *
     *  <p>Mandatory properties have already been defined.
     *  Derived class overrides to add its own properties.
     *
     *  @param properties List to which properties must be added
     */
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        properties.add(x = positionX.createProperty(this, 0));
        properties.add(y = positionY.createProperty(this, 0));
        properties.add(width = positionWidth.createProperty(this, 100));
        properties.add(height = positionHeight.createProperty(this, 20));
        properties.add(actions = behaviorActions.createProperty(this, Collections.emptyList()));
        properties.add(scripts = behaviorScripts.createProperty(this, Collections.emptyList()));
    }

    /** @return Position 'x' */
    public WidgetProperty<Integer> positionX()
    {
        return x;
    }

    /** @return Position 'y' */
    public WidgetProperty<Integer> positionY()
    {
        return y;
    }

    /** @return Position 'width' */
    public WidgetProperty<Integer> positionWidth()
    {
        return width;
    }

    /** @return Position 'height' */
    public WidgetProperty<Integer> positionHeight()
    {
        return height;
    }

    /** @return Behavior 'actions' */
    public WidgetProperty<List<ActionInfo>> behaviorActions()
    {
        return actions;
    }

    /** @return Behavior 'scripts' */
    public WidgetProperty<List<ScriptInfo>> behaviorScripts()
    {
        return scripts;
    }
}
