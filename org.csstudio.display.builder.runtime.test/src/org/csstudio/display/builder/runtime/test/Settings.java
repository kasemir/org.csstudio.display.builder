/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.test;

import java.io.FileInputStream;
import java.util.logging.LogManager;

import org.csstudio.display.builder.model.persist.WidgetColorService;

/** Demo settings
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Settings
{
    public static final String example_name =
//        "example.opi";
//        "legacy.opi";
//        "legacy_embed.opi";
        "https://webopi.sns.gov/webopi/opi/Instruments.opi";
//          "main.opi";

    public static void setup() throws Exception
    {
        LogManager.getLogManager().readConfiguration(new FileInputStream("examples/logging.properties"));

        final String addr_list = "127.0.0.1 webopi.sns.gov:5066 160.91.228.17";
        System.setProperty("com.cosylab.epics.caj.CAJContext.addr_list", addr_list);
        System.setProperty("gov.aps.jca.jni.JNIContext.addr_list", addr_list);

        final String color_url = "https://webopi.sns.gov/share/opi/color.def";
        WidgetColorService.loadColors(color_url);
    }
}