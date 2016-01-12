/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

/** Horizontal alignment
 *  @author Kay Kasemir
 */
public enum HorizontalAlignment
{
    LEFT("Left"), CENTER("Center"),RIGHT("Right");
    private final String label;

    private HorizontalAlignment(final String label)
    {
        this.label = label;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
