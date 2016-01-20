/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import org.csstudio.display.builder.editor.rcp.DisplayEditorPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/** Action to execute something on the current editor
 *  @author Kay Kasemir
 */
public class EditorAction extends Action
{
    protected DisplayEditorPart edit_part;

    public EditorAction(final String name, final ImageDescriptor icon)
    {
        super(name, icon);
    }

    public void setActiveEditor(final DisplayEditorPart edit_part)
    {
        this.edit_part = edit_part;
    }
}
