/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.RuntimeWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.macros.Macros;
import org.diirt.vtype.VType;

/** Common widget properties.
 *
 *  <p>
 *  Helper that defines the names of common widget properties and provides
 *  helpers for creating them.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class CommonWidgetProperties
{
    /** Constructor for string property
     *  @param category Widget property category
     *  @param name Internal name of the property
     *  @param description Human-readable description
     */
    public static final WidgetPropertyDescriptor<String> newStringPropertyDescriptor(final WidgetPropertyCategory category,
                                                                                     final String name, final String description)
    {
        return new WidgetPropertyDescriptor<String>(category, name, description)
        {
            @Override
            public WidgetProperty<String> createProperty(final Widget widget, final String value)
            {
                return new StringWidgetProperty(this, widget, value);
            }
        };
    }

    /** Constructor for Integer property
     *  @param category Widget property category
     *  @param name Internal name of the property
     *  @param description Human-readable description
     */
    public static final WidgetPropertyDescriptor<Integer> newIntegerPropertyDescriptor(final WidgetPropertyCategory category,
                                                                                       final String name, final String description)
    {
        return new WidgetPropertyDescriptor<Integer>(category, name, description)
        {
            @Override
            public WidgetProperty<Integer> createProperty(final Widget widget, final Integer value)
            {
                return new IntegerWidgetProperty(this, widget, value);
            }
        };
    }

    /** Constructor for Double property
     *  @param category Widget property category
     *  @param name Internal name of the property
     *  @param description Human-readable description
     */
    public static final WidgetPropertyDescriptor<Double> newDoublePropertyDescriptor(final WidgetPropertyCategory category,
                                                                                     final String name, final String description)
    {
        return new WidgetPropertyDescriptor<Double>(category, name, description)
        {
            @Override
            public WidgetProperty<Double> createProperty(final Widget widget, final Double value)
            {
                return new DoubleWidgetProperty(this, widget, value);
            }
        };
    }

    /** Constructor for Boolean property
     *  @param category Widget property category
     *  @param name Internal name of the property
     *  @param description Human-readable description
     */
    public static final WidgetPropertyDescriptor<Boolean> newBooleanPropertyDescriptor(final WidgetPropertyCategory category,
                                                                                       final String name, final String description)
    {
        return new WidgetPropertyDescriptor<Boolean>(category, name, description)
        {
            @Override
            public WidgetProperty<Boolean> createProperty(final Widget widget, final Boolean value)
            {
                return new BooleanWidgetProperty(this, widget, value);
            }
        };
    }

    /** Constructor for Color property
     *  @param category Widget property category
     *  @param name Internal name of the property
     *  @param description Human-readable description
     */
    public static final WidgetPropertyDescriptor<WidgetColor> newColorPropertyDescriptor(final WidgetPropertyCategory category,
                                                                                         final String name, final String description)
    {
        return new WidgetPropertyDescriptor<WidgetColor>(category, name, description)
        {
            @Override
            public WidgetProperty<WidgetColor> createProperty(final Widget widget, final WidgetColor value)
            {
                return new ColorWidgetProperty(this, widget, value);
            }
        };
    }

    /** Constructor for value property
     *  @param name Internal name of the property
     *  @param description Human-readable description
     */
    public static final WidgetPropertyDescriptor<VType> newRuntimeValue(final String name, final String description)
    {
        return new WidgetPropertyDescriptor<VType>(WidgetPropertyCategory.RUNTIME, name, description)
        {
            @Override
            public WidgetProperty<VType> createProperty(final Widget widget, final VType value)
            {
                return new RuntimeWidgetProperty<VType>(this, widget, value)
                {
                    @Override
                    public void setValueFromObject(final Object value) throws Exception
                    {
                        if (value instanceof VType)
                            setValue((VType) value);
                        else
                            throw new Exception("Need VType, got " + value);
                    }
                };
            }
        };
    }

    // All properties are described by
    // Category and property name

    /** Widget 'type': "label", "rectangle", "textupdate", .. */
    public static final WidgetPropertyDescriptor<String> widgetType = new WidgetPropertyDescriptor<String>(
        WidgetPropertyCategory.WIDGET, "type", Messages.WidgetProperties_Type, true)
    {
        @Override
        public WidgetProperty<String> createProperty(final Widget widget,
                                                     final String type)
        {
            return new StringWidgetProperty(this, widget, type);
        }
    };

    /** Widget 'name'
     *
     *  <p>Assigned by user, allows lookup of widget by name.
     *  Several widgets may have the same name,
     *  but lookup by name is then unpredictable.
     */
    public static final WidgetPropertyDescriptor<String> widgetName =
        newStringPropertyDescriptor(WidgetPropertyCategory.WIDGET, "name", Messages.WidgetProperties_Name);

    /** Widget 'macros' */
    public static final WidgetPropertyDescriptor<Macros> widgetMacros =
        new WidgetPropertyDescriptor<Macros>(
            WidgetPropertyCategory.WIDGET, "macros", Messages.WidgetProperties_Macros)
    {
        @Override
        public WidgetProperty<Macros> createProperty(final Widget widget,
                                                     final Macros macros)
        {
            return new MacrosWidgetProperty(this, widget, macros);
        }
    };

    /** Position 'x' */
    public static final WidgetPropertyDescriptor<Integer> positionX =
        newIntegerPropertyDescriptor(WidgetPropertyCategory.POSITION, "x", Messages.WidgetProperties_X);

    /** Position 'y' */
    public static final WidgetPropertyDescriptor<Integer> positionY =
        newIntegerPropertyDescriptor(WidgetPropertyCategory.POSITION, "y", Messages.WidgetProperties_Y);

    /** Position 'width' */
    public static final WidgetPropertyDescriptor<Integer> positionWidth =
        newIntegerPropertyDescriptor(WidgetPropertyCategory.POSITION, "width", Messages.WidgetProperties_Width);

    /** Position 'height' */
    public static final WidgetPropertyDescriptor<Integer> positionHeight =
        newIntegerPropertyDescriptor(WidgetPropertyCategory.POSITION, "height", Messages.WidgetProperties_Height);

    /** Position 'visible': Is position visible? */
    public static final WidgetPropertyDescriptor<Boolean> positionVisible =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.POSITION, "visible", Messages.WidgetProperties_Visible);

    /** Display 'border_alarm_sensitive' */
    public static final WidgetPropertyDescriptor<Boolean> displayBorderAlarmSensitive =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "border_alarm_sensitive", Messages.WidgetProperties_BorderAlarmSensitive);

    /** Display 'foreground_color' */
    public static final WidgetPropertyDescriptor<WidgetColor> displayForegroundColor =
        newColorPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "foreground_color", Messages.WidgetProperties_ForegroundColor);

    /** Display 'background_color' */
    public static final WidgetPropertyDescriptor<WidgetColor> displayBackgroundColor =
        newColorPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "background_color", Messages.WidgetProperties_BackgroundColor);

    /** Display 'fill_color' */
    public static final WidgetPropertyDescriptor<WidgetColor> displayFillColor =
        newColorPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "fill_color", Messages.WidgetProperties_FillColor);

    /** Display 'line_color' */
    public static final WidgetPropertyDescriptor<WidgetColor> displayLineColor =
        newColorPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "line_color", Messages.WidgetProperties_LineColor);

    /** Display 'line_width' */
    public static final WidgetPropertyDescriptor<Integer> displayLineWidth =
        newIntegerPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "line_width", Messages.WidgetProperties_LineWidth);

    /** Display 'transparent' */
    public static final WidgetPropertyDescriptor<Boolean> displayTransparent =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "transparent", Messages.WidgetProperties_Transparent);

    /** Display 'text': Text to display */
    public static final WidgetPropertyDescriptor<String> displayText =
        newStringPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "text", Messages.WidgetProperties_Text);

    /** Display 'font': Font for display */
    public static final WidgetPropertyDescriptor<WidgetFont> displayFont =
        new WidgetPropertyDescriptor<WidgetFont>(
            WidgetPropertyCategory.DISPLAY, "font", Messages.WidgetProperties_Font)
    {
        @Override
        public WidgetProperty<WidgetFont> createProperty(final Widget widget,
                                                         final WidgetFont font)
        {
            return new FontWidgetProperty(this, widget, font);
        }
    };

    /** Display 'file': File to display */
    public static final WidgetPropertyDescriptor<String> displayFile =
        newStringPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "file", Messages.WidgetProperties_File);

    /** Display 'points': Points to display */
    public static final WidgetPropertyDescriptor<Points> displayPoints =
        new WidgetPropertyDescriptor<Points>(
            WidgetPropertyCategory.DISPLAY, "points", Messages.WidgetProperties_Points)
    {
        @Override
        public WidgetProperty<Points> createProperty(final Widget widget,
                                                     final Points points)
        {
            return new PointsWidgetProperty(this, widget, points);
        }
    };

    /** Behavior 'pv_name':Primary PV Name */
    public static final WidgetPropertyDescriptor<String> behaviorPVName =
        newStringPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "pv_name", Messages.WidgetProperties_PVName);

    /** Behavior 'actions': Actions that user can invoke */
    public static final WidgetPropertyDescriptor<List<ActionInfo>> behaviorActions =
        new WidgetPropertyDescriptor<List<ActionInfo>>(
            WidgetPropertyCategory.BEHAVIOR, "actions", Messages.WidgetProperties_Actions)
    {
        @Override
        public WidgetProperty<List<ActionInfo>> createProperty(final Widget widget,
                                                               final List<ActionInfo> actions)
        {
            return new ActionsWidgetProperty(this, widget, actions);
        }
    };

    /** Behavior 'scripts': Scripts to execute */
    public static final WidgetPropertyDescriptor<List<ScriptInfo>> behaviorScripts =
        new WidgetPropertyDescriptor<List<ScriptInfo>>(
            WidgetPropertyCategory.BEHAVIOR, "scripts", Messages.WidgetProperties_Scripts)
    {
        @Override
        public WidgetProperty<List<ScriptInfo>> createProperty(final Widget widget,
                                                               final List<ScriptInfo> scripts)
        {
            return new ScriptsWidgetProperty(this, widget, scripts);
        }
    };

    /** Behavior 'limits_from_pv': Use limits from PV's meta data? */
    public static final WidgetPropertyDescriptor<Boolean> behaviorLimitsFromPV =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "limits_from_pv", Messages.WidgetProperties_LimitsFromPV);

    /** Behavior 'minimum': Minimum display range */
    public static final WidgetPropertyDescriptor<Double> behaviorMinimum =
        newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "minimum", Messages.WidgetProperties_Minimum);

    /** Behavior 'maximum': Maximum display range */
    public static final WidgetPropertyDescriptor<Double> behaviorMaximum =
        newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "maximum", Messages.WidgetProperties_Maximum);

    /** Runtime 'value': Typically read from primary PV */
    public static final WidgetPropertyDescriptor<VType> runtimeValue = newRuntimeValue("value", Messages.WidgetProperties_Value);

    /** Runtime 'insets': Container widget representations may set these. */
    public static final WidgetPropertyDescriptor<int[]> runtimeInsets =
        new WidgetPropertyDescriptor<int[]>(
            WidgetPropertyCategory.RUNTIME, "insets", Messages.WidgetProperties_Insets)
    {
        @Override
        public WidgetProperty<int[]> createProperty(final Widget widget,
                                                    final int[] value)
        {
            return new RuntimeWidgetProperty<int[]>(this, widget, value)
            {
                @Override
                public void setValueFromObject(final Object value) throws Exception
                {
                    if (value instanceof int[])
                        setValue((int[]) value);
                    else
                        throw new Exception("Need int[2], got " + value);
                }
            };
        }
    };
}
