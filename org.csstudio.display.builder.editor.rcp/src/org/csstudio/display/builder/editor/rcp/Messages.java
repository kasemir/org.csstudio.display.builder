/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import org.eclipse.osgi.util.NLS;

/** Externalized texts
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.csstudio.display.builder.editor.rcp.messages";

    // Keep in alphabetical order!
    public static String AccessDeniedMessage;
    public static String CannotLoadDisplayTitle;
    public static String Copy;
    public static String Cut;
    public static String DownloadPromptFMT;
    public static String DownloadTitle;
    public static String EditEmbededDisplay;
    public static String ExecuteDisplay;
    public static String FileNotFoundMessage;
    public static String FindWidget;
    public static String NewDisplay_Browse;
    public static String NewDisplay_BrowseTitle;
    public static String NewDisplay_Container;
    public static String NewDisplay_ContainerNotFound;
    public static String NewDisplay_Description;
    public static String NewDisplay_Error;
    public static String NewDisplay_ExtensionError;
    public static String NewDisplay_Filename;
    public static String NewDisplay_InitialName;
    public static String NewDisplay_InvalidFileName;
    public static String NewDisplay_MissingContainer;
    public static String NewDisplay_MissingExtensionError;
    public static String NewDisplay_MissingFileName;
    public static String NewDisplay_NotWriteable;
    public static String NewDisplay_Title;
    public static String OpenEditorPerspective;
    public static String Paste;
    public static String Redo;
    public static String ReplaceWith;
    public static String ReplaceWith_NoWidgets;
    public static String Undo;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
