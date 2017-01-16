/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import org.csstudio.display.builder.model.macros.MacroXMLUtil;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.openfile.IOpenDisplayAction;

/** Handler for opening files from command line
 *
 *  <p>See
 *  Opening Files from Command-Line - org.csstudio.openfile
 *  in CS-Studio docbook.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class OpenDisplayFile implements IOpenDisplayAction
{
    @Override
    public void openDisplay(final String path, final String data) throws Exception
    {
        final Macros macros = MacroXMLUtil.readMacros(data);
        final DisplayInfo info = new DisplayInfo(path, "Display from command line", macros);
        new OpenDisplayAction(info).run();
    }
}
