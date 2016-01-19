/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;

import java.util.Optional;

import org.csstudio.csdata.ProcessVariable;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.representation.ToolkitListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;

/** Context menu
 *  @author Kay Kasemir
 */
public class ContextMenuSupport
{
    private Widget context_menu_widget = null;

    /** Create SWT context menu
     *  @param site RCP site
     *  @param parent Parent SWT widget
     *  @param representation Representation
     */
    ContextMenuSupport(final IWorkbenchPartSite site, final Composite parent, final RCP_JFXRepresentation representation)
    {
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
        mm.addMenuListener((manager) ->
        {   // Widget info
            manager.add(new WidgetInfoAction(context_menu_widget));

            // TODO Eventually, widget's representation will want to
            // add widget-specific menu items.
            // Representation will need to provide list of
            // { String label, String icon_url, Consumer<Widget> runnable }
            // that can then be added to the menu manager right here.

            // Placeholder for ProcessVariable object contributions
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        });
        site.registerContextMenu(mm, sel_provider);

        // Create menu ..
        final Menu menu = mm.createContextMenu(parent);
        // .. but _don't_ attach to SWT control
        //     parent.setMenu(menu);
        // Menu is shown by representation listener _after_
        // setting the selection to widget's PV
        representation.addListener(new ToolkitListener()
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
        });
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
