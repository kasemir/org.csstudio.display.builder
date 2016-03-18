/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamWriter;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.persist.XMLTags;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.w3c.dom.Element;

import javafx.util.Pair;

/** Widget property that describes rules
 *
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class RulesWidgetProperty extends WidgetProperty<List<RuleInfo>>
{
    /** Constructor
     *  @param descriptor Property descriptor
     *  @param widget Widget that holds the property and handles listeners
     *  @param default_value Default and initial value
     */
    public RulesWidgetProperty(
            final WidgetPropertyDescriptor<List<RuleInfo>> descriptor,
            final Widget widget,
            final List<RuleInfo> default_value)
    {
        super(descriptor, widget, default_value);
    }

    /** @param value Must be ScriptInfo array(!), not List */
    @Override
    public void setValueFromObject(final Object value) throws Exception
    {
        if (value instanceof RuleInfo[])
            setValue(Arrays.asList((RuleInfo[]) value));
        else
            throw new Exception("Need RuleInfo[], got " + value);
    }

    @Override
    public void writeToXML(final ModelWriter model_writer, final XMLStreamWriter writer) throws Exception
    {
        for (final RuleInfo info : value)
        {
            // <rule name="name" prop_id="prop" out_exp="true">
            writer.writeStartElement(XMLTags.RULE);
            writer.writeAttribute(XMLTags.NAME, info.getName());
            writer.writeAttribute("prop_id", info.getPropID());
            writer.writeAttribute("out_exp", String.valueOf(info.getOutputExprFlag()));

            for ( final Pair<String,String> expr : info.getExpressions())
            {
                // <exp bool_exp="foo==1">
                writer.writeStartElement("exp");
                writer.writeAttribute("bool_exp", expr.getKey());
                if (info.getOutputExprFlag())
                    // <value>
                    writer.writeStartElement("value");
                else
                    // <expression>
                    writer.writeStartElement("expression");
                // some string of the value or expression
                writer.writeCharacters(expr.getValue());
                // </value> or </expression>
                writer.writeEndElement();
                // </exp>
                writer.writeEndElement();
            }
            for (final ScriptPV pv : info.getPVs())
            {
                //<pv trig="true">
                writer.writeStartElement(XMLTags.PV_NAME);
                if (! pv.isTrigger())
                    writer.writeAttribute(XMLTags.TRIGGER, Boolean.FALSE.toString());
                //some string of the pv name
                writer.writeCharacters(pv.getName());
                //</pv>
                writer.writeEndElement();
            }
            //</rule>
            writer.writeEndElement();
        }
    }

    @Override
    public void readFromXML(final ModelReader model_reader, final Element property_xml) throws Exception
    {
        Iterable<Element> rule_xml;
        rule_xml = XMLUtil.getChildElements(property_xml, XMLTags.RULE);

        final List<RuleInfo> rules = new ArrayList<>();
        for (final Element xml : rule_xml)
        {
            String name = xml.getAttribute(XMLTags.NAME);
            if (name.isEmpty())
                Logger.getLogger(getClass().getName())
                      .log(Level.WARNING, "Missing rule 'name'");

            String prop_id = xml.getAttribute("prop_id");
            if (prop_id.isEmpty())
                Logger.getLogger(getClass().getName())
                      .log(Level.WARNING, "Missing rule 'prop_id'");

            String out_exp_str = xml.getAttribute("out_exp");
            boolean out_exp = false;
            if (out_exp_str.isEmpty())
                Logger.getLogger(getClass().getName())
                      .log(Level.WARNING, "Missing rule 'out_exp'");
            else
                out_exp = Boolean.parseBoolean(out_exp_str);

            List<Pair<String,String>> exprs;
            if (out_exp)
            {
                exprs = readExpressions("expression", xml);
            }
            else
            {
                exprs = readExpressions("value", xml);
            }

            final List<ScriptPV> pvs = readPVs(xml);
            rules.add(new RuleInfo(name, prop_id, out_exp, exprs, pvs));
        }
        setValue(rules);
    }

    private List<Pair<String,String>> readExpressions(String tagstr, final Element xml)
    {
        final List<Pair<String,String>> exprs = new ArrayList<>();
        final Iterable<Element> exprs_xml;
        exprs_xml = XMLUtil.getChildElements(xml, "exp");

        for (final Element exp_xml : exprs_xml)
        {
            String bool_exp = exp_xml.getAttribute("bool_exp");
            if (bool_exp.isEmpty())
                Logger.getLogger(getClass().getName())
                      .log(Level.WARNING, "Missing exp 'bool_exp'");

            final Element val_xml = XMLUtil.getChildElement(exp_xml, tagstr);
            final String val_str = val_xml != null
                    ? val_xml.getFirstChild().getNodeValue()
                    : null;

             exprs.add(new Pair<String,String>(bool_exp, val_str));
        }
        return exprs;
    }

    private List<ScriptPV> readPVs(final Element xml)
    {
        final List<ScriptPV> pvs = new ArrayList<>();
        // Legacy used just 'pv'
        final Iterable<Element> pvs_xml;
        if (XMLUtil.getChildElement(xml, XMLTags.PV_NAME) != null)
            pvs_xml = XMLUtil.getChildElements(xml, XMLTags.PV_NAME);
        else
            pvs_xml = XMLUtil.getChildElements(xml, "pv");
        for (final Element pv_xml : pvs_xml)
        {   // Unless either the new or old attribute is _present_ and set to false,
            // default to triggering on this PV
            final boolean trigger =
                XMLUtil.parseBoolean(pv_xml.getAttribute(XMLTags.TRIGGER), true) &&
                XMLUtil.parseBoolean(pv_xml.getAttribute("trig"), true);
            final String name = XMLUtil.getString(pv_xml);
            pvs.add(new ScriptPV(name, trigger));
        }
        return pvs;
    }
}
