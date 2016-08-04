package org.csstudio.display.builder.editor.rcp;

import static org.csstudio.display.builder.rcp.Plugin.logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.editor.undo.AddWidgetAction;
import org.csstudio.display.builder.editor.undo.RemoveWidgetsAction;
import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetFactory;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.ArrayWidget;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

/**
 * Helper for creating the SWT/RCP context menu to morph widgets (replace
 * widgets with a particular type of widget) in editor.
 *
 * Intended to for use as a sub-menu in editor's main context menu.
 *
 * @author Amanda Carpenter
 *
 */
@SuppressWarnings("nls")
public class MorphWidgetMenuSupport
{
    private final DisplayEditor editor;

    private class MorphAction extends Action
    {
        final WidgetDescriptor descriptor;

        MorphAction(WidgetDescriptor descr)
        {
            descriptor = descr;
            setText(descriptor.getName());
            ImageDescriptor image = null;
            try
            {
                image = ImageDescriptor.createFromImageData(new ImageData(descriptor.getIconStream()));
            } catch (Exception e)
            {
                logger.log(Level.WARNING, "Cannot create menu icon for widget type " + descr.getType(), e); //$NON-NLS-1$
            }
            setImageDescriptor(image);
        }

        @Override
        public void run()
        {
            final WidgetSelectionHandler selection = editor.getWidgetSelectionHandler();
            List<Widget> widgets = new ArrayList<Widget>(selection.getSelection());
            for (Widget widget : widgets)
            {
                if (widget.getType().equals(descriptor.getType()))
                    continue;
                final ChildrenProperty target = ChildrenProperty.getParentsChildren(widget);
                //ArrayWidgets should avoid holding elements of different types in their list of children,
                //in order to avoid errors with matching element properties.
                if (target.getWidget() instanceof ArrayWidget)
                {
                    List<Widget> children = new ArrayList<Widget>(target.getValue());
                    //remove all children of ArrayWidget from editor
                    editor.getUndoableActionManager().execute(new RemoveWidgetsAction(children));

                    //add copies of selected children to editor
                    children.retainAll(widgets);
                    for (Widget child : children)
                    {
                        final Widget new_widget = createNewWidget(child);
                        editor.getUndoableActionManager().execute(new AddWidgetAction(target, new_widget));
                    }

                    //ignore children in subsequent iterations
                    children.remove(widget);
                    widgets.removeAll(children);
                }
                else
                {
                    final Widget new_widget = createNewWidget(widget);
                    editor.getUndoableActionManager().execute(new RemoveWidgetsAction(Arrays.asList(widget)));
                    editor.getUndoableActionManager().execute(new AddWidgetAction(target, new_widget));
                }
            }
        }

        private Widget createNewWidget(Widget widget)
        {
            final Widget new_widget = descriptor.createWidget();
            final Set<WidgetProperty<?>> props = widget.getProperties();
            for (WidgetProperty<?> prop : props)
            {
                Optional<WidgetProperty<?>> new_prop = new_widget.checkProperty(prop.getName());
                if (new_prop.isPresent())
                    try
                    {
                        new_prop.get().setValueFromObject(prop.getValue());
                    } catch (Exception ignored)
                    {
                    }
            }
            return new_widget;
        }
    }

    final MenuManager mm;

    //not intended for use, but for testing/debugging
    public MorphWidgetMenuSupport(final DisplayEditor editor, final Composite parent)
    {
        this.editor = editor;
        mm = createMenuManager();
        final Menu menu = createMenu(mm, parent);
        parent.setMenu(menu);
    }

    public MorphWidgetMenuSupport(final DisplayEditor editor)
    {
        this.editor = editor;
        mm = createMenuManager();
    }

    public MenuManager getMenuManager()
    {
        return mm;
    }

    private MenuManager createMenuManager()
    {
        final MenuManager mm = new MenuManager(Messages.ReplaceWith);
        mm.setRemoveAllWhenShown(true);
        mm.addMenuListener((manager)->
        {
            if (editor.getWidgetSelectionHandler().getSelection().isEmpty())
                manager.add(new Action(Messages.ReplaceWith_NoWidgets) {});
            else
                for (WidgetDescriptor descr : WidgetFactory.getInstance().getWidgetDescriptions())
                    manager.add(new MorphAction(descr));
        });

        mm.setImageDescriptor(Plugin.getIcon("replace.png"));
        return mm;
    }

    private Menu createMenu(MenuManager mm, Composite parent)
    {
        Menu menu = mm.createContextMenu(parent);
        menu.setVisible(true);
        return menu;
    }
}
