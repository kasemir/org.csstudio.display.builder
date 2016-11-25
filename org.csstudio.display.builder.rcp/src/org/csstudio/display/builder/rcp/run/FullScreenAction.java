/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import org.csstudio.display.builder.rcp.Messages;
import org.csstudio.display.builder.rcp.Plugin;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Action to enter/exit 'full screen' mode
 *
 *  <p>Makes current window as much 'full screen' as possible
 *  while still staying within RCP workbench.
 *  Menu, perspective selector, tool- and status bars are hidden,
 *  part layouts otherwise remain, which includes the CTabFolder
 *  and thus the 'tabs' on each part.
 *
 *  <p>Runtime views keep their context menu via which
 *  this action can be reversed.
 *
 *  <p>Uses internal API (WorkbenchWindow) and hacks (setStatusBarVisibile)
 *  to accomplish its deed.
 *  Based on original CompactModeAction and FullScreenAction explorations
 *  in opibuilder by Xihui Chen.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings({"nls","restriction"})
public class FullScreenAction extends Action
{
    private final IWorkbenchPage page;

    public FullScreenAction(IWorkbenchPage page)
    {
        if (page.getWorkbenchWindow().getShell().getFullScreen())
        {
            setText(Messages.ExitFullscreen);
            setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Plugin.ID, "icons/exitfullscreen.png"));
        }
        else
        {
            setText(Messages.EnterFullscreen);
            setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Plugin.ID, "icons/fullscreen.png"));
        }
        this.page = page;
    }

    @Override
    public void run()
    {
        final Shell shell = page.getWorkbenchWindow().getShell();
        final boolean fullscreen = ! shell.getFullScreen();

        final WorkbenchWindow window = (WorkbenchWindow) page.getWorkbenchWindow();
        shell.setFullScreen(fullscreen);
        window.setCoolBarVisible(! fullscreen);
        window.setPerspectiveBarVisible(! fullscreen);
        setStatusBarVisibile(shell, ! fullscreen);
        // TODO Show/hide Menu bar for Linux and Windows?
    }

    private void setStatusBarVisibile(final Shell shell, final boolean visible)
    {
        // A hack to set status line visibility because none of these work:
        // window.setStatusLineVisible(false);
        // window.getActionBars().getStatusLineManager().getItems()[0].setVisible(visible);
        // window.getStatusLineManager().getItems()[0].setVisible(visible);
        // window.getStatusLineManager().getControl().setVisible(visible);
        for (Control child : shell.getChildren())
            if (child.getClass().equals(Composite.class)  &&  ! child.isDisposed())
                for (Control c : ((Composite)child).getChildren())
                    if (c.getClass().getSimpleName().contains("StatusLine")) //$NON-NLS-1$
                    {
                        child.setVisible(visible);
                        shell.layout();
                        return;
                    }
    }
}
