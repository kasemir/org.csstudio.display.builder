/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.util.UUID;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/** Represent display builder in JFX inside RCP Views
 *
 *  @author Kay Kasemir
 */
public class RCP_JFXRepresentation // TODO extends JFXRepresentation? extends ToolkitRepresentation<Group, Node>
{
    // TODO Similar to JFXRepresentation, but using RuntimeViewPart as 'Window'

    public void openNewWindow()
    {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try
        {
            final RuntimeViewPart rt_view = (RuntimeViewPart)
                    page.showView(RuntimeViewPart.ID, UUID.randomUUID().toString(), IWorkbenchPage.VIEW_ACTIVATE);

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
