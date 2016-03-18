/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javafx.util.Pair;

/** Information about a rule
 *
 *
 *  <p>PVs will be created for each input/output.
 *  The rule is executed whenever one or
 *  more of the 'triggering' inputs receive
 *  a new value.
 *
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class RuleInfo
{
    private final List<Pair<String,String>> expressions;
    private final List<ScriptPV> pvs;
    private final String name;
    private final String prop_id;
    private final boolean output_expression;

    /** @param name Name of rule
     *  @param prop_id property that this rule applies to
     *  @param out_expr Set to true if expressions output expressions, false if output values
     *  @param exprs Pairs of (boolean expression , output), where output is either a value or another expression
     *  @param pvs PVs
     */
    public RuleInfo(final String name, final String prop_id, final boolean out_expr,
            final List<Pair<String,String>> exprs, final List<ScriptPV> pvs)
    {
        this.name = name;
        this.output_expression = out_expr;
        this.prop_id = prop_id;
        this.expressions = Collections.unmodifiableList(Objects.requireNonNull(exprs));
        this.pvs = Collections.unmodifiableList(Objects.requireNonNull(pvs));
    }

    /** @return Path to the script. May be URL, or contain macros.
     *          File ending or magic EMBEDDED_* name determines type of script
     */
    public List<Pair<String,String>> getExpressions()
    {
        return expressions;
    }

    /** @return Input/Output PVs used by the script */
    public List<ScriptPV> getPVs()
    {
        return pvs;
    }

    public String getName()
    {
        return name;
    }

    public String getPropID()
    {
        return prop_id;
    }

    public boolean getOutputExprFlag()
    {
        return output_expression;
    }

    @Override
    public String toString()
    {
        return "RuleInfo('" + name + ": " + expressions + "', " + pvs + ")";
    }
}
