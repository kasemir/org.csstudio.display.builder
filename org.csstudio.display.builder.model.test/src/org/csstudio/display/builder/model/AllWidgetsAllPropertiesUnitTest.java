/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;


import static org.csstudio.display.builder.model.FileMatcher.hasSameTextContent;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
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

    /** Check example file
     *  @throws Exception on error
     */
    @Test
    public void testDemoFile() throws Exception
    {
        final File original = new File(EXAMPLE_FILE);
        final ModelReader reader = new ModelReader(new FileInputStream(original));
        final DisplayModel model = reader.readModel();

        // Assert that demo file includes all widgets, all properties
        for (final WidgetDescriptor widget_type : WidgetFactory.getInstance().getWidgetDescriptions())
            checkWidgetType(model, widget_type);

        // Write back out
        final File copy = new File("/tmp/test.opi");
        final ModelWriter writer = new ModelWriter(new FileOutputStream(copy));
        writer.writeModel(model);
        writer.close();

        // Compare with original
        assertThat(copy, hasSameTextContent(original));
    }

    // Check if model contains widget type
    private void checkWidgetType(final DisplayModel model, final WidgetDescriptor widget_type)
    {
        final Optional<Widget> widget = model.getChildren()
                                             .stream()
                                             .filter(w -> w.getType().equals(widget_type.getType()))
                                             .findAny();
        assertTrue("Missing widget of type " + widget_type, widget.isPresent());
        checkProperties(widget.get());
    }

    // Check if widget has non-default value for each property
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
