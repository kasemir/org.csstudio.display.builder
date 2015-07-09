/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Optional;

import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.junit.Test;

/** JUnit test that checks if a demo file containts
 *  all widget types and all properties
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class AllWidgetsAllPropertiesUnitTest
{
    private static final String EXAMPLE_FILE = "../org.csstudio.display.builder.runtime.test/examples/all_widgets.opi";

    /** Check common widget properties
     *  @throws Exception on error
     */
    @Test
    public void testDemoFile() throws Exception
    {
        final ModelReader reader = new ModelReader(new FileInputStream(EXAMPLE_FILE));
        final DisplayModel model = reader.readModel();

        // Assert that demo file includes all widgets, all properties
        for (final WidgetDescriptor widget_type : WidgetFactory.getInstance().getWidgetDescriptions())
            checkWidgetType(model, widget_type);

        // Write back out
        final ModelWriter writer = new ModelWriter(new FileOutputStream(EXAMPLE_FILE + "2"));
        writer.writeModel(model);
        writer.close();

        // TODO Compare with original
    }

    private void checkWidgetType(final DisplayModel model, final WidgetDescriptor widget_type)
    {
        final Optional<Widget> widget = model.getChildren()
                                             .stream()
                                             .filter(w -> w.getType().equals(widget_type.getType()))
                                             .findAny();
        assertTrue("Missing widget of type " + widget_type, widget.isPresent());
        checkProperties(widget.get());
    }

    private void checkProperties(final Widget widget)
    {
        System.out.println("Widget type " + widget.getType());
        for (final WidgetProperty<?> property : widget.getProperties())
        {
            if (property.isReadonly())
                continue;
            if (property.getCategory() == WidgetPropertyCategory.RUNTIME)
                continue;
            System.out.println(" " + property.getName());
            assertFalse("Widget " + widget.getType() +
                        " missing value for " + property.getName(),
                        property.isDefaultValue());
        }
    }
}
