/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.top;

import static org.csstudio.display.builder.rcp.Plugin.logger;

import java.util.List;
import java.util.logging.Level;

import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.DisplayInfoXMLUtil;
import org.csstudio.display.builder.rcp.OpenDisplayAction;
import org.csstudio.display.builder.rcp.Preferences;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

/** Create drop-down items for the "Top Displays" toolbar button
 *
 *  <p>Implementation of drop-down buttons in tool bar
 *  is a bit circuitous:
 *  <ol>
 *  <li>plugin.xml declares menuContribution for toolbar:org.eclipse.ui.main.toolbar,
 *      adding a toolbar with an "ToolbarTopDisplays" command of style "pulldown"
 *  <li>plugin.xml declares a "ToolbarTopDisplays" command.
 *      That command uses a 'visibleWhen' expression to only show
 *      when there are top displays defined.
 *      The handler class, {@link ToolbarTopDisplaysHandler}, could be a NOP.
 *      In this case, it offers another way to open the drop-down.
 *  <li>plugin.xml declares another menuContribution for menu:ToolbarTopDisplays,
 *      i.e. using the ID of the command.
 *      This menu is what's actually shown in the tool bar drop-down.
 *      Ideally, it could use the TopDisplaysMenu, but that would result in
 *      a nested menu: Tool bar drop down -> another "Top Displays" menu button -> items.
 *      To get the items right away via Tool bar drop down -> items,
 *      the menuContribution uses this class to provide the contributions.
 *  </ol>
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TopDisplaysToolbarItems extends ExtensionContributionFactory
{
    @Override
    public void createContributionItems(final IServiceLocator serviceLocator,
                                        final IContributionRoot additions)
    {   // See http://blog.vogella.com/2009/12/03/commands-menu-runtime
        // for ExtensionContributionFactory example
        try
        {
            final String setting = Preferences.getTopDisplays();
            final List<DisplayInfo> displays = DisplayInfoXMLUtil.fromDisplaysXML(setting);
            for (DisplayInfo display : displays)
            {
                final IAction action = new OpenDisplayAction(display);
                final IContributionItem item = new ActionContributionItem(action);
                additions.addContributionItem(item, null);
            }
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot create 'top displays'", ex);
        }
    }
}
