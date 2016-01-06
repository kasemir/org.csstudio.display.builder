/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.vtype.pv.PVPool;
import org.csstudio.vtype.pv.jca.JCA_PVFactory;
import org.csstudio.vtype.pv.local.LocalPVFactory;
import org.csstudio.vtype.pv.sim.SimPVFactory;

/** Display Runtime.
 *
 *  <p>Initializes display-wide facilities
 *
 *  @author Kay Kasemir
 */
public class DisplayRuntime extends WidgetRuntime<DisplayModel>
{
    private static boolean pv_initialized = false;

    static synchronized void init()
    {
        if (pv_initialized)
            return;

        // PVPool should initialize from registry
        if (PVPool.getSupportedPrefixes().length == 0)
        {   // Fall back for tests without OSGi
            PVPool.addPVFactory(new LocalPVFactory());
            PVPool.addPVFactory(new SimPVFactory());
            PVPool.addPVFactory(new JCA_PVFactory());
        }

        pv_initialized = true;
    }

    /** Start: Connect to PVs, ...
     *  @throws Exception on error
     */
    @Override
    public void start() throws Exception
    {
        init();
        super.start();
    }
}
