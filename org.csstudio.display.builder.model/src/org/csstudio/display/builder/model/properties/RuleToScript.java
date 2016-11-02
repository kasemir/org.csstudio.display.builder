/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.RuleInfo.ExpressionInfo;

/** Transform rules into scripts
 *
 *
 *  <p>Rules produce scripts attached to widgets
 *  rules execute in response to changes in triggering
 *  PVs
 *
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class RuleToScript
{
    /** @param pvCount Number of script/rule input PVs
     *  @return Map of script variables that refer to those PVs and PVUtil calls to create them
     */
    private static Map<String,String> pvNameOptions(int pvCount)
    {   // LinkedHashMap to preserve order of PVs
        // so it looks good in generated script
        final Map<String,String> pvm = new LinkedHashMap<String,String>();
        for (int idx = 0; idx < pvCount; idx++)
        {
            final String istr = Integer.toString(idx);
            pvm.put("pv" + istr, "PVUtil.getDouble(pvs["+istr+"])"  );
            pvm.put("pvReal" + istr, "PVUtil.getDouble(pvs["+istr+"])"  );
            pvm.put("pvInt" + istr, "PVUtil.getLong(pvs["+istr+"])"  );
            pvm.put("pvLong" + istr, "PVUtil.getLong(pvs["+istr+"])"  );
            pvm.put("pvStr" + istr, "PVUtil.getString(pvs["+istr+"])"  );
            //pvm.put("pvLabels" + istr, "PVUtil.getLabels(pvs["+istr+"])"  );
        }
        return pvm;
    }

    private enum PropFormat
    {
        NUMERIC, BOOLEAN, STRING, COLOR
    }

    private static String formatPropVal(WidgetProperty<?> prop, int exprIDX, PropFormat pform)
    {
        switch(pform)
        {
        case BOOLEAN:
            return (Boolean) prop.getValue() ? "True" : "False";
        case STRING:
            return "\"" + prop.getValue() + "\"";
        case COLOR:
            if (exprIDX >= 0)
                return "colorVal" + String.valueOf(exprIDX);
            else
                return "colorCurrent";
        case NUMERIC:
        default:
            return String.valueOf(prop.getValue());
        }
    }


    private static int countMatches(String s, char c)
    {
        int counter = 0;
        for( int i=0; i<s.length(); i++ ) {
            if( s.charAt(i) == c ) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Substitute the string "True" for all instances of string "true" to update old javascript rules into Python
     */
    protected static String TrueFortrue(final String instr)
    {
        //return instr.replaceAll("(\\W|^)(true)", "$1True");
        Matcher m = Pattern.compile("(.*?)((true)+)").matcher(instr);
        StringBuffer sb = new StringBuffer();

        boolean inquotes=false;
        while(m.find()) {
            if ((countMatches(m.group(1), '\"') % 2) == 1)
                inquotes = !inquotes;
            if (inquotes)
                m.appendReplacement(sb, m.group(1) + m.group(2));
            else if (m.group(1).matches(".*\\w"))
                m.appendReplacement(sb, m.group(1) + m.group(2));
            else if (m.group(2).matches("true(true)+"))
                m.appendReplacement(sb, m.group(1) + m.group(2));
            else
                m.appendReplacement(sb, m.group(1) + "True");
        }
        m.appendTail(sb);

        return sb.toString();
    }

    /**
     * Substitute Python logical operators 'and', 'or', and 'not' for old
     * javascript operators '&&', '||', '!'
     */
    protected static String replaceLogicalOperators(final String instr)
    {
        //matches '&&', '||', and '!', but not '!='
        Matcher m = Pattern.compile("((.*?) ?)(\\&\\&|\\|\\||!(?!=)) ?").matcher(instr);
        Pattern qp = Pattern.compile("(?<!\\\\)\\\""); //matches `"` but not `\"`
        StringBuffer sb = new StringBuffer();

        boolean inquotes = false;
        while (m.find())
        {
            final Matcher qm = qp.matcher(m.group(2));
            int quotes = 0;
            while (qm.find())
                quotes++;
            if ((quotes & 1) != 0)
                inquotes = !inquotes;
            if (!inquotes)
            {
                String operator = m.group(3);
                if (operator.equals("&&"))
                    operator = "and";
                else if (operator.equals("||"))
                    operator = "or";
                else if (operator.equals("!"))
                    operator = "not";
                //quoteReplacement for group(2) to preserve escaping '\'
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(2)) + ' ' + operator + ' ');
            }
            else
                m.appendReplacement(sb, m.group(1) + m.group(3));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String generatePy(final Widget attached_widget, final RuleInfo rule)
    {
        WidgetProperty<?> prop = attached_widget.getProperty(rule.getPropID());

        //TODO: Replace macros from attached_widget.getMacrosOrProperties()?
        // Example of replacing macros:
        // final String script_name = MacroHandler.replace(macros, script_info.getPath());

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

        final StringBuilder script = new StringBuilder();
        script.append("## Script for Rule: ").append(rule.getName()).append("\n\n");
        script.append("from org.csstudio.display.builder.runtime.script import PVUtil\n");
        if (pform == PropFormat.COLOR)
            script.append("from org.csstudio.display.builder.model.properties import WidgetColor\n");

        script.append("\n## Process variable extraction\n");
        script.append("## Use any of the following valid variable names in an expression:\n");

        Map<String,String> pvm = pvNameOptions(rule.getPVs().size());

        for (Map.Entry<String, String> entry : pvm.entrySet())
            script.append("##     " + entry.getKey() + "\n");
        script.append("\n");

        // Check which pv* variables are actually used
        Map<String,String> output_pvm = new HashMap<String,String>();
        for (ExpressionInfo<?> expr : rule.getExpressions())
        {
            // Check the boolean expressions.
            // In principle, should parse an expression like
            //   pv0 > 10
            // to see if it refers to the variable "pv0".
            // Instead of implementing a full parser, we
            // just check for "pv0" anywhere in the expression.
            // This will erroneously detect a variable reference in
            //   len("Text with pv0")>4
            // which doesn't actually reference "pv0" as a variable,
            // but it doesn't matter if the script creates some
            // extra variable "pv0" which is then left unused.
            String expr_to_check = expr.getBoolExp();
            // If properties are also expressions, check those by
            // simply including them in the string to check
            if (rule.getPropAsExprFlag())
                expr_to_check += " " + expr.getPropVal().toString();
            for (Map.Entry<String, String> entry : pvm.entrySet())
            {
                final String varname = entry.getKey();
                if (expr_to_check.contains(varname)) {
                    output_pvm.put(varname, entry.getValue());
                }
            }
        }
        // Generate code that reads the required pv* variables from PVs
        for (Map.Entry<String, String> entry : output_pvm.entrySet())
            script.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");

        if (pform == PropFormat.COLOR)
        {   // If property is a color, create variables for all the used colors
            script.append("\n## Define Colors\n");
            WidgetColor col = (WidgetColor) prop.getValue();
            script.append("colorCurrent = ")
                  .append("WidgetColor(").append(col.getRed()).append(", ")
                                         .append(col.getGreen()).append(", ")
                                         .append(col.getBlue()).append(")\n");

            if (!rule.getPropAsExprFlag())
            {
                int idx = 0;
                for (ExpressionInfo<?> expr : rule.getExpressions())
                {
                    if (expr.getPropVal() instanceof WidgetProperty<?>)
                    {
                        final Object value = (( WidgetProperty<?>)expr.getPropVal()).getValue();
                        if (value instanceof WidgetColor)
                        {
                            col = (WidgetColor) value;
                            script.append("colorVal").append(idx).append(" = ")
                                  .append("WidgetColor(").append(col.getRed()).append(", ")
                                  .append(col.getGreen()).append(", ")
                                  .append(col.getBlue()).append(")\n");
                        }
                    }
                    idx++;
                }
            }
        }

        script.append("\n## Script Body\n");
        String indent = "    ";

        String setPropStr = "widget.setPropertyValue('" + rule.getPropID() + "', ";
        int idx = 0;
        for (ExpressionInfo<?> expr : rule.getExpressions())
        {
            script.append((idx == 0) ? "if" : "elif");
            script.append(" ").append(replaceLogicalOperators(TrueFortrue(expr.getBoolExp()))).append(":\n");
            script.append(indent).append(setPropStr);
            if (rule.getPropAsExprFlag())
                script.append(replaceLogicalOperators(TrueFortrue(expr.getPropVal().toString()))).append(")\n");
            else
                script.append(formatPropVal((WidgetProperty<?>) expr.getPropVal(), idx, pform)).append(")\n");
            idx++;
        }

        if (idx > 0)
        {
            script.append("else:\n");
            script.append(indent).append(setPropStr).append(formatPropVal(prop, -1, pform)).append(")\n");
        }
        else
            script.append(setPropStr).append(formatPropVal(prop, -1, pform)).append(")\n");

        return script.toString();
    }

    /** Add line numbers to script
     *  @param script Script text
     *  @return Same text with line numbers
     */
    public static String addLineNumbers(final String script)
    {
        final String[] lines = script.split("\r\n|\r|\n");
        // Reserve buffer for script, then on each line add "1234: "
        final StringBuilder ret = new StringBuilder(script.length() + lines.length*6);
        for (int ldx = 0; ldx < lines.length; ldx++)
            ret.append(String.format("%4d", ldx+1)).append(": ").append(lines[ldx]).append("\n");
        return ret.toString();
    }
}
