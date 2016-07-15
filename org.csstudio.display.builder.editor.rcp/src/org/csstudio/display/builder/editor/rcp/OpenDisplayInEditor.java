/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import static org.csstudio.display.builder.editor.rcp.Plugin.logger;

import java.util.logging.Level;

import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.run.RuntimeViewPart;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/** Open display of currently active view in editor
 *
 *  <p>plugin.xml adds this to a context menu defined
 *  by ContextMenuSupport in org.csstudio.display.builder.rcp plugin.
 *
 *  @author Kay Kasemir
 */
public class OpenDisplayInEditor extends AbstractHandler implements IHandler
{
    @SuppressWarnings("nls")
    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException
    {
        // TODO Auto-generated method stub

        final IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
        if (part instanceof RuntimeViewPart)
        {
            final RuntimeViewPart view = (RuntimeViewPart) part;
            final DisplayInfo info = view.getDisplayInfo();
            System.out.println("Need to edit " + info);
        }
        else
        {
            logger.log(Level.WARNING, "Cannot locate active display, got " + part);
        }
        return null;
    }

}
