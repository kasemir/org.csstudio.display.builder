/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.script;

import static org.csstudio.display.builder.rcp.Plugin.logger;

import java.util.Arrays;
import java.util.logging.Level;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

/** RCP-related Script Utilities
 *  @author Kay Kasemir
 *  @author Xihui Chen, Will Rogers - Original code in org.csstudio.opibuilder.scriptUtil.ScriptUtil
 */
@SuppressWarnings("nls")
public class RCPUtil
{
    // Note: To allow scripts (jython) to call this, two entries are required in MANIFEST.MF
    //       1) Eclipse-RegisterBuddy: org.python.jython
    //       2) Some dependencu on org.python.jython

    /** Execute an Eclipse command with optional parameters.
     *
     *  <pre>executeEclipseCommand("id", ["pkey", "pvalue", "pkey", "pvalue", ...])</pre>
     *
     *  @param commandId Eclipse command id
     *  @param parameters String arguments alternating key, value:
     */
    public final static void executeEclipseCommand(String commandId, String... parameters)
    {
        // Must execute on UI thread
        Display.getDefault().asyncExec(() -> doExecuteEclipseCommand(commandId, parameters));
    }

    private final static void doExecuteEclipseCommand(String commandId, String... parameters)
    {
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        final IHandlerService handlerService = window.getService(IHandlerService.class);
        try
        {
            if (parameters.length % 2 != 0)
                throw new IllegalArgumentException("Parameterized commands must have "
                        + "an equal number of keys and values");

            if (parameters.length == 0)
                handlerService.executeCommand(commandId, null);
            else
            {
                final ICommandService commandService = window.getService(ICommandService.class);
                final Parameterization[] params = new Parameterization[parameters.length / 2];
                final Command c = commandService.getCommand(commandId);
                for (int i = 0; i < parameters.length / 2; i++)
                {
                    final String key = parameters[2 * i];
                    final String value = parameters[2 * i + 1];
                    final IParameter p = c.getParameter(key);
                    final Parameterization pp = new Parameterization(p, value);
                    params[i] = pp;
                }
                final ParameterizedCommand pc = new ParameterizedCommand(c, params);
                handlerService.executeCommand(pc, null);
            }
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Failed to execute eclipse command '" + commandId + "' " + Arrays.toString(parameters), ex);
        }
    }
}
