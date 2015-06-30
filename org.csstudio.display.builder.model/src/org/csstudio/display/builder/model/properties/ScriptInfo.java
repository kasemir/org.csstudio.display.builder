/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Information about a script.
 *
 *  <p>Script has a path
 *  TODO - implement embedded script, no path -
 *  and inputs.
 *  PVs will be created for each input/output.
 *  The script is executed whenever one or
 *  more of the 'triggering' inputs receive
 *  a new value.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ScriptInfo
{
    private final String path;
    private final List<ScriptPV> pvs;

    public ScriptInfo(final String path, final List<ScriptPV> pvs)
    {
        this.path = Objects.requireNonNull(path);
        this.pvs = Objects.requireNonNull(pvs);
    }

    public ScriptInfo(final String path, final ScriptPV... pvs)
    {
        this(path, Arrays.asList(pvs));
    }

    /** @return Path to the script. File ending determines type of script */
    public String getPath()
    {
        return path;
    }

    /** @return Input/Output PVs used by the script */
    public List<ScriptPV> getPVs()
    {
        return pvs;
    }

    @Override
    public String toString()
    {
        return "ScriptInfo(path='" + path + "', " + pvs + ")";
    }
}
