/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.script;

import java.util.concurrent.Future;

import javax.script.CompiledScript;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.vtype.pv.PV;

/** Compiled Java script
 *  @author Kay Kasemir
 */
class JavaScript implements Script
{
    private final JavaScriptSupport support;
    private final String name;
    private final CompiledScript code;

    /** Parse and compile script file
     *
     *  @param support {@link JavaScriptSupport} that will execute this script
     *  @param name Name of script (file name, URL)
     *  @param code Compiled code
     */
    public JavaScript(final JavaScriptSupport support, final String name, final CompiledScript code)
    {
        this.support = support;
        this.name = name;
        this.code = code;
    }

    /** @return Name of script (file name, URL) */
    public String getName()
    {
        return name;
    }

    /** @return Compiled code */
    public CompiledScript getCode()
    {
        return code;
    }

    @Override
    public Future<Object> submit(final Widget widget, final PV... pvs)
    {
        return support.submit(this, widget, pvs);
    }
}
