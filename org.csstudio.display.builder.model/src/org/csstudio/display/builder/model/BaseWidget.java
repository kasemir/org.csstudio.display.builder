/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionVisible;

import java.util.List;

/** Base class for most widgets.
 *  @author Kay Kasemir
 */
public class BaseWidget extends Widget
{
    private WidgetProperty<Boolean> visible;

    public BaseWidget(final String type)
    {
        this(type, 100, 20);
    }

    public BaseWidget(final String type, final int default_width, final int default_height)
    {
        super(type, default_width, default_height);
    }

    /** Called on construction to define widget's properties.
    *
    *  <p>Mandatory properties have already been defined.
    *  Derived class overrides to add its own properties.
    *
    *  @param properties List to which properties must be added
    */
   @Override
   protected void defineProperties(final List<WidgetProperty<?>> properties)
   {
       super.defineProperties(properties);
       properties.add(visible = positionVisible.createProperty(this, true));

       // TODO colors, border
   }

   /** @return Position 'visible' */
   public WidgetProperty<Boolean> positionVisible()
   {
       return visible;
   }
}
