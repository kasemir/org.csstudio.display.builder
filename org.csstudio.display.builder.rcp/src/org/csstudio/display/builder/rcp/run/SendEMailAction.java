/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import org.csstudio.email.ui.AbstractSendEMailAction;
import org.csstudio.javafx.Screenshot;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.swt.widgets.Shell;

import javafx.scene.Scene;

/** Action for e-mailing snapshot of display
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class SendEMailAction extends AbstractSendEMailAction
{
    private final Scene scene;

    public SendEMailAction(final Shell shell, final Scene scene)
    {
        super(shell, "", "Screenshot");
        this.scene = scene;
    }

    @Override
    protected String getImage()
    {
        try
        {
            final Screenshot screenshot = new Screenshot(scene, "display");
            return screenshot.getFilename();
        }
        catch (Exception ex)
        {
            ExceptionDetailsErrorDialog.openError(shell, "Cannot obtain screenshot", ex);
            return null;
        }
    }
}
