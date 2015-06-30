/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.junit.Test;

@SuppressWarnings("nls")
public class EmbeddedTest
{
    @Test
    public void testEmbeddedModel() throws Exception
    {
        final ModelReader reader = new ModelReader(new FileInputStream("examples/legacy_embed.opi"));
        final DisplayModel model = reader.readModel();

        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        final ModelWriter writer = new ModelWriter(buf);
        writer.writeModel(model);
        writer.close();

        System.out.println(buf.toString());

        final Widget embedded = model.getChildByName("Embedded Example");
        assertThat(embedded, notNullValue());

        final String file = embedded.getPropertyValue(CommonWidgetProperties.displayFile);
        assertThat(file, equalTo("legacy_embedded.opi"));
    }
}
