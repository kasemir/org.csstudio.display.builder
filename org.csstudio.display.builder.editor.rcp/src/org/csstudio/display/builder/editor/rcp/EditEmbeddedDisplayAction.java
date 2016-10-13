/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import static org.csstudio.display.builder.editor.rcp.Plugin.logger;

import java.util.logging.Level;

import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Action that opens the body of an embedded display widget in the editor
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EditEmbeddedDisplayAction extends Action
{
    private String file;

    public EditEmbeddedDisplayAction(final EmbeddedDisplayWidget widget)
    {
        super(Messages.EditEmbededDisplay,
              AbstractUIPlugin.imageDescriptorFromPlugin("org.csstudio.display.builder.model", "icons/embedded.png"));
        try
        {
            file = ModelResourceUtil.resolveResource(widget.getDisplayModel(), widget.propFile().getValue());
        }
        catch (Exception ex)
        {
            file = widget.propFile().getValue();
        }
    }

    @Override
    public void run()
    {
        try
        {
            OpenDisplayInEditor.open(file);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot open editor for " + file, ex);
        }
    }
}
