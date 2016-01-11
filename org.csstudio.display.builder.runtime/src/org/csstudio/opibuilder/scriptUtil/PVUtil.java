/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.opibuilder.scriptUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Compatibility wrapper of PVUtil for legacy scripts
 *
 *  <p>When the legacy opibuilder is included in the product,
 *  its scriptUtils will be found and they will fail.
 *
 *  <p>If the legacy opibuilder is not included in the product,
 *  existing scripts will find this wrapper which will "work"
 *  for the most part, but issue an initial warning so that
 *  scripts can be updated.
 *
 *  @author Kay Kasemir
 */
public class PVUtil extends org.csstudio.display.builder.runtime.script.PVUtil
{
    // TODO Change PVUtil compatibility mechanism
    // When both opibuilder and display.builder plugins are in product,
    // unclear which one the script sees.
    // If it sees this one, that will break scripts called by legacy runtime.
    static
    {
        Logger.getLogger(PVUtil.class.getName())
              .log(Level.SEVERE,
                   "Script accessed deprecated org.csstudio.opibuilder.scriptUtil.PVUtil," +
                   " update to org.csstudio.display.builder.runtime.script.PVUtil");
    }
}
