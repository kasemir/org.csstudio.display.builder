/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/** Plugin information.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Plugin implements BundleActivator
{
    /** Plugin ID */
    public final static String ID = "org.csstudio.display.builder.rcp";

    @Override
    public void start(final BundleContext context) throws Exception
    {
        getPVMenuItems();
    }

    @Override
    public void stop(final BundleContext context) throws Exception
    {
    }

    /** @param key Preference key
     *  @param default_value Default value
     *  @return Preference text or default value
     */
    public static String getPreference(final String key, final String default_value)
    {
        final IPreferencesService prefs = Platform.getPreferencesService();
        return prefs.getString(Plugin.ID, key, default_value, null);
    }

    /** TODO Context menus of JFX widgets should still have the PV menu items,
     *       but can't use the RCP/SWT object contributions?
     *       --> Find the labels/icons/commands to invoke from regitry?
     */
    private void getPVMenuItems()
    {
        final ICommandService commands = PlatformUI.getWorkbench().getService(ICommandService.class);

        final IExtensionRegistry registry = RegistryFactory.getRegistry();
        // Look for
        // <menuContribution locationURI="popup:org.csstudio.ui.menu.popup.processvariable">
        //     <command commandId="some.command.to.invoke" ...
        for (IConfigurationElement config :  registry.getConfigurationElementsFor("org.eclipse.ui.menus"))
        {
            if (! config.getName().equals("menuContribution"))
                continue;
            if (! config.getAttribute("locationURI").equals("popup:org.csstudio.ui.menu.popup.processvariable"))
                continue;
            final IConfigurationElement[] configs = config.getChildren("command");
            if (configs.length != 1)
                continue; // Uses dynamic item, no plain command

            final IConfigurationElement cmd = configs[0];
            final String cmd_id = cmd.getAttribute("commandId");
            System.out.println("\nLabel: " + cmd.getAttribute("label"));
            final Command command = commands.getCommand(cmd_id);
            System.out.println("COMMAND:" + command);
            final String icon_attr = cmd.getAttribute("icon");
            System.out.println("Icon: " + AbstractUIPlugin.imageDescriptorFromPlugin(cmd.getContributor().getName(), icon_attr));
        }
    }
}
