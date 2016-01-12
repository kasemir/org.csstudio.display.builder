/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.display.builder.editor.undo;

import java.util.List;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.properties.ScriptInfo;
import org.csstudio.display.builder.model.properties.ScriptsWidgetProperty;
import org.csstudio.display.builder.util.undo.UndoableAction;

/** Action to update widget scripts
 *  @author Kay Kasemir
 */
public class SetWidgetScriptsAction extends UndoableAction
{
    private final ScriptsWidgetProperty property;
    private final List<ScriptInfo> orig_scripts;
    private final List<ScriptInfo> scripts;

    public SetWidgetScriptsAction(final ScriptsWidgetProperty property,
                                  final List<ScriptInfo> scripts)
    {
        super(Messages.SetWidgetActions);
        this.property = property;
        this.orig_scripts = property.getValue();
        this.scripts = scripts;
    }

    @Override
    public void run()
    {
        property.setValue(scripts);
    }

    @Override
    public void undo()
    {
        property.setValue(orig_scripts);
    }
}
