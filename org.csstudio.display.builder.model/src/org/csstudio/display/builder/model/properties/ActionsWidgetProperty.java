/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamWriter;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.XMLTags;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.w3c.dom.Element;

/** Widget property that describes actions.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ActionsWidgetProperty extends WidgetProperty<List<ActionInfo>>
{
    private static final String OPEN_DISPLAY = "open_display";

    /** Constructor
     *  @param descriptor Property descriptor
     *  @param widget Widget that holds the property and handles listeners
     *  @param default_value Default and initial value
     */
    public ActionsWidgetProperty(
            final WidgetPropertyDescriptor<List<ActionInfo>> descriptor,
            final Widget widget,
            final List<ActionInfo> default_value)
    {
        super(descriptor, widget, default_value);
    }

    /** @param value Must be ActionInfo array(!), not List */
    @Override
    public void setValueFromObject(final Object value) throws Exception
    {
        if (value instanceof ActionInfo[])
            setValue(Arrays.asList((ActionInfo[]) value));
        else
            throw new Exception("Need ActionInfo[], got " + value);
    }

    @Override
    public void writeToXML(final XMLStreamWriter writer) throws Exception
    {
        // <action type="..">
        //   <path>some/display.opi</path>
        //   <target>replace</target>
        //   <description>some/display.opi</description>
        // </action>
        for (ActionInfo info : value)
        {
            writer.writeStartElement(XMLTags.ACTION);
            if (info instanceof OpenDisplayActionInfo)
            {
                final OpenDisplayActionInfo action = (OpenDisplayActionInfo) info;
                writer.writeAttribute(XMLTags.TYPE, OPEN_DISPLAY);
                writer.writeStartElement(XMLTags.FILE);
                writer.writeCharacters(action.getFile());
                writer.writeEndElement();
                if (! action.getMacros().getNames().isEmpty())
                {
                    writer.writeStartElement(XMLTags.MACROS);
                    MacrosWidgetProperty.writeMacros(writer, action.getMacros());
                    writer.writeEndElement();
                }
                writer.writeStartElement(XMLTags.TARGET);
                writer.writeCharacters(action.getTarget().name().toLowerCase());
                writer.writeEndElement();
            }
            else
                throw new Exception("Cannot write action of type " + info.getClass().getName());
            writer.writeStartElement(XMLTags.DESCRIPTION);
            writer.writeCharacters(info.getDescription());
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    @Override
    public void readFromXML(final Element property_xml) throws Exception
    {
        final List<ActionInfo> scripts = new ArrayList<>();
        for (Element action_xml : XMLUtil.getChildElements(property_xml, XMLTags.ACTION))
        {
            final String type = action_xml.getAttribute(XMLTags.TYPE);
            final String description = XMLUtil.getChildString(action_xml, XMLTags.DESCRIPTION).orElse("");
            if (OPEN_DISPLAY.equalsIgnoreCase(type)) // legacy used uppercase type name
            {   // Use <file>, falling back to legacy <path>
                final String file = XMLUtil.getChildString(action_xml, XMLTags.FILE)
                                           .orElse(XMLUtil.getChildString(action_xml, "path")
                                                          .orElse(""));

                OpenDisplayActionInfo.Target target = OpenDisplayActionInfo.Target.REPLACE;
                // Legacy used <replace> with value 0/1/2 for TAB/REPLACE/WINDOW
                final Optional<String> replace = XMLUtil.getChildString(action_xml, "replace");
                if (replace.isPresent())
                {
                    if ("0".equals(replace.get()))
                        target = OpenDisplayActionInfo.Target.TAB;
                    else if ("2".equals(replace.get()))
                        target = OpenDisplayActionInfo.Target.WINDOW;
                }
                else
                    target = OpenDisplayActionInfo.Target.valueOf(
                        XMLUtil.getChildString(action_xml, XMLTags.TARGET)
                               .orElse(OpenDisplayActionInfo.Target.REPLACE.name())
                               .toUpperCase() );

                final Macros macros;
                final Element macro_xml = XMLUtil.getChildElement(action_xml, XMLTags.MACROS);
                if (macro_xml != null)
                    macros = MacrosWidgetProperty.readMacros(macro_xml);
                else
                    macros = new Macros();

                scripts.add(new OpenDisplayActionInfo(description, file, macros, target));
            }
            else
                Logger.getLogger(getClass().getName())
                    .log(Level.WARNING, "Ignoring action of unknown type '" + type + "'");
        }
        setValue(scripts);
    }
}
