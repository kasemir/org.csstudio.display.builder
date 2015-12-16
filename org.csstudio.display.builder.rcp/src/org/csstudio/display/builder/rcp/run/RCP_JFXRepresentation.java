/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import javafx.scene.Group;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

/** Represent display builder in JFX inside RCP Views
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RCP_JFXRepresentation extends JFXRepresentation
{
    // Similar to JFXRepresentation, but using RuntimeViewPart as 'Window'

    public RCP_JFXRepresentation()
    {
        RuntimeUtil.hookRepresentationListener(this);
    }

    @Override
    public Group openNewWindow(final DisplayModel model,
                               final Consumer<DisplayModel> close_handler) throws Exception
    {
        final RuntimeViewPart part = RuntimeViewPart.open(close_handler);
        return part.getRoot();
    }

    @Override
    public void representModel(final Group parent, final DisplayModel model)
            throws Exception
    {
        // Top-level Group of the part's Scene has pointer to RuntimeViewPart.
        // For EmbeddedDisplayWidget, the parent is inside the EmbeddedDisplayWidget.
        final RuntimeViewPart part = (RuntimeViewPart) parent.getProperties().get(RuntimeViewPart.ROOT_RUNTIME_VIEW_PART);
        if (part != null)
            part.trackCurrentModel(model);

        super.representModel(parent, model);
    }

    /** Create Java FX {@link MenuItem}s for all PV contributions
     *
     *  <p>With legacy implementation, PV related menu items were added
     *  via object contributions.
     *
     *  <p>Parse the registry information and create JFX menu items.
     *
     *  @return List of {@link MenuItem}s
     */
    @Override
    public List<MenuItem> getPVMenuItems()
    {
        final List<MenuItem> pv_menu_items = new ArrayList<>();

        final ICommandService commands = PlatformUI.getWorkbench().getService(ICommandService.class);

        final IExtensionRegistry registry = RegistryFactory.getRegistry();
        for (IConfigurationElement config :  registry.getConfigurationElementsFor("org.eclipse.ui.menus"))
        {
            // Look for menuContribution with specific locationURI
            if (! (config.getName().equals("menuContribution") &&
                   config.getAttribute("locationURI").equals("popup:org.csstudio.ui.menu.popup.processvariable")))
                continue;

            // Expect exactly one <command commandId="some.command.to.invoke" ...
            // XXX A few contributions use a dynamic menu, no command. Not handled.
            final IConfigurationElement[] configs = config.getChildren("command");
            if (configs.length != 1)
                continue;

            final IConfigurationElement cmd = configs[0];
            final String cmd_id = cmd.getAttribute("commandId");
            final Command command = commands.getCommand(cmd_id);

            String label = cmd.getAttribute("label");
            if (label == null)
                try
                {
                    label = command.getName();
                }
                catch (NotDefinedException e)
                {
                    label = cmd_id;
                }

            final MenuItem item = new MenuItem(label);
            final String icon_attr = cmd.getAttribute("icon");
            if (icon_attr != null)
            {
                final String icon_url = "platform:/plugin/" + cmd.getContributor().getName() + "/" + icon_attr;
                item.setGraphic(new ImageView(icon_url));
            }
            item.setOnAction((event) ->
            {
                // TODO Set RCP selection to widget's PV
                final ExecutionEvent exe_event = new ExecutionEvent();
                try
                {
                    command.executeWithChecks(exe_event);
                }
                catch (Exception ex)
                {
                    logger.log(Level.WARNING, "Cannot invoke PV menu command" + command, ex);
                }
            });
            pv_menu_items.add(item);
        }
        return pv_menu_items;
    }
}
