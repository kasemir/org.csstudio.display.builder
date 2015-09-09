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
 *  <p>Script has a path and inputs.
 *  Info may also contain the script text,
 *  in which case the path is only used to identify the
 *  script type.
 *
 *  <p>PVs will be created for each input/output.
 *  The script is executed whenever one or
 *  more of the 'triggering' inputs receive
 *  a new value.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ScriptInfo
{
    /** Script 'path' used to indicate an embedded python script */
    public final static String EMBEDDED_PYTHON = "EmbeddedPy";

    private final String file, text;
    private final List<ScriptPV> pvs;

    public ScriptInfo(final String file, final String text, final List<ScriptPV> pvs)
    {
        this.file = Objects.requireNonNull(file);
        this.text = text;
        this.pvs = Objects.requireNonNull(pvs);
    }

    public ScriptInfo(final String path, final ScriptPV... pvs)
    {
        this(path, null, Arrays.asList(pvs));
    }

    /** @return Path to the script. May be URL, or contain macros. File ending determines type of script */
    public String getFile()
    {
        if (text != null)
            return EMBEDDED_PYTHON;
        return file;
    }

    /** @return Script text, may be <code>null</code> */
    public String getText()
    {
        return text;
    }

    /** @return Input/Output PVs used by the script */
    public List<ScriptPV> getPVs()
    {
        return pvs;
    }

    @Override
    public String toString()
    {
        return "ScriptInfo('" + file + "', " + pvs + ")";
    }
}
