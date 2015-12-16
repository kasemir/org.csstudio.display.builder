/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import org.eclipse.osgi.util.NLS;

/** Externalized Strings
 *  @author Kay Kasemir
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.csstudio.display.builder.representation.javafx.messages"; //$NON-NLS-1$

    // Keep in alphabetical order, synchronized with messages.properties
    public static String ActionsDialog_Actions;
    public static String ActionsDialog_Description;
    public static String ActionsDialog_Detail;
    public static String ActionsDialog_Info;
    public static String ActionsDialog_Path;
    public static String ActionsDialog_PVName;
    public static String ActionsDialog_Title;
    public static String ActionsDialog_Value;
    public static String Add;
    public static String Blue;
    public static String ColorDialog_Custom;
    public static String ColorDialog_Info;
    public static String ColorDialog_Predefined;
    public static String ColorDialog_Title;
    public static String CopyPVName;
    public static String FontDialog_ExampleText;
    public static String FontDialog_Family;
    public static String FontDialog_Info;
    public static String FontDialog_Predefined;
    public static String FontDialog_Preview;
    public static String FontDialog_Size;
    public static String FontDialog_Style;
    public static String FontDialog_Title;
    public static String Green;
    public static String MacrosDialog_Info;
    public static String MacrosDialog_NameCol;
    public static String MacrosDialog_Title;
    public static String MacrosDialog_ValueCol;
    public static String MacrosTable_NameHint;
    public static String MacrosTable_ToolTip;
    public static String MacrosTable_ValueHint;
    public static String MoveDown;
    public static String MoveUp;
    public static String Red;
    public static String Remove;
    public static String ScriptsDialog_BtnEmbed;
    public static String ScriptsDialog_BtnFile;
    public static String ScriptsDialog_ColPV;
    public static String ScriptsDialog_ColScript;
    public static String ScriptsDialog_ColTrigger;
    public static String ScriptsDialog_DefaultEmbeddedScript;
    public static String ScriptsDialog_FileBrowser_Title;
    public static String ScriptsDialog_FileType_All;
    public static String ScriptsDialog_FileType_Script;
    public static String ScriptsDialog_Info;
    public static String ScriptsDialog_PVsTT;
    public static String ScriptsDialog_ScriptsTT;
    public static String ScriptsDialog_SelectScript;
    public static String ScriptsDialog_Title;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
