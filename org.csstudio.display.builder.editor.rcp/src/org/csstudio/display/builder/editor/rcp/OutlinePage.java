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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
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
            setActionDefinitionId(ActionFactory.UNDO.getCommandId());
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
            setActionDefinitionId(ActionFactory.REDO.getCommandId());
            setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
        }

        @Override
        public void run()
        {
            editor.getUndoableActionManager().redoLast();
        }
    }

    private class CutAction extends AcceleratedAction
    {
        public CutAction()
        {
            super(Messages.Cut, "Shortcut+X");
            setActionDefinitionId(ActionFactory.CUT.getCommandId());
            setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
        }

        @Override
        public void run()
        {
            editor.cutToClipboard();
        }
    }

    private class CopyAction extends AcceleratedAction
    {
        public CopyAction()
        {
            super(Messages.Copy, "Shortcut+C");
            setActionDefinitionId(ActionFactory.COPY.getCommandId());
            setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        }

        @Override
        public void run()
        {
            editor.copyToClipboard();
        }
    }

    private class PasteAction extends AcceleratedAction
    {
        public PasteAction()
        {
            super(Messages.Paste, "Shortcut+V");
            setActionDefinitionId(ActionFactory.PASTE.getCommandId());
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

    private class FindWidgetAction extends AcceleratedAction
    {

        public FindWidgetAction()
        {
            super(Messages.FindWidget, "Shortcut+F");
            setActionDefinitionId(ActionFactory.FIND.getCommandId());
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

    private class AcceleratedAction extends Action {

        private final KeyCombination accelerator;

        AcceleratedAction ( String text, String accelerator ) {
            super(text);
            this.accelerator = ( accelerator == null || accelerator.isEmpty() )
                             ? null
                             : KeyCombination.keyCombination(accelerator);
        }

        /**
         * @param event The {@link KeyEvent} under test.
         * @return {@code true} if this description contains an accelerator and it
         *         matches the given {@code event}, {@code false} otherwise.
         */
        public boolean match ( final KeyEvent event ) {
            if ( accelerator != null ) {
                return accelerator.match(event);
            } else {
                return false;
            }
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

        ActionForActionDescription(final int accelerator, final ActionDescription action)
        {
            this(action);
            setAccelerator(accelerator);
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
        final FindWidgetAction findWidgetAction = new FindWidgetAction();
        final CreateGroupAction createGroupAction = new CreateGroupAction(editor);
        final RemoveGroupAction removeGroupAction = new RemoveGroupAction(editor);
        final ActionForActionDescription toBackAction = new ActionForActionDescription(SWT.ALT | SWT.SHIFT | 'B', ActionDescription.TO_BACK);
        final ActionForActionDescription backwardAction = new ActionForActionDescription(SWT.ALT | 'B', ActionDescription.MOVE_UP);
        final ActionForActionDescription forwardAction = new ActionForActionDescription(SWT.ALT | 'F', ActionDescription.MOVE_DOWN);
        final ActionForActionDescription toFrontAction = new ActionForActionDescription(SWT.ALT | SWT.SHIFT | 'F', ActionDescription.TO_FRONT);
        final MenuManager manager = new MenuManager();

        manager.add(undoAction);
        manager.add(redoAction);
        manager.add(new Separator());
        manager.add(cutAction);
        manager.add(copyAction);
        manager.add(pasteAction);
        manager.add(findWidgetAction);
        manager.add(new Separator());
        manager.add(createGroupAction);
        manager.add(removeGroupAction);
        manager.add(new Separator());
        manager.add(toBackAction);
        manager.add(backwardAction);
        manager.add(forwardAction);
        manager.add(toFrontAction);

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

        tree.getView().setOnKeyPressed(event -> {
            if ( !WidgetTree.handleWidgetOrderKeys(event, editor) ) {
                if ( ActionDescription.UNDO.match(event) ) {
                    event.consume();
                    ActionDescription.UNDO.run(editor);
                } else if ( ActionDescription.REDO.match(event) ) {
                    event.consume();
                    ActionDescription.REDO.run(editor);
                } else if ( cutAction.match(event) ) {
                    event.consume();
                    cutAction.run();
                } else if ( copyAction.match(event) ) {
                    event.consume();
                    copyAction.run();
                } else if ( pasteAction.match(event) ) {
                    event.consume();
                    pasteAction.run();
                } else if ( findWidgetAction.match(event) ) {
                    event.consume();
                    findWidgetAction.run();
                }
            }
        });
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
