/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.util.List;

import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.DisplayNavigation;
import org.csstudio.display.builder.rcp.Plugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/** Action to open 'previous' display in back/forward navigation
 *  @author Kay Kasemir
 */
public class NavigateBackAction extends Action implements DisplayNavigation.Listener, IMenuCreator
{
	private final RuntimeViewPart part;
	private final DisplayNavigation navigation;
	private Menu menu = null;

	public NavigateBackAction(final RuntimeViewPart part, final DisplayNavigation navigation)
	{
		this.part = part;
		this.navigation = navigation;
        final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        setText("&Back");
        setToolTipText("Open previous display");
        setId(ActionFactory.BACKWARD_HISTORY.getId());
        setImageDescriptor(sharedImages
        		.getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
        setDisabledImageDescriptor(sharedImages
        		.getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));
        setActionDefinitionId("org.eclipse.ui.navigate.backwardHistory");
        navigation.addListener(this);
        displayHistoryChanged(navigation);
                
		setMenuCreator(this);
	}

	@Override
	public void displayHistoryChanged(final DisplayNavigation navigation)
	{
		setEnabled(navigation.getBackwardDisplays().size() > 0);
	}

	// IMenuCreator
	@Override
	public Menu getMenu(final Control parent)
	{
		if (menu != null)
			menu.dispose();
		
		menu = new Menu(parent);
		final List<DisplayInfo> displays = navigation.getBackwardDisplays();
		for (int i=0; i<displays.size(); ++i)
			addAction(displays.get(i), i+1);
		
		return menu;
	}

	private void addAction(final DisplayInfo display, final int steps)
	{
		final Action action = new Action(display.getName())
		{
			@Override
			public void run()
			{
				final DisplayInfo display = navigation.goBackward(steps);
				part.loadDisplayFile(display);
			}
		};
		action.setImageDescriptor(Plugin.getIcon("display.png"));
		final ActionContributionItem item = new ActionContributionItem(action);
		item.fill(menu, -1);
	}

	// IMenuCreator
	@Override
	public Menu getMenu(final Menu parent)
	{
		// Not used
		return null;
	}

	// IMenuCreator
	@Override
	public void dispose()
	{
		if (menu != null)
			menu.dispose();
		menu = null;
		navigation.removeListener(this);
	}
}
