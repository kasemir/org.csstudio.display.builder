/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/** Action to open 'previous' display in back/forward navigation
 *  @author Kay Kasemir
 */
public class NavigateBackAction extends Action
{
	public NavigateBackAction()
	{
        final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        setText("&Back");
        setToolTipText("Open previous display");
        setId(ActionFactory.BACKWARD_HISTORY.getId());
        setImageDescriptor(sharedImages
        		.getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
        setDisabledImageDescriptor(sharedImages
        		.getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));
        setActionDefinitionId("org.eclipse.ui.navigate.backwardHistory");
	}
}
