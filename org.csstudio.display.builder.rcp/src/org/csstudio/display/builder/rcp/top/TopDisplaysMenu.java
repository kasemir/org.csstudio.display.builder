/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.top;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.rcp.Messages;
import org.csstudio.display.builder.rcp.Plugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.actions.CompoundContributionItem;

/** Menu item for opening "top" displays
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TopDisplaysMenu extends CompoundContributionItem
{
    private static List<IAction> createDisplayActions() throws Exception
    {
        final String setting = Plugin.getPreference("top_displays", "");
        final List<DisplayInfo> displays = DisplayInfoParser.parse(setting);
        final List<IAction> actions = new ArrayList<>(displays.size());
        displays.forEach(info -> actions.add(new OpenDisplayAction(info)));
        return actions;
    }

    @Override
    protected IContributionItem[] getContributionItems()
    {
        final IMenuManager items = new MenuManager(Messages.TopDisplays, OpenDisplayAction.getIcon(), null);
        try
        {
            for (IAction action : createDisplayActions())
                items.add(action);
        }
        catch (Exception ex)
        {
            Logger.getLogger(getClass().getName())
                  .log(Level.WARNING, "Cannot create 'top displays'", ex);
        }

        return new IContributionItem[] { items };
    }
}
