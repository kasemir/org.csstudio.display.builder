/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newStringPropertyDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamWriter;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.persist.XMLTags;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.RuleInfo.ExpressionInfo;
import org.w3c.dom.Element;

/** Widget property that describes rules
 *
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class RulesWidgetProperty extends WidgetProperty<List<RuleInfo>>
{
    private final Logger logger = Logger.getLogger(getClass().getName());

    private static final WidgetPropertyDescriptor<String> miscUnkownPropID =
            newStringPropertyDescriptor(WidgetPropertyCategory.MISC, "rule_unknown_propid", "?");

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


    public static Map<String,String> pvNameOptions(String istr)
    {
    	Map<String,String> pvm = new HashMap<String,String>();
    	
    	pvm.put("pv" + istr, "PVUtil.getDouble(pvs["+istr+"])"  );
    	pvm.put("pvReal" + istr, "PVUtil.getDouble(pvs["+istr+"])"  );
    	pvm.put("pvInt" + istr, "PVUtil.getLong(pvs["+istr+"])"  );
    	pvm.put("pvLong" + istr, "PVUtil.getLong(pvs["+istr+"])"  );
    	pvm.put("pvStr" + istr, "PVUtil.getString(pvs["+istr+"])"  );
    	//pvm.put("pvLabels" + istr, "PVUtil.getLabels(pvs["+istr+"])"  );
    	
    	return pvm;
    }
    
    public static Map<String,String> pvNameOptions(int pvCount)
    {
        Map<String,String> pvm = new HashMap<String,String>();
        
        for (int idx = 0; idx < pvCount; idx++)
        {
        	String istr = String.valueOf(idx);
        	pvm.putAll(pvNameOptions(istr));
        }
        
        return pvm;
    }

    private enum PropFormat {
        NUMERIC, BOOLEAN, STRING, COLOR
    }
    
    private static String formatPropVal(WidgetProperty<?> prop, int exprIDX, PropFormat pform)
    {
    	String ret = null;
    	
    	switch(pform)
    	{
    	case NUMERIC:
    		ret = String.valueOf(prop.getValue());
    		break;
    	case BOOLEAN:
    		ret = (Boolean) prop.getValue() ? "True" : "False";
    		break;
    	case STRING:
    		ret = "\"" + prop.getValue() + "\"";
    		break;
    	case COLOR:
    		if (exprIDX >= 0) {
    			ret = "colorVal" + String.valueOf(exprIDX);
    		}
    		else {
    			ret = "colorCurrent";
    		}
    		break;
    	}
    	
    	return ret;
    }

    public static String generatePy(final Widget attached_widget, final RuleInfo rule)
    {
        WidgetProperty<?> prop = attached_widget.getProperty(rule.getPropID());
        //Now we have a property. There is a big selection statement under PropertyPanelSection.createUI that shows
        //how to check for various types of property instances, e.g. Integer, Enum, etc.

        //We will do this same thing in the write to xml part..., so the expressions part of RuleInfo needs to map from String:WidgetProperty<?> when using values;
        //Then we can pull the property and call prop.writeToXML or prop.readFromXML

        //The challenge here is to take prop_id and determine what value needs to get sent to:
        //widget.setPropertyValue(prop_id, value), which calls getProperty(name).setValueFromObject(value);

        //So, if the rule stores the expression targets as WidgetProperties, value will be the right type
        // ... but need output value into script such that it reads back in correctly
        // ... Can we declare a widgetProperty<?> =  in the javascript, then pass that in?
        //Then, the only problem is getting the right WidgetProperty<?> object made when we read from XML
        //So just declare a prop like above, pull the property, copy it into the RuleInfo, then reset value from XML

        PropFormat pform = PropFormat.STRING;

        if (prop.getDefaultValue() instanceof Number)
        {
            pform = PropFormat.NUMERIC;
        }
        else if (prop.getDefaultValue() instanceof Boolean)
        {
            pform = PropFormat.BOOLEAN;
        }
        else if (prop.getDefaultValue() instanceof WidgetColor)
        {
            pform = PropFormat.COLOR;
        }

        String script_str = "## Script for Rule: " + rule.getName() + "\n\n";

        script_str += "from org.csstudio.display.builder.runtime.script import PVUtil\n";
        if (pform == PropFormat.COLOR)
        {
            script_str += "from org.csstudio.display.builder.model.properties import WidgetColor\n";
        }
        
        script_str += "\n## Process variable extraction\n";
        
        Map<String,String> pvm = pvNameOptions(rule.getPVs().size());
        for (ExpressionInfo<?> expr : rule.getExpressions())
        {
        	String[] toks = expr.getBoolExp().split("\\s");
        	for (String tok : toks)
        	{
        		String mapping = pvm.get(tok);
        		if (mapping != null)
        		{
        			script_str += tok + " = " + mapping + "\n";
        		}
        		
        	}
        }
        
        if (pform == PropFormat.COLOR)
        {
        	script_str += "\n## Define Colors\n";
        	WidgetColor col = (WidgetColor) prop.getValue();
        	script_str += "colorCurrent = "
                       + "WidgetColor(" + String.valueOf(col.getRed()) 
     			       + ", " + String.valueOf(col.getGreen())
     			       + ", " + String.valueOf(col.getBlue()) + ")\n";
        	
        	int idx = 0;
            for (ExpressionInfo<?> expr : rule.getExpressions())
            {
            	col = ((WidgetProperty<WidgetColor>) expr.getValExp()).getValue();
            	script_str += "colorVal" + String.valueOf(idx) + " = "
                           + "WidgetColor(" + String.valueOf(col.getRed()) 
            			   + ", " + String.valueOf(col.getGreen())
            			   + ", " + String.valueOf(col.getBlue()) + ")\n";
            	idx++;
            }
        }
        
        script_str += "\n## Script Body\n";
        
        String setPropStr = "widget.setPropertyValue( \"" + rule.getPropID() + "\", ";
        int idx = 0;
        for (ExpressionInfo<?> expr : rule.getExpressions())
        {
        	script_str += (idx == 0) ? "if" : "elif";
        	script_str += " (" + expr.getBoolExp() + "):\n";
        	script_str += "    " + setPropStr;
        	if (rule.getOutputExprFlag())
        	{
        		script_str += expr.getValExp() + " )\n";
        	}
        	else
        	{
        		script_str += formatPropVal((WidgetProperty<?>) expr.getValExp(), idx, pform) + " )\n";
        	}
        }
        script_str += "else:\n";
        script_str += setPropStr + formatPropVal(prop, -1, pform) + " )\n";

        return script_str;
    }

    public String generatePy(String rulename)
    {
        RuleInfo found_rule = null;

        for (RuleInfo rule : this.getValue())
        {
            if (rule.getName() == rulename)
            {
                found_rule = rule;
                break;
            }
        }

        if (found_rule == null)
        {
        	return "No such rule: " + rulename;
        }

        return generatePy(this.getWidget(), found_rule);
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

            for ( final ExpressionInfo<?> expr : info.getExpressions())
            {
                // <exp bool_exp="foo==1">
                writer.writeStartElement("exp");
                writer.writeAttribute("bool_exp", expr.getBoolExp());

                if (info.getOutputExprFlag())
                {
                    // <expression>
                    writer.writeStartElement("expression");
                    if (!(expr.getValExp() instanceof String))
                    {
                        logger.log(Level.SEVERE, "Mismatch of rules output expression flag with expression value type, expected String, got ", expr.getValExp().getClass());
                        writer.writeCharacters("ERROR");
                    }
                    // some string of the value or expression
                    writer.writeCharacters((String)expr.getValExp());
                }
                else
                {
                    // <value>
                    writer.writeStartElement("value");
                    if (!(expr.getValExp() instanceof WidgetProperty<?>))
                    {
                        logger.log(Level.SEVERE, "Mismatch of rules output expression flag with expression value type, expected Widget Property, got ", expr.getValExp().getClass());
                        writer.writeCharacters("ERROR");
                    }
                   // write the property
                   ((WidgetProperty<?>)expr.getValExp()).writeToXML(model_writer, writer);
                }
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

            List<ExpressionInfo<?>> exprs = readExpressions(model_reader, prop_id, out_exp, xml);

            final List<ScriptPV> pvs = readPVs(xml);
            rules.add(new RuleInfo(name, prop_id, out_exp, exprs, pvs));
        }
        setValue(rules);
    }

    private List<ExpressionInfo<?>> readExpressions(final ModelReader model_reader,
                                                    final String prop_id,
                                                    final boolean out_exp,
                                                    final Element xml) throws Exception
    {
        final List<ExpressionInfo<?>> exprs = new ArrayList<>();
        final Iterable<Element> exprs_xml;
        exprs_xml = XMLUtil.getChildElements(xml, "exp");
        final String tagstr = (out_exp) ? "expression" : "value";

        for (final Element exp_xml : exprs_xml)
        {
            String bool_exp = exp_xml.getAttribute("bool_exp");
            if (bool_exp.isEmpty())
                Logger.getLogger(getClass().getName())
                      .log(Level.WARNING, "Missing exp 'bool_exp'");

            final Element tag_xml = XMLUtil.getChildElement(exp_xml, tagstr);
            //legacy case where value is used for all value expression
            final Element val_xml = (tag_xml == null) ? XMLUtil.getChildElement(exp_xml, "value") : tag_xml;
            final String val_str = val_xml != null
                    ? val_xml.getFirstChild().getNodeValue()
                    : null;

             if (out_exp)
             {
                 exprs.add(new ExpressionInfo<String>(bool_exp, val_str));
             }
             else
             {
                 Optional<WidgetProperty<?>> prop = this.getWidget().checkProperty(prop_id);
                 WidgetProperty<?> val_prop = null;
                 if (!prop.isPresent())
                 {
                     Logger.getLogger(getClass().getName())
                     .log(Level.WARNING, "Widget " + this.getWidget().getClass().getName()
                             + " rule indicates unknown property id " + prop_id);
                     val_prop = miscUnkownPropID.createProperty(null, prop_id + " : " + val_str);
                 }
                 else
                 {
                    val_prop = prop.get().clone();
                    val_prop.readFromXML(model_reader, val_xml);
                 }
                 exprs.add(new ExpressionInfo<WidgetProperty<?>>(bool_exp, val_prop));
             }
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
