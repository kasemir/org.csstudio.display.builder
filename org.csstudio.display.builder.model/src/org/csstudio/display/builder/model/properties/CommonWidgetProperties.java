/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
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
import org.epics.vtype.VType;

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
    // All properties are described by
    // Category and property name

    /** Widget 'type': "label", "rectangle", "textupdate", ..
     */
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
        new WidgetPropertyDescriptor<String>(
            WidgetPropertyCategory.WIDGET, "name", Messages.WidgetProperties_Name)
    {
        @Override
        public WidgetProperty<String> createProperty(final Widget widget,
                                                     final String name)
        {
            return new StringWidgetProperty(this, widget, name);
        }
    };

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
        new WidgetPropertyDescriptor<Integer>(
            WidgetPropertyCategory.POSITION, "x", Messages.WidgetProperties_X)
    {
        @Override
        public WidgetProperty<Integer> createProperty(final Widget widget,
                                                      final Integer x)
        {
            return new IntegerWidgetProperty(this, widget, x);
        }
    };

    /** Position 'y' */
    public static final WidgetPropertyDescriptor<Integer> positionY =
        new WidgetPropertyDescriptor<Integer>(
            WidgetPropertyCategory.POSITION, "y", Messages.WidgetProperties_Y)
    {
        @Override
        public WidgetProperty<Integer> createProperty(final Widget widget,
                                                      final Integer y)
        {
            return new IntegerWidgetProperty(this, widget, y);
        }
    };

    /** Position 'width' */
    public static final WidgetPropertyDescriptor<Integer> positionWidth =
        new WidgetPropertyDescriptor<Integer>(
            WidgetPropertyCategory.POSITION, "width", Messages.WidgetProperties_Width)
    {
        @Override
        public WidgetProperty<Integer> createProperty(final Widget widget,
                                                      final Integer width)
        {
            return new IntegerWidgetProperty(this, widget, width);
        }
    };

    /** Position 'height' */
    public static final WidgetPropertyDescriptor<Integer> positionHeight =
        new WidgetPropertyDescriptor<Integer>(
            WidgetPropertyCategory.POSITION, "height", Messages.WidgetProperties_Height)
    {
        @Override
        public WidgetProperty<Integer> createProperty(final Widget widget,
                                                      final Integer height)
        {
            return new IntegerWidgetProperty(this, widget, height);
        }
    };

    /** Position 'visible': Is position visible? */
    public static final WidgetPropertyDescriptor<Boolean> positionVisible =
        new WidgetPropertyDescriptor<Boolean>(
            WidgetPropertyCategory.POSITION, "visible", Messages.WidgetProperties_Visible)
    {
        @Override
        public WidgetProperty<Boolean> createProperty(final Widget widget,
                                                      final Boolean visible)
        {
            return new BooleanWidgetProperty(this, widget, visible);
        }
    };

    /** Display 'foreground_color' */
    public static final WidgetPropertyDescriptor<WidgetColor> displayForegroundColor =
        new WidgetPropertyDescriptor<WidgetColor>(
            WidgetPropertyCategory.DISPLAY, "foreground_color", Messages.WidgetProperties_ForegroundColor)
    {
        @Override
        public WidgetProperty<WidgetColor> createProperty(final Widget widget,
                                                          final WidgetColor color)
        {
            return new ColorWidgetProperty(this, widget, color);
        }
    };

    /** Display 'background_color' */
    public static final WidgetPropertyDescriptor<WidgetColor> displayBackgroundColor =
        new WidgetPropertyDescriptor<WidgetColor>(
            WidgetPropertyCategory.DISPLAY, "background_color", Messages.WidgetProperties_BackgroundColor)
    {
        @Override
        public WidgetProperty<WidgetColor> createProperty(final Widget widget,
                                                          final WidgetColor color)
        {
            return new ColorWidgetProperty(this, widget, color);
        }
    };

    /** Display 'fill_color' */
    public static final WidgetPropertyDescriptor<WidgetColor> displayFillColor = new WidgetPropertyDescriptor<WidgetColor>(
            WidgetPropertyCategory.DISPLAY, "fill_color", Messages.WidgetProperties_FillColor)
    {
        @Override
        public WidgetProperty<WidgetColor> createProperty(final Widget widget,
                                                          final WidgetColor default_color)
        {
            return new ColorWidgetProperty(this, widget, default_color);
        }
    };



    /** Display 'transparent' */
    public static final WidgetPropertyDescriptor<Boolean> displayTransparent =
        new WidgetPropertyDescriptor<Boolean>(
            WidgetPropertyCategory.DISPLAY, "transparent", Messages.WidgetProperties_Transparent)
    {
        @Override
        public WidgetProperty<Boolean> createProperty(final Widget widget,
                                                      final Boolean transparent)
        {
            return new BooleanWidgetProperty(this, widget, transparent);
        }
    };

    /** Display 'text': Text to display */
    public static final WidgetPropertyDescriptor<String> displayText =
        new WidgetPropertyDescriptor<String>(
            WidgetPropertyCategory.DISPLAY, "text", Messages.WidgetProperties_Text)
    {
        @Override
        public WidgetProperty<String> createProperty(final Widget widget,
                                                     final String text)
        {
            return new StringWidgetProperty(this, widget, text);
        }
    };

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
        new WidgetPropertyDescriptor<String>(
            WidgetPropertyCategory.DISPLAY, "file", Messages.WidgetProperties_File)
    {
        @Override
        public WidgetProperty<String> createProperty(final Widget widget,
                                                     final String text)
        {
            return new StringWidgetProperty(this, widget, text);
        }
    };


    /** Behavior 'pv_name':Primary PV Name */
    public static final WidgetPropertyDescriptor<String> behaviorPVName =
        new WidgetPropertyDescriptor<String>(
            WidgetPropertyCategory.BEHAVIOR, "pv_name", Messages.WidgetProperties_PVName)
    {
        @Override
        public WidgetProperty<String> createProperty(final Widget widget,
                                                     final String pv_name)
        {
            return new StringWidgetProperty(this, widget, pv_name);
        }
    };

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
            new WidgetPropertyDescriptor<Boolean>(
                WidgetPropertyCategory.BEHAVIOR, "limits_from_pv", Messages.WidgetProperties_LimitsFromPV)
        {
            @Override
            public WidgetProperty<Boolean> createProperty(final Widget widget,
                                                          final Boolean limits_from_pv)
            {
                return new BooleanWidgetProperty(this, widget, limits_from_pv);
            }
        };

    /** Behavior 'minimum': Minimum display range */
    public static final WidgetPropertyDescriptor<Double> behaviorMinimum =
            new WidgetPropertyDescriptor<Double>(
                WidgetPropertyCategory.BEHAVIOR, "minimum", Messages.WidgetProperties_Minimum)
        {
            @Override
            public WidgetProperty<Double> createProperty(final Widget widget,
                                                         final Double min)
            {
                return new DoubleWidgetProperty(this, widget, min);
            }
        };

    /** Behavior 'maximum': Maximum display range */
    public static final WidgetPropertyDescriptor<Double> behaviorMaximum =
        new WidgetPropertyDescriptor<Double>(
            WidgetPropertyCategory.BEHAVIOR, "maximum", Messages.WidgetProperties_Maximum)
    {
        @Override
        public WidgetProperty<Double> createProperty(final Widget widget,
                                                      final Double max)
        {
            return new DoubleWidgetProperty(this, widget, max);
        }
    };

    /** Constructor for value property
     *  @param name Internal name of the property
     *  @param description Human-readable description
     */
    public static final WidgetPropertyDescriptor<VType> newRuntimeValue(final String name, final String description)
    {
        return new WidgetPropertyDescriptor<VType>(WidgetPropertyCategory.RUNTIME, name, description)
        {
            @Override
            public WidgetProperty<VType> createProperty(final Widget widget,
                                                        final VType value)
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
