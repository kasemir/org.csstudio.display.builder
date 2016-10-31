/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp.actions;

import java.util.List;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.editor.undo.GroupWidgetsAction;
import org.csstudio.display.builder.editor.util.GeometryTools;
import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.ModelPlugin;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import javafx.geometry.Rectangle2D;

/** SWT Action to create group
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class CreateGroupAction extends Action
{
    private final DisplayEditor editor;
    private final List<Widget> widgets;

    public CreateGroupAction(final DisplayEditor editor, final List<Widget> widgets)
    {
        super(Messages.CreateGroup,
              AbstractUIPlugin.imageDescriptorFromPlugin(ModelPlugin.ID, "icons/group.png"));
        this.editor = editor;
        this.widgets = widgets;
    }

    @Override
    public void run()
    {
        editor.getWidgetSelectionHandler().clear();

        final GroupWidget group = new GroupWidget();

        // Create group that surrounds the original widget boundaries
        final Rectangle2D rect = GeometryTools.getDisplayBounds(widgets);

        // Inset depends on representation and changes with group title, font, ...
        // TODO Get default from JFX GroupRepresentation
        final int inset = 16;

        group.propX().setValue((int) rect.getMinX() - inset);
        group.propY().setValue((int) rect.getMinY() - inset);
        group.propWidth().setValue((int) rect.getWidth() + 2*inset);
        group.propHeight().setValue((int) rect.getHeight() + 2*inset);
        group.propName().setValue(org.csstudio.display.builder.model.Messages.GroupWidget_Name);

        final ChildrenProperty parent_children = ChildrenProperty.getParentsChildren(widgets.get(0));
        final UndoableActionManager undo = editor.getUndoableActionManager();
        undo.execute(new GroupWidgetsAction(parent_children, group, widgets, (int)rect.getMinX(), (int)rect.getMinY()));

        editor.getWidgetSelectionHandler().toggleSelection(group);
    }
}
