/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import org.eclipse.osgi.util.NLS;

/** Externalized Strings
 *  @author Kay Kasemir
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.csstudio.display.builder.model.messages"; //$NON-NLS-1$

    // Keep in alphabetical order, matching the order in messages.properties
    public static String Actions_Zero;
    public static String Actions_N_Fmt;
    public static String BoolWidget_OffLabel;
    public static String BoolWidget_OnLabel;
    public static String ActiveTab;
    public static String Bottom;
    public static String Checkbox_Label;
    public static String Center;
    public static String FontStyle_Bold;
    public static String FontStyle_BoldItalic;
    public static String FontStyle_Italic;
    public static String FontStyle_Regular;
    public static String Format_Compact;
    public static String Format_Decimal;
    public static String Format_Default;
    public static String Format_Engineering;
    public static String Format_Exponential;
    public static String Format_Hexadecimal;
    public static String Format_String;
    public static String GroupWidget_Description;
    public static String GroupWidget_Name;
    public static String Horizontal;
    public static String LabelWidget_Text;
    public static String Left;
    public static String Middle;
    public static String PlotWidget_AutoScale;
    public static String PlotWidget_Color;
    public static String PlotWidget_LogScale;
    public static String PlotWidget_PointSize;
    public static String PlotWidget_PointType;
    public static String PlotWidget_ScaleFont;
    public static String PlotWidget_ShowLegend;
    public static String PlotWidget_Title;
    public static String PlotWidget_TitleFont;
    public static String PlotWidget_Trace;
    public static String PlotWidget_Traces;
    public static String PlotWidget_X;
    public static String PlotWidget_XAxis;
    public static String PlotWidget_XPV;
    public static String PlotWidget_Y;
    public static String PlotWidget_YAxes;
    public static String PlotWidget_YAxis;
    public static String PlotWidget_YPV;
    public static String PointType_Circles;
    public static String PointType_Diamonds;
    public static String PointType_None;
    public static String PointType_Squares;
    public static String PointType_Triangles;
    public static String PointType_X;
    public static String Resize_Container;
    public static String Resize_Content;
    public static String Resize_None;
    public static String Right;
    public static String Style;
    public static String Style_Group;
    public static String Style_Line;
    public static String Style_None;
    public static String Style_Title;
    public static String Tab_Height;
    public static String Tab_Item;
    public static String TabsWidget_Description;
    public static String TabsWidget_Name;
    public static String TabsWidget_TabNameFmt;
    public static String Target_Replace;
    public static String Target_Tab;
    public static String Target_Window;
    public static String Top;
    public static String Vertical;
    public static String WidgetCategory_Controls;
    public static String WidgetCategory_Graphics;
    public static String WidgetCategory_Miscellaneous;
    public static String WidgetCategory_Monitors;
    public static String WidgetCategory_Plots;
    public static String WidgetCategory_Structure;
    public static String WidgetProperties_Actions;
    public static String WidgetProperties_AngleSize;
    public static String WidgetProperties_AngleStart;
    public static String WidgetProperties_BackgroundColor;
    public static String WidgetProperties_Bit;
    public static String WidgetProperties_BorderAlarmSensitive;
    public static String WidgetProperties_ColorMap;
    public static String WidgetProperties_Connected;
    public static String WidgetProperties_CornerHeight;
    public static String WidgetProperties_CornerWidth;
    public static String WidgetProperties_DataHeight;
    public static String WidgetProperties_DataWidth;
    public static String WidgetProperties_Direction;
    public static String WidgetProperties_File;
    public static String WidgetProperties_FillColor;
    public static String WidgetProperties_Font;
    public static String WidgetProperties_ForegroundColor;
    public static String WidgetProperties_Format;
    public static String WidgetProperties_Height;
    public static String WidgetProperties_HorizontalAlignment;
    public static String WidgetProperties_Insets;
    public static String WidgetProperties_LimitsFromPV;
    public static String WidgetProperties_LineColor;
    public static String WidgetProperties_LineWidth;
    public static String WidgetProperties_Macros;
    public static String WidgetProperties_Maximum;
    public static String WidgetProperties_Minimum;
    public static String WidgetProperties_Name;
    public static String WidgetProperties_OffColor;
    public static String WidgetProperties_OnColor;
    public static String WidgetProperties_Points;
    public static String WidgetProperties_Precision;
    public static String WidgetProperties_PVName;
    public static String WidgetProperties_ResizeBehavior;
    public static String WidgetProperties_Rotation;
    public static String WidgetProperties_Rules;
    public static String WidgetProperties_ScaleFactor;
    public static String WidgetProperties_Scripts;
    public static String WidgetProperties_ShowUnits;
    public static String WidgetProperties_StretchToFit;
    public static String WidgetProperties_Text;
    public static String WidgetProperties_Transparent;
    public static String WidgetProperties_Type;
    public static String WidgetProperties_Value;
    public static String WidgetProperties_VerticalAlignment;
    public static String WidgetProperties_Visible;
    public static String WidgetProperties_Width;
    public static String WidgetProperties_X;
    public static String WidgetProperties_Y;
    public static String WidgetPropertyCategory_Behavior;
    public static String WidgetPropertyCategory_Display;
    public static String WidgetPropertyCategory_Misc;
    public static String WidgetPropertyCategory_Position;
    public static String WidgetPropertyCategory_Runtime;
    public static String WidgetPropertyCategory_Widget;
    public static String XYPlot_Description;
    public static String XYPlot_Name;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
