/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.pv.vtype_pv;

import org.csstudio.display.builder.runtime.pv.RuntimePV;
import org.csstudio.display.builder.runtime.pv.RuntimePVFactory;
import org.csstudio.vtype.pv.PV;
import org.csstudio.vtype.pv.PVPool;

/** {@link RuntimePVFactory} for {@link PV}
 *
 *  @author Kay Kasemir
 */
public class VTypePVFactory implements RuntimePVFactory
{
    /** @param name PV Name that might contain legacy information
     *  @return Patched PV name
     */
    private String patch(String name)
    {
        // Remove PVManager's longString modifier.
        // Not using regular expression because text
        // had to be exactly like this at end of PV name.
        if (name.endsWith(" {\"longString\":true}"))
            name = name.substring(0,  name.length()-20);
        return name;
    }

    @Override
    public RuntimePV getPV(final String name) throws Exception
    {
        return new VTypePV(PVPool.getPV(patch(name)));
    }

    @Override
    public void releasePV(final RuntimePV pv)
    {
        PVPool.releasePV(((VTypePV)pv).getPV());
    }
}
