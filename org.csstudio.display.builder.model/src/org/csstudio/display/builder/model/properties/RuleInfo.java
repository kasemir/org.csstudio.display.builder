/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.StructuredWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.macros.MacroValueProvider;

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

	public static class ExpressionInfo<T>
	{
		private final String bool_exp;
		private final T val_exp;

		public ExpressionInfo(final String bool_exp, final T val_exp)
		{
			this.bool_exp = bool_exp;
			this.val_exp = val_exp;
		}

		public String getBoolExp()
		{
			return bool_exp;
		}

		public T getValExp()
		{
			return val_exp;
		}
	};

	private final List<ExpressionInfo<?>> expressions;
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
			final List<ExpressionInfo<?>> exprs, final List<ScriptPV> pvs)
	{
		this.name = name;
		this.output_expression = out_expr;
		this.prop_id = prop_id;
		this.expressions = Collections.unmodifiableList(Objects.requireNonNull(exprs));
		this.pvs = Collections.unmodifiableList(Objects.requireNonNull(pvs));
	}

	/** Some properties cannot be the target of rules. This function takes a widget and returns a list of
	 * valid targets for expressions
	 * @param attached_widget
	 * @return List of all properties of a widget that a rule can target
	 */
	static public List<WidgetProperty<?>> getTargettableProperties (Widget attached_widget)
	{
		List<WidgetProperty<?>> propls = new ArrayList<>();

		attached_widget.getProperties().forEach(prop ->
		{
			// Do not include properties from categories: WIDGET or RUNTIME
			// Do not include properties that are not supported in scripting
			WidgetPropertyCategory pcat = prop.getCategory();
			switch(pcat)
			{
			case WIDGET:
			case RUNTIME:
				break;
			default:
			{
				if ( !(prop instanceof MacrosWidgetProperty) &&
						!(prop instanceof ActionsWidgetProperty) &&
						!(prop instanceof ScriptsWidgetProperty) &&
						!(prop instanceof RulesWidgetProperty) &&
						!(prop instanceof StructuredWidgetProperty) &&
						!(prop instanceof ArrayWidgetProperty) )
				{
					propls.add(prop);
				}
			}
			}
		});

		return propls;
	}


	/** @return Expressions consisting of (bool expression, target) pairs
	 */
	public List<ExpressionInfo<?>> getExpressions()
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

	public String getTextPy(final Widget attached_widget, final MacroValueProvider macros)
	{
		return RuleToScript.generatePy(attached_widget, macros, this);
	}

	public String getNumberedTextPy(final Widget attached_widget, final MacroValueProvider macros)
	{
		final String scr = RuleToScript.generatePy(attached_widget, macros, this);
		String ret = "";
		String[] lines = scr.split("\r\n|\r|\n");
		for (int ldx = 0; ldx < lines.length; ldx++)
		{
			ret += String.format("%4d", ldx+1) + ": " + lines[ldx] + "\n";
		}
		return ret;
	}

	@Override
	public String toString()
	{
		return "RuleInfo('" + name + ": " + expressions + "', " + pvs + ")";
	}
}
