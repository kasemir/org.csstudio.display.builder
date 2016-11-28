/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo;
import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.Messages;
import org.csstudio.display.builder.rcp.Plugin;
import org.csstudio.display.builder.rcp.RuntimeViewPart;
import org.csstudio.display.builder.runtime.ActionUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Action to run display in standalone Stage
 *
 *  <p>Executes display in JFX Stage without menu bar,
 *  toolbar, perspectives, context menu etc.
 *
 *  <p>Action is not reversible.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class StandaloneAction extends Action
{
    private final RuntimeViewPart view;

    public StandaloneAction(final RuntimeViewPart view)
    {
        super(Messages.OpenStandalone,
              AbstractUIPlugin.imageDescriptorFromPlugin(Plugin.ID, "icons/standalone.png"));
        this.view = view;
    }

    @Override
    public void run()
    {
        final DisplayModel parent_model = view.getDisplayModel();
        final DisplayInfo info = view.getDisplayInfo();
        ActionUtil.handleAction(parent_model,
                                new OpenDisplayActionInfo(Messages.OpenStandalone, info.getPath(),
                                                          info.getMacros(), OpenDisplayActionInfo.Target.STANDALONE));
    }
}
