/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

/** JUnit test of widget properties, their order, categories
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetPropertiesUnitTest
{
    /** Check common widget properties */
    @Test
    public void testListingWidgetProperties()
    {
        final Widget widget = new CustomWidget();

        for (final WidgetProperty<?> property : widget.getProperties())
            System.out.println(property.getCategory().name() + " - " + property.getName());

        // Get list of property names
        final List<String> prop_names =
            widget.getProperties().stream().map(WidgetProperty::getName).collect(Collectors.toList());
        System.out.println(prop_names);

        // "quirk" was added last, but should appear before "x"
        // because it's in the WIDGET category, while "x" is a POSITION
        assertThat(widget.getProperty("quirk").getCategory(), equalTo(WidgetPropertyCategory.WIDGET));
        assertThat(widget.getProperty("x").getCategory(), equalTo(WidgetPropertyCategory.POSITION));
        assertTrue(WidgetPropertyCategory.WIDGET.ordinal() < WidgetPropertyCategory.POSITION.ordinal());
        final int x_idx = prop_names.indexOf("x");
        final int quirk_idx = prop_names.indexOf("quirk");
        assertTrue(x_idx >= 0);
        assertTrue(quirk_idx >= 0);
        assertTrue(quirk_idx < x_idx);
    }
}
