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
    public static String Blue;
    public static String ColorDialog_Custom;
    public static String ColorDialog_Info;
    public static String ColorDialog_Predefined;
    public static String ColorDialog_Title;
    public static String FontDialog_ExampleText;
    public static String FontDialog_Family;
    public static String FontDialog_Info;
    public static String FontDialog_Predefined;
    public static String FontDialog_Preview;
    public static String FontDialog_Size;
    public static String FontDialog_Style;
    public static String FontDialog_Title;
    public static String Green;
    public static String Red;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
