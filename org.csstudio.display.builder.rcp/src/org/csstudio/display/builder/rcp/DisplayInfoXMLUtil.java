/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.csstudio.display.builder.model.persist.XMLTags;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.w3c.dom.Element;

/** XML support for {@link DisplayInfo}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DisplayInfoXMLUtil
{
    /** @param display_info DisplayInfo
     *  @return XML for one display info
     *  @throws Exception on error
     */
    public static String toXML(final DisplayInfo display_info) throws Exception
    {
        final ByteArrayOutputStream xml = new ByteArrayOutputStream();
        final XMLStreamWriter writer =
                XMLOutputFactory.newInstance().createXMLStreamWriter(xml, XMLUtil.ENCODING);
        writeDisplayInfo(writer, display_info);
        return xml.toString();
    }

    /** Write display info into XML stream
     *  @param writer XML writer
     *  @param display_info Display info to write
     *  @throws Exception on error
     */
    public static void writeDisplayInfo(final XMLStreamWriter writer, final DisplayInfo display_info) throws Exception
    {
        writer.writeStartElement(XMLTags.DISPLAY);

        writer.writeStartElement(XMLTags.NAME);
        writer.writeCharacters(display_info.getName());
        writer.writeEndElement();

        writer.writeStartElement(XMLTags.FILE);
        writer.writeCharacters(display_info.getPath());
        writer.writeEndElement();

        writer.writeEndElement();
    }

    /** @param xml XML for one display info
     *  @return DisplayInfo
     *  @throws Exception on error
     */
    public static DisplayInfo fromXML(final String xml) throws Exception
    {
        final ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        final Element root = XMLUtil.openXMLDocument(stream, XMLTags.DISPLAY);
        return readDisplayInfo(root);
    }

    /** @param xml XML for "&lt;displays>", containing zero or more display infos
     *  @return List of {@link DisplayInfo}s
     *  @throws Exception on error
     */
    public static List<DisplayInfo> fromDisplaysXML(final String xml) throws Exception
    {
        final List<DisplayInfo> displays = new ArrayList<>();
        final ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        final Element root = XMLUtil.openXMLDocument(stream, "displays");
        for (Element display : XMLUtil.getChildElements(root, XMLTags.DISPLAY))
            displays.add(readDisplayInfo(display));
        return displays;
    }

    /** Read display info
     *  @param macros_xml
     *  @throws Exception on error
     */
    public static DisplayInfo readDisplayInfo(final Element display) throws Exception
    {
        final String path = XMLUtil.getChildString(display, XMLTags.FILE).orElseThrow(() -> new Exception("Missing display path"));
        final String name = XMLUtil.getChildString(display, XMLTags.NAME).orElse(path);
        return new DisplayInfo(path, name);
    }
}
