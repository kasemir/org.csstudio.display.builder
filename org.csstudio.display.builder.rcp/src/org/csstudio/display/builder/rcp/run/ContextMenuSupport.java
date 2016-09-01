/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.rcp.Plugin.logger;

import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;

import org.csstudio.csdata.ProcessVariable;
import org.csstudio.display.builder.model.ModelPlugin;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.representation.ToolkitListener;
import org.csstudio.display.builder.representation.javafx.widgets.JFXBaseRepresentation;
import org.csstudio.display.builder.runtime.ActionUtil;
import org.csstudio.display.builder.runtime.RuntimeAction;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import javafx.scene.Node;
import javafx.scene.Scene;

/** Context menu
 *
 *  <p>Helper for creating the RCP/SWT context menu
 *  for a widget.
 *
 *  <p>Adds the runtime actions and widget actions,
 *  and allows object contributions from RCP
 *  for the PV of the widget.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ContextMenuSupport
{
    private Shell shell;

    /** SWT/JFace Action for a model's ActionInfo
     *
     *  <p>Shows the ActionInfo's description and icon,
     *  invokes it.
     */
    private static class ActionInfoWrapper extends Action
    {
        private final Widget widget;
        private final ActionInfo info;
        public ActionInfoWrapper(final Widget widget, final ActionInfo info)
        {
            super(info.getDescription(),
                  AbstractUIPlugin.imageDescriptorFromPlugin(ModelPlugin.ID, info.getType().getIconPath()));
            this.widget = widget;
            this.info = info;
        }

        @Override
        public void run()
        {
            ActionUtil.handleAction(widget, info);
        }
    }

    /** SWT/JFace Action for a model's RuntimeAction
     *
     *  <p>Shows the description and icon,
     *  invokes it.
     */
    private static class RuntimeActionWrapper extends Action
    {
        private final RuntimeAction info;
        public RuntimeActionWrapper(final Widget widget, final RuntimeAction info)
        {
            super(info.getDescription(), getIcon(info));
            this.info = info;
        }

        @Override
        public void run()
        {
            info.run();
        }
    }

    private static ImageDescriptor getIcon(final RuntimeAction info)
    {
        try
        {
            return ImageDescriptor.createFromURL(new URL(info.getIconPath()));
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot obtain icon " + info.getIconPath(), ex);
            return null;
        }
    }

    private Widget context_menu_widget = null;

    private final IMenuListener menu_listener = (IMenuManager manager) ->
    {
        if (context_menu_widget == null)
        {
            logger.log(Level.WARNING, "Missing context_menu_widget");
            manager.add(new Action("No widget") {});
        }
        else
        {
            // Widget info
            manager.add(new WidgetInfoAction(context_menu_widget));

            // Actions of the widget
            for (ActionInfo info : context_menu_widget.behaviorActions().getValue())
                manager.add(new ActionInfoWrapper(context_menu_widget, info));

            // Actions of the widget runtime
            final WidgetRuntime<Widget> runtime = RuntimeUtil.getRuntime(context_menu_widget);
            if (runtime == null)
                throw new NullPointerException("Missing runtime for " + context_menu_widget);
            for (RuntimeAction info : runtime.getRuntimeActions())
                manager.add(new RuntimeActionWrapper(context_menu_widget, info));
        }

        // Placeholder for ProcessVariable object contributions
        manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Separator());

        if (context_menu_widget != null)
        {
            final Node node = JFXBaseRepresentation.getJFXNode(context_menu_widget);
            final Scene scene = node.getScene();

            manager.add(new SendEMailAction(shell, scene));
            manager.add(new SendLogbookAction(shell, scene));
        }

        // Placeholder for the display editor.
        // If editor.rcp plugin is included, it adds "Open in editor"
        manager.add(new Separator("display_editor"));
        manager.add(new ReloadDisplayAction());
    };

    /** Create SWT context menu
     *  @param site RCP site
     *  @param parent Parent SWT widget
     *  @param representation Representation
     */
    public ContextMenuSupport(final IWorkbenchPartSite site, final Composite parent, final RCP_JFXRepresentation representation)
    {
        shell = site.getShell();

        // Tried to use a JFX context menu on the individual items,
        // but adding the existing PV contributions requires parsing
        // the registry and creating suitable JFX menu entries.
        // Finally, it was unclear how to set the "activeMenuSelection"
        // required by existing object contributions.
        //
        // So using SWT context menu, automatically populated with PV contributions.

        // Selection provider to inform RCP about PV for the context menu
        final ISelectionProvider sel_provider = new RCPSelectionProvider();
        site.setSelectionProvider(sel_provider);

        // RCP context menu w/ "additions" placeholder for contributions
        final MenuManager mm = new MenuManager();
        mm.setRemoveAllWhenShown(true);
        mm.addMenuListener(menu_listener);
        site.registerContextMenu(mm, sel_provider);

        // Create menu ..
        final Menu menu = mm.createContextMenu(parent);
        // .. but _don't_ attach to SWT control
        //     parent.setMenu(menu);

        // Menu is shown by representation listener _after_
        // setting the selection to widget's PV
        final ToolkitListener tkl = new ToolkitListener()
        {
            @Override
            public void handleContextMenu(final Widget widget)
            {
                IStructuredSelection sel = StructuredSelection.EMPTY;
                final Optional<WidgetProperty<String>> name_prop = widget.checkProperty(behaviorPVName);
                if (name_prop.isPresent())
                {
                    final String pv_name = name_prop.get().getValue();
                    if (!pv_name.isEmpty())
                        sel = new StructuredSelection(new ProcessVariable(pv_name));
                }
                sel_provider.setSelection(sel);

                // Show the menu
                context_menu_widget = widget;
                menu.setVisible(true);
            }
        };
        representation.addListener(tkl);
        parent.addDisposeListener(event -> representation.removeListener(tkl));

        // Clear context_menu_widget reference when menu closed
        menu.addMenuListener(new MenuAdapter()
        {
            @Override
            public void menuHidden(MenuEvent e)
            {
                context_menu_widget = null;
            }
        });
    }
}
