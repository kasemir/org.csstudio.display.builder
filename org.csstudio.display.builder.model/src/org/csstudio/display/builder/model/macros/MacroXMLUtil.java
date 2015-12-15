/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.macros;

import javax.xml.stream.XMLStreamWriter;

import org.csstudio.display.builder.model.persist.XMLUtil;
import org.w3c.dom.Element;

/** Read/write macros as XML
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class MacroXMLUtil
{
    /** Write macros (without surrounding "&ltmacros>") into XML stream
     *  @param writer XML writer
     *  @param macros Macros to write
     *  @throws Exception on error
     */
    public static void writeMacros(final XMLStreamWriter writer, final Macros macros) throws Exception
    {
        // XXX Write if parent macros are inherited (or forget about that concept, they're always inherited)
        for (String name : macros.getNames())
        {
            writer.writeStartElement(name);
            writer.writeCharacters(macros.getValue(name));
            writer.writeEndElement();
        }
    }

    /** Read content of "&ltmacros>"
     *  @param macros_xml XML that contains macros
     */
    public static Macros readMacros(final Element macros_xml)
    {
        final Macros macros = new Macros();
        for (Element element = XMLUtil.findElement(macros_xml.getFirstChild());
             element != null;
             element = XMLUtil.findElement(element.getNextSibling()))
        {
            final String name = element.getTagName();
            final String value = XMLUtil.getString(element);
            // Legacy used 'include_parent_macros'
            // in a way that actually conflicts with a macro of that name.
            // This implementation _always_ inherits parent macros,
            // so that setting is obsolete.
            if (name.equals("include_parent_macros"))
                continue;
            macros.add(name, value);
        }
        return macros;
    }
}
