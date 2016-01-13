/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

/** Base class for most widgets.
 *  @author Kay Kasemir
 *  @deprecated Use {@link Widget}. This class will be removed
 */
@Deprecated
public class BaseWidget extends Widget
{
    public BaseWidget(final String type)
    {
        this(type, 100, 20);
    }

    public BaseWidget(final String type, final int default_width, final int default_height)
    {
        super(type, default_width, default_height);
    }
}
