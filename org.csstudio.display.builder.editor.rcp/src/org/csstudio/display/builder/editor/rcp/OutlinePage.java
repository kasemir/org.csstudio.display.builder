/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import java.util.List;
import java.util.Random;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.EditorUtil;
import org.csstudio.display.builder.editor.actions.ActionDescription;
import org.csstudio.display.builder.editor.rcp.actions.CreateGroupAction;
import org.csstudio.display.builder.editor.rcp.actions.RemoveGroupAction;
import org.csstudio.display.builder.editor.tree.WidgetTree;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.javafx.DialogHelper;
import org.csstudio.javafx.PreferencesHelper;
import org.csstudio.javafx.swt.JFX_SWT_Wrapper;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.StackPane;

/** Outline view
 *
 *  <p>Displays tree of widgets
 *  @author Kay Kasemir
 */
public class OutlinePage extends Page implements IContentOutlinePage
{
    private class UndoAction extends Action
    {
        public UndoAction()
        {
            super(Messages.Undo);
            setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
        }

        @Override
        public void run()
        {
            editor.getUndoableActionManager().undoLast();
        }
    }

    private class RedoAction extends Action
    {
        public RedoAction()
        {
            super(Messages.Redo);
            setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
        }

        @Override
        public void run()
        {
            editor.getUndoableActionManager().redoLast();
        }
    }

    private class CutAction extends Action
    {
        public CutAction()
        {
            super(Messages.Cut);
            setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
        }

        @Override
        public void run()
        {
            editor.cutToClipboard();
        }
    }

    private class CopyAction extends Action
    {
        public CopyAction()
        {
            super(Messages.Copy);
            setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        }

        @Override
        public void run()
        {
            editor.copyToClipboard();
        }
    }

    private class PasteAction extends Action
    {
        public PasteAction()
        {
            super(Messages.Paste);
            setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
        }

        @Override
        public void run()
        {
            final Random random = new Random();
            int mouse_x = random.nextInt(100);
            int mouse_y = random.nextInt(100);

            editor.pasteFromClipboard(mouse_x, mouse_y);
        }
    }

    private class FindWidgetAction extends Action
    {
        public FindWidgetAction()
        {
            super(Messages.FindWidget);
            setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));
        }

        @Override
        public void run()
        {
            // Prompt for widget name
            final TextInputDialog prompt = new TextInputDialog();
            prompt.setTitle(Messages.FindWidget);
            prompt.setHeaderText("Enter (partial) widget name");
            DialogHelper.positionAndSize(prompt, tree.getView(), PreferencesHelper.userNodeForClass(OutlinePage.class), 32, 32);
            final String pattern = prompt.showAndWait().orElse(null);
            if (pattern != null  &&  !pattern.isEmpty())
                editor.selectWidgetsByName(pattern);
        }
    }

    private class ActionForActionDescription extends Action
    {
        private final ActionDescription action;

        ActionForActionDescription(final ActionDescription action)
        {
            super(action.getToolTip(),
                  AbstractUIPlugin.imageDescriptorFromPlugin(org.csstudio.display.builder.editor.Plugin.ID,
                                                             action.getIcon()));
            this.action = action;
        }

        @Override
        public void run()
        {
            action.run(editor);
        }
    }

    private final DisplayEditor editor;
    private final WidgetTree tree;

    private Control canvas;

    public OutlinePage(final DisplayEditor editor)
    {
        this.editor = editor;
        tree = new WidgetTree(editor);
    }

    public void setModel(final DisplayModel model)
    {
        tree.setModel(model);
    }

    @Override
    public void createControl(final Composite parent)
    {
        final JFX_SWT_Wrapper wrapper = new JFX_SWT_Wrapper(parent, () ->
        {
            // StackPane w/ tree as single child to 'fill' the available space.
            final StackPane root = new StackPane(tree.create());
            return new Scene(root, 200.0, 400.0);
        });
        canvas = wrapper.getFXCanvas();
        EditorUtil.setSceneStyle(wrapper.getScene());

        createContextMenu();
    }

    private void createContextMenu()
    {
        final UndoAction undoAction = new UndoAction();
        final RedoAction redoAction = new RedoAction();
        final CutAction cutAction = new CutAction();
        final CopyAction copyAction = new CopyAction();
        final PasteAction pasteAction = new PasteAction();
        final CreateGroupAction createGroupAction = new CreateGroupAction(editor);
        final RemoveGroupAction removeGroupAction = new RemoveGroupAction(editor);
        final MenuManager manager = new MenuManager();

        manager.add(undoAction);
        manager.add(redoAction);
        manager.add(new Separator());
        manager.add(cutAction);
        manager.add(copyAction);
        manager.add(pasteAction);
        manager.add(new FindWidgetAction());
        manager.add(new Separator());
        manager.add(createGroupAction);
        manager.add(removeGroupAction);
        manager.add(new Separator());
        manager.add(new ActionForActionDescription(ActionDescription.TO_BACK));
        manager.add(new ActionForActionDescription(ActionDescription.MOVE_UP));
        manager.add(new ActionForActionDescription(ActionDescription.MOVE_DOWN));
        manager.add(new ActionForActionDescription(ActionDescription.TO_FRONT));

        final Menu menu = manager.createContextMenu(canvas);

        menu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuShown ( MenuEvent e ) {
                final List<Widget> selection = editor.getWidgetSelectionHandler().getSelection();
                final int selectionSize = selection.size();
                final String xml = Clipboard.getSystemClipboard().getString();
                undoAction.setEnabled(editor.getUndoableActionManager().canUndo());
                redoAction.setEnabled(editor.getUndoableActionManager().canRedo());
                cutAction.setEnabled(selectionSize >= 1);
                copyAction.setEnabled(selectionSize >= 1);
                pasteAction.setEnabled(xml != null && xml.startsWith("<?xml")  && xml.contains("<display"));
                createGroupAction.setEnabled(selectionSize >= 1);
                removeGroupAction.setEnabled(selectionSize == 1  &&  selection.get(0) instanceof GroupWidget);
            }
        });
        canvas.setMenu(menu);
    }

    @Override
    public Control getControl()
    {
        return canvas;
    }

    @Override
    public void setFocus()
    {
        canvas.setFocus();
    }

    // Pass selection from WidgetTree on to SWT/RCP?
    // Seems unnecessary, the WidgetTree and DisplayEditor
    // use their own WidgetSelectionHandler, not RCP.

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener)
    {
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener)
    {
    }

    @Override
    public ISelection getSelection()
    {
        return null;
    }

    @Override
    public void setSelection(ISelection selection)
    {
    }

    @Override
    public void dispose()
    {
        tree.setModel(null);
        super.dispose();
    }
}
