/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.pv;

import org.csstudio.vtype.pv.PV;

/** Listener to a {@link PV}
 *  @author Kay Kasemir
 */
public interface RuntimePVFactory
{
    public RuntimePV getPV(String name) throws Exception;

    public void releasePV(RuntimePV pv);
}
