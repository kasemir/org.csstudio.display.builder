/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.persist;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.csstudio.display.builder.model.ContainerWidget;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;

/** Write model as XML.
 *
 *  <p>For each widget, writes each property.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ModelWriter implements Closeable
{
    private final XMLStreamWriter writer;

    /** Convert model into XML
     *  @param model DisplayModel
     *  @return XML for the model
     *  @throws Exception on error
     */
    public static String getXML(final DisplayModel model) throws Exception
    {
        final ByteArrayOutputStream xml = new ByteArrayOutputStream();
        try
        (
            final ModelWriter writer = new ModelWriter(xml);
        )
        {
            writer.writeModel(model);
        }
        return xml.toString();
    }

    /** Create writer.
     *
     *  <p>Best used in try-with-resources to support auto-close.
     *
     *  @param stream Output stream to write, will be closed
     *  @throws Exception on error
     */
    public ModelWriter(final OutputStream stream) throws Exception
    {
        final XMLStreamWriter base =
            XMLOutputFactory.newInstance().createXMLStreamWriter(stream, XMLUtil.ENCODING);
        writer = new IndentingXMLStreamWriter(base);

        writer.writeStartDocument(XMLUtil.ENCODING, "1.0");
        writer.writeStartElement(XMLTags.DISPLAY);
        writer.writeAttribute(XMLTags.VERSION, "1.0.0");
    }

    /** Write display model
     *  @param model Display model to write
     *  @throws Exception on error
     */
    public void writeModel(final DisplayModel model) throws Exception
    {
        // Write properties of display itself
        writeWidgetProperties(model);

        // Write each widget of the display
        writeChildWidgets(model);
    }

    private void writeChildWidgets(final ContainerWidget parent)  throws Exception
    {
        for (final Widget widget : parent.getChildren())
            writeWidget(widget);
    }

    /** Write widget
     *  @param widget Widget to write
     *  @throws Exception on error
     */
    protected void writeWidget(final Widget widget) throws Exception
    {   // 'protected' to allow unit test calls
        writer.writeStartElement(XMLTags.WIDGET);
        writer.writeAttribute(XMLTags.TYPE, widget.getType());
        writer.writeAttribute(XMLTags.VERSION, widget.getVersion().toString());

        writeWidgetProperties(widget);

        if (widget instanceof ContainerWidget)
            writeChildWidgets((ContainerWidget) widget);

        writer.writeEndElement();
    }

    /** @param widget All properties of this widget, except for 'type', are written
     *  @throws Exception on error
     */
    private void writeWidgetProperties(final Widget widget) throws Exception
    {
        for (final WidgetProperty<?> property : widget.getProperties())
        {
            // Skip runtime properties
            if (property.getCategory() == WidgetPropertyCategory.RUNTIME)
                continue;
            // Skip read-only properties
            if (property.isReadonly())
                continue;
            // Skip writing default values for certain properties
            if (property.isDefaultValue())
                continue;
            writeProperty(property);
        }
    }

    /** @param property Single property to write
     *  @throws Exception on error
     */
    private void writeProperty(final WidgetProperty<?> property) throws Exception
    {
        writer.writeStartElement(property.getName());
        property.writeToXML(writer);
        writer.writeEndElement();
    }

    /** Flush and close XML. */
    @Override
    public void close() throws IOException
    {
        try
        {
            // End display
            writer.writeEndElement();

            // End and close document
            writer.writeEndDocument();
            writer.flush();
            writer.close();
        }
        catch (final Exception ex)
        {
            throw new IOException("Failed to close XML", ex);
        }
    }
}
