package org.csstudio.display.builder.editor.rcp;

import static org.csstudio.display.builder.rcp.Plugin.logger;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetFactory;
import org.csstudio.display.builder.model.WidgetProperty;
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
public class MorphWidgetMenuSupport
{
    private final WidgetSelectionHandler selection;

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
            List<Widget> widgets = selection.getSelection();
            for (Widget widget : widgets)
            {
                final Widget new_widget = descriptor.createWidget();
                logger.warning("Morphing "+widget+" to "+new_widget); //$NON-NLS-1$ //$NON-NLS-2$
                final Set<WidgetProperty<?>> props = widget.getProperties();
                for (WidgetProperty<?> prop : props)
                {
                    Optional<WidgetProperty<?>> new_prop = new_widget.checkProperty(prop.getName());
                    if (new_prop.isPresent())
                        try
                        {
                            new_prop.get().setValueFromObject(prop.getValue());
                        } catch (Exception ignored)
                        {}
                }
                //toggleSelection:
                //If selected widgets are removed from or not yet added to the target
                //ChildrenProperty, attempting to toggleSelection will result in an error getting
                //the display model when selection change listeners are called, as it's not part
                //of a display. Thus, the old widget must be de-selected before removing it, and
                //the new one must be added before selecting it.
                selection.toggleSelection(widget);
                final ChildrenProperty target = ChildrenProperty.getParentsChildren(widget);
                target.addChild(target.removeChild(widget), new_widget);
                selection.toggleSelection(new_widget);
            }
        }
    }

    final MenuManager mm;

    public MorphWidgetMenuSupport(final WidgetSelectionHandler selection, final Composite parent)
    {
        this.selection = selection;
        mm = createMenuManager();
        final Menu menu = createMenu(mm, parent);
        parent.setMenu(menu);
    }

    public MorphWidgetMenuSupport(final WidgetSelectionHandler selection)
    {
        this.selection = selection;
        mm = createMenuManager();
    }

    public MenuManager getMenuManager()
    {
        return mm;
    }

    private MenuManager createMenuManager()
    {
        final MenuManager mm = new MenuManager("Replace with"); //$NON-NLS-1$ TODO localize
        mm.setRemoveAllWhenShown(true);
        mm.addMenuListener((manager)->
        {
            if (selection.getSelection().isEmpty())
                manager.add(new Action("<No selected widgets>") {}); //$NON-NLS-1$ TODO localize
            else
                for (WidgetDescriptor descr : WidgetFactory.getInstance().getWidgetDescriptions())
                    manager.add(new MorphAction(descr));
        });
        return mm;
    }

    private Menu createMenu(MenuManager mm, Composite parent)
    {
        Menu menu = mm.createContextMenu(parent);
        menu.setVisible(true);
        return menu;
    }
}
