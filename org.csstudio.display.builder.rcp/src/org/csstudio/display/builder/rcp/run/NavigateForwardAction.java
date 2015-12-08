/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import org.csstudio.display.builder.rcp.DisplayNavigation;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/** Action to open 'next' display in back/forward navigation
 *  @author Kay Kasemir
 */
public class NavigateForwardAction extends Action implements DisplayNavigation.Listener
{
	public NavigateForwardAction(final RuntimeViewPart part, final DisplayNavigation navigation)
	{
		final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        setText("&Forward");
        setToolTipText("Open next display");
        setId(ActionFactory.FORWARD_HISTORY.getId());
        setImageDescriptor(sharedImages
                .getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
        setDisabledImageDescriptor(sharedImages
                .getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD_DISABLED));
        setActionDefinitionId("org.eclipse.ui.navigate.forwardHistory");
        navigation.addListener(this);
        displayHistoryChanged(navigation);
	}

	@Override
	public void displayHistoryChanged(final DisplayNavigation navigation)
	{
		setEnabled(navigation.getForwardDisplays().size() > 0);
	}
}
