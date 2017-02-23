/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
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
import org.csstudio.display.builder.rcp.Messages;
import org.csstudio.display.builder.rcp.OpenDisplayAction;
import org.csstudio.display.builder.rcp.Preferences;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.actions.CompoundContributionItem;

/** Menu item for opening "top" displays
 *
 *  <p>Will automatically disappear if there are no top displays,
 *  i.e. the menu would be empty.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TopDisplaysMenu extends CompoundContributionItem
{
    @Override
    protected IContributionItem[] getContributionItems()
    {
        final IMenuManager items = new MenuManager(Messages.TopDisplays, OpenDisplayAction.getIcon(), null);
        try
        {
            final String setting = Preferences.getTopDisplays();
            final List<DisplayInfo> displays = DisplayInfoXMLUtil.fromDisplaysXML(setting);
            for (DisplayInfo display : displays)
                items.add(new OpenDisplayAction(display));
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot create 'top displays'", ex);
        }
        return new IContributionItem[] { items };
    }
}
