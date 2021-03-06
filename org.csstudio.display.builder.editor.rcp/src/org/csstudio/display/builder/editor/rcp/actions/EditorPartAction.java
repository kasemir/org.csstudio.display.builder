/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.actions.ActionDescription;
import org.csstudio.display.builder.editor.rcp.DisplayEditorPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Action to execute something on the current editor
 *  @author Kay Kasemir
 */
public class EditorPartAction extends Action
{
    public static EditorPartAction forActionDescription(final ActionDescription action)
    {
        final ImageDescriptor icon =
                AbstractUIPlugin.imageDescriptorFromPlugin(org.csstudio.display.builder.editor.Plugin.ID,
                                                           action.getIcon());
        return new EditorPartAction(action.getToolTip(), icon)
        {
            @Override
            public void run()
            {
                final DisplayEditor editor = edit_part.getDisplayEditor();
                action.run(editor);
            }
        };
    }

    public static EditorPartAction forToggledActionDescription(final ActionDescription action)
    {
        final ImageDescriptor icon =
                AbstractUIPlugin.imageDescriptorFromPlugin(org.csstudio.display.builder.editor.Plugin.ID,
                                                           action.getIcon());
        final EditorPartAction epa = new EditorPartAction(action.getToolTip(), icon, Action.AS_CHECK_BOX)
        {
            @Override
            public void run()
            {
                final DisplayEditor editor = edit_part.getDisplayEditor();
                action.run(editor, isChecked());
            }
        };
        epa.setChecked(true);
        return epa;
    }


    protected DisplayEditorPart edit_part;

    public EditorPartAction(final String name, final ImageDescriptor icon)
    {
        super(name, icon);
    }

    public EditorPartAction(final String name, final ImageDescriptor icon, final int style)
    {
        super(name, style);
        setImageDescriptor(icon);
    }

    public void setActiveEditor(final DisplayEditorPart edit_part)
    {
        this.edit_part = edit_part;
    }
}
