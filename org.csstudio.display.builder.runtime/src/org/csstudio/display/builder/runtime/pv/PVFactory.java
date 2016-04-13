/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.pv;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.runtime.Preferences;
import org.csstudio.display.builder.runtime.pv.vtype_pv.VTypePVFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

/** Factory for PVs used by the widget runtime
 *
 *  <p>Allows pluggable implementations: vtype.pv, PVManager, ..
 *
 *  @author Kay Kasemir
 */
public class PVFactory
{
    /** The PV factory */
    private final static RuntimePVFactory factory;

    static
    {
        final Logger logger = Logger.getLogger(PVFactory.class.getName());

        RuntimePVFactory the_factory = null;

        // Try to use extension point
        final IExtensionRegistry registry = RegistryFactory.getRegistry();
        if (registry == null)
        {   // Fall back for running without OSGi
            logger.log(Level.CONFIG, "Defaulting to VTypePVFactory");
            the_factory = new VTypePVFactory();
        }
        else
        {
            try
            {   // Locate available PV factories
                final Map<String, RuntimePVFactory> factories = new HashMap<>();
                for (IConfigurationElement config : registry.getConfigurationElementsFor(RuntimePVFactory.EXTENSION_POINT))
                {
                    final String id = config.getAttribute("id");
                    logger.log(Level.CONFIG, "{0} contributes {1}", new Object[] { config.getContributor().getName(), id });
                    factories.put(id, (RuntimePVFactory) config.createExecutableExtension("class"));
                }
                // Use the factory selected in preferences
                final String selected = Preferences.getPV_Factory();
                logger.log(Level.CONFIG, "Selected PV Factory {0}", selected);
                the_factory = factories.get(selected);
                if (the_factory == null)
                    logger.log(Level.SEVERE, "Cannot locate PV Factory \"{0}\". Available: {1}", new Object[] { selected, factories.keySet() });
            }
            catch (Exception ex)
            {
                logger.log(Level.SEVERE, "Cannot locate PV Factories", ex);
            }
        }

        factory = the_factory;
    }

    /** Get a PV
     *  @param name Name of PV
     *  @return {@link RuntimePV}
     *  @throws Exception on error
     */
    public static RuntimePV getPV(final String name) throws Exception
    {
        return factory.getPV(name);
    }

    /** Release a PV (close, dispose resources, ...)
     *  @param pv {@link RuntimePV} to release
     */
    public static void releasePV(final RuntimePV pv)
    {
        factory.releasePV(pv);
    }
}
