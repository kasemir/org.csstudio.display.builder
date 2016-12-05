/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import static org.csstudio.display.builder.model.ModelPlugin.logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import javax.xml.stream.XMLStreamWriter;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.macros.MacroXMLUtil;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.persist.XMLTags;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo.Target;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Element;

/** Widget property that describes actions.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ActionsWidgetProperty extends WidgetProperty<List<ActionInfo>>
{
    private static final String OPEN_DISPLAY = "open_display";
    private static final String WRITE_PV = "write_pv";
    private static final String EXECUTE_SCRIPT = "execute";
    private static final String OPEN_FILE = "open_file";

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

    /** @param mode One of the modes from org.csstudio.opibuilder.runmode.RunModeService.DisplayMode
     *  @return
     */
    private Target modeToTargetConvert(int mode)
    {
        switch (mode)
        {
        // 0 - REPLACE
        case 0: return OpenDisplayActionInfo.Target.REPLACE;
        // 7 - NEW_WINDOW
        case 7: return OpenDisplayActionInfo.Target.WINDOW;
        // 8 - NEW_SHELL
        case 8: return OpenDisplayActionInfo.Target.STANDALONE;
        // 1 - NEW_TAB
        // 2 - NEW_TAB_LEFT
        // 3 - NEW_TAB_RIGHT
        // 4 - NEW_TAB_TOP
        // 5 - NEW_TAB_BOTTOM
        // 6 - NEW_TAB_DETACHED
        default: return OpenDisplayActionInfo.Target.TAB;
        }
    }

    @Override
    public void writeToXML(final ModelWriter model_writer, final XMLStreamWriter writer) throws Exception
    {
        for (final ActionInfo info : value)
        {
            // <action type="..">
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
                    MacroXMLUtil.writeMacros(writer, action.getMacros());
                    writer.writeEndElement();
                }
                writer.writeStartElement(XMLTags.TARGET);
                writer.writeCharacters(action.getTarget().name().toLowerCase());
                writer.writeEndElement();
            }
            else if (info instanceof WritePVActionInfo)
            {
                final WritePVActionInfo action = (WritePVActionInfo) info;
                writer.writeAttribute(XMLTags.TYPE, WRITE_PV);
                writer.writeStartElement(XMLTags.PV_NAME);
                writer.writeCharacters(action.getPV());
                writer.writeEndElement();
                writer.writeStartElement(XMLTags.VALUE);
                writer.writeCharacters(action.getValue());
                writer.writeEndElement();
            }
            else if (info instanceof ExecuteScriptActionInfo)
            {
                final ExecuteScriptActionInfo action = (ExecuteScriptActionInfo) info;
                final ScriptInfo script = action.getInfo();
                writer.writeAttribute(XMLTags.TYPE, EXECUTE_SCRIPT);
                writer.writeStartElement(XMLTags.SCRIPT);
                writer.writeAttribute(XMLTags.FILE, script.getPath());
                final String text = script.getText();
                if (text != null)
                {
                    writer.writeStartElement(XMLTags.TEXT);
                    writer.writeCData(text);
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
            else if (info instanceof OpenFileActionInfo)
            {
                final OpenFileActionInfo action = (OpenFileActionInfo) info;
                writer.writeAttribute(XMLTags.TYPE, OPEN_FILE);
                writer.writeStartElement(XMLTags.FILE);
                writer.writeCharacters(action.getFile());
                writer.writeEndElement();
            }
            else
                throw new Exception("Cannot write action of type " + info.getClass().getName());
            if (! info.getDescription().isEmpty())
            {
                writer.writeStartElement(XMLTags.DESCRIPTION);
                writer.writeCharacters(info.getDescription());
                writer.writeEndElement();
            }
            // </action>
            writer.writeEndElement();
        }
    }

    @Override
    public void readFromXML(final ModelReader model_reader, final Element property_xml) throws Exception
    {
        final List<ActionInfo> actions = new ArrayList<>();
        for (final Element action_xml : XMLUtil.getChildElements(property_xml, XMLTags.ACTION))
        {
            final String type = action_xml.getAttribute(XMLTags.TYPE);
            final String description = XMLUtil.getChildString(action_xml, XMLTags.DESCRIPTION).orElse("");
            if (OPEN_DISPLAY.equalsIgnoreCase(type)) // legacy used uppercase type name
            {   // Use <file>, falling back to legacy <path>
                final String file = XMLUtil.getChildString(action_xml, XMLTags.FILE)
                                           .orElse(XMLUtil.getChildString(action_xml, XMLTags.PATH)
                                           .orElse(""));

                OpenDisplayActionInfo.Target target = OpenDisplayActionInfo.Target.REPLACE;
                // Legacy used <replace> with value 0/1/2 for TAB/REPLACE/WINDOW
                final Optional<String> replace = XMLUtil.getChildString(action_xml, "replace");
                // later it switched to <mode> with many more options
                final Optional<String> mode = XMLUtil.getChildString(action_xml, "mode");
                if (replace.isPresent())
                {
                    if ("0".equals(replace.get()))
                        target = OpenDisplayActionInfo.Target.TAB;
                    else if ("2".equals(replace.get()))
                        target = OpenDisplayActionInfo.Target.WINDOW;
                }
                else if (mode.isPresent())
                    target = modeToTargetConvert(Integer.valueOf(mode.get()));
                else
                    target = OpenDisplayActionInfo.Target.valueOf(
                            XMLUtil.getChildString(action_xml, XMLTags.TARGET)
                            .orElse(OpenDisplayActionInfo.Target.REPLACE.name())
                            .toUpperCase() );

                final Macros macros;
                final Element macro_xml = XMLUtil.getChildElement(action_xml, XMLTags.MACROS);
                if (macro_xml != null)
                    macros = MacroXMLUtil.readMacros(macro_xml);
                else
                    macros = new Macros();

                actions.add(new OpenDisplayActionInfo(description, file, macros, target));
            }
            else if (WRITE_PV.equalsIgnoreCase(type)) // legacy used uppercase type name
            {
                // Compare legacy XML:
                // <action type="WRITE_PV">
                //     <pv_name>$(M).TWR</pv_name>
                //     <value>1</value>
                //     <timeout>10</timeout>
                //     <confirm_message/>
                //     <description>-</description>
                // </action>
                final String pv_name = XMLUtil.getChildString(action_xml, XMLTags.PV_NAME).orElse("");
                final String value = XMLUtil.getChildString(action_xml, XMLTags.VALUE).orElse("");
                if (pv_name.isEmpty()  ||  value.isEmpty())
                    logger.log(Level.WARNING, "Ignoring <action type='" + WRITE_PV + "'> with empty <pv_name> and/or <value>");
                else
                    actions.add(new WritePVActionInfo(description, pv_name, value));
            }
            else if (EXECUTE_SCRIPT.equals(type))
            {
                // <script file="EmbeddedPy">
                //   <text>  the embedded text  </text>
                // </script>
                final Element el = XMLUtil.getChildElement(action_xml, XMLTags.SCRIPT);
                if (el == null)
                    throw new Exception("Missing <script..>");
                else
                {
                    final String path = el.getAttribute(XMLTags.FILE);
                    final String text = XMLUtil.getChildString(el, XMLTags.TEXT).orElse(null);
                    final ScriptInfo info = new ScriptInfo(path, text, false, Collections.emptyList());
                    actions.add(new ExecuteScriptActionInfo(description, info));
                }
            }
            else if (type.startsWith("EXECUTE_"))
            {
                // Legacy XML:
                // <action type="EXECUTE_PYTHONSCRIPT"> .. or "EXECUTE_JAVASCRIPT"
                //     <path>script.py</path>
                //     <scriptText><![CDATA[ /* The script */ ]]></scriptText>
                //     <embedded>false</embedded>
                //     <description>A script</description>
                // </action>
                final boolean embed = Boolean.parseBoolean(XMLUtil.getChildString(action_xml, "embedded").orElse("false"));
                final String path = XMLUtil.getChildString(action_xml, XMLTags.PATH).orElse("");
                final String text = XMLUtil.getChildString(action_xml, "scriptText").orElse("");
                final ScriptInfo info;
                if (embed)
                {
                    final String dialect = type.contains("PYTHON")
                            ? ScriptInfo.EMBEDDED_PYTHON : ScriptInfo.EMBEDDED_JAVASCRIPT;
                    info = new ScriptInfo(dialect, text, false, Collections.emptyList());
                }
                else
                    info = new ScriptInfo(path, null, false, Collections.emptyList());
                actions.add(new ExecuteScriptActionInfo(description, info));
            }
            else if (OPEN_FILE.equalsIgnoreCase(type)) // legacy used uppercase type name
            {   // Use <file>, falling back to legacy <path>
                final String file = XMLUtil.getChildString(action_xml, XMLTags.FILE)
                                           .orElse(XMLUtil.getChildString(action_xml, XMLTags.PATH)
                                           .orElse(""));
                actions.add(new OpenFileActionInfo(description, file));
            }
            else
                logger.log(Level.WARNING, "Ignoring action of unknown type '" + type + "'");
        }
        setValue(actions);
    }

    @Override
    public String toString()
    {
        final List<ActionInfo> actions = value;
        if (actions.isEmpty())
            return Messages.Actions_Zero;
        if (actions.size() == 1)
            return actions.get(0).getDescription();
        return NLS.bind(Messages.Actions_N_Fmt, actions.size());
    }
}
