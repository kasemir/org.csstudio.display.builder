/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;

/** Hacks that remove RCP UI element
 *
 *  <p>When the display builder is running within an RCP
 *  product that also contains BOY, git, ...,
 *  these hacks can hide those UI elements to present
 *  the display builder runtime with a minimum of
 *  unrelated UI elements.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RCPHacks
{
    /** IDs of contributions to remove from toolbar */
    private static final List<String> remove_from_toolbar = Arrays.asList(
        // "New"
        "file",
        // Log in/out
        "org.csstudio.security.toolbar",
        // Make logbook entry
        "org.csstudio.logbook.ui.toolbar",
        "org.eclipse.ui.edit.text.actionSet.presentation"
       );

    private static boolean patched_menu = false;

    /** Remove toolbar and menu entries unrelated to the display builder
     *  @param page {@link IWorkbenchPage}
     */
    public static void hideUnrelatedUI(final IWorkbenchPage page)
    {
        // Hide BOY "Top Files" tool bar drop-down
        page.hideActionSet("org.csstudio.opibuilder.actionSet");
        // Hide "Search" (git, file) from tool bar
        page.hideActionSet("org.eclipse.search.searchActionSet");

        if (! (page.getWorkbenchWindow() instanceof WorkbenchWindow))
            return;
        final WorkbenchWindow window = (WorkbenchWindow)page.getWorkbenchWindow();

        final ICoolBarManager toolbar = window.getCoolBarManager2();
        for (IContributionItem item : toolbar.getItems())
        {
            // System.out.println(item.getId());
            if (remove_from_toolbar.contains(item.getId()))
                toolbar.remove(item);
        }
        toolbar.update(true);

        patchMenu(window);
    }

    /** Remove unrelated menu entries (once)
     *  @param window
     */
    private static void patchMenu(final WorkbenchWindow window)
    {
        if (patched_menu)
            return;
        patched_menu = true;
        final MenuManager manager = window.getMenuManager();
        // File/Top Files needs to be removed because it would
        // allow user to open BOY displays.
        MenuManager file = null;
        for (IContributionItem item : manager.getItems())
            if (item instanceof MenuManager  &&  item.getId().equals("file"))
            {
                file = (MenuManager) item;
                break;
            }
        if (file == null)
            return;
        for (IContributionItem item : file.getItems())
            if (item instanceof MenuManager  &&  ((MenuManager) item).getMenuText().equals("Top Files"))
            {
                file.remove(item);
                break;
            }
        manager.update(true);
    }
}
