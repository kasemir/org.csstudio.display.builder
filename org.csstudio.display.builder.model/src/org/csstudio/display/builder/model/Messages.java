/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
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

    public static String ActionButtonWidget_Description;
    public static String ActionButtonWidget_Name;
    public static String Actions_Zero;
    public static String Actions_N_Fmt;
    public static String EmbeddedDisplayWidget_Description;
    public static String EmbeddedDisplayWidget_Name;
    public static String FontStyle_Bold;
    public static String FontStyle_BoldItalic;
    public static String FontStyle_Italic;
    public static String FontStyle_Regular;
    public static String GroupWidget_Description;
    public static String GroupWidget_Name;
    public static String LabelWidget_Description;
    public static String LabelWidget_Name;
    public static String LabelWidget_Text;
    public static String LEDWidget_Description;
    public static String LEDWidget_Name;
    public static String LEDWidget_OffColor;
    public static String LEDWidget_OnColor;
    public static String ProgressBar_Description;
    public static String ProgressBar_Name;
    public static String RectangleWidget_Description;
    public static String RectangleWidget_Name;
    public static String TextEntryWidget_Name;
    public static String TextEntryWidget_Description;
    public static String TextUpdateWidget_Description;
    public static String TextUpdateWidget_Name;
    public static String WidgetCategory_Controls;
    public static String WidgetCategory_Graphics;
    public static String WidgetCategory_Miscellaneous;
    public static String WidgetCategory_Monitors;
    public static String WidgetCategory_Plots;
    public static String WidgetCategory_Structure;
    public static String WidgetProperties_Actions;
    public static String WidgetProperties_BackgroundColor;
    public static String WidgetProperties_File;
    public static String WidgetProperties_Font;
    public static String WidgetProperties_ForegroundColor;
    public static String WidgetProperties_Height;
    public static String WidgetProperties_Insets;
    public static String WidgetProperties_Macros;
    public static String WidgetProperties_Name;
    public static String WidgetProperties_PVName;
    public static String WidgetProperties_Scripts;
    public static String WidgetProperties_Text;
    public static String WidgetProperties_Type;
    public static String WidgetProperties_Value;
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

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
