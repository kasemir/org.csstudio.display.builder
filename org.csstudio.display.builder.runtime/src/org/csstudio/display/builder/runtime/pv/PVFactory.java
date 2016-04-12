/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.pv;

import org.csstudio.display.builder.runtime.pv.vtype_pv.VTypePVFactory;

/** Factory for PVs used by the widget runtime
 *
 *  <p>Allows pluggable implementations: vtype.pv, PVManager
 *
 *  @author Kay Kasemir
 */
public class PVFactory
{
    private final static RuntimePVFactory factory;

    static
    {
        // TODO Extension point, locate which one to use via preferences
        factory = new VTypePVFactory();
    }

    public static RuntimePV getPV(final String name) throws Exception
    {
        return factory.getPV(name);
    }

    public static void releasePV(final RuntimePV pv)
    {
        factory.releasePV(pv);
    }
}
