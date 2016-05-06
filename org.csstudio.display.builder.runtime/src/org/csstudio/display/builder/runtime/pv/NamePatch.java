/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.pv;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Patch for PV name
 *
 *  <p>Keeps pre-compiled pattern and the replacement.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class NamePatch
{
    final private Pattern pattern;
    final private String replacement;

    /** @param pattern Regular expression pattern, may contain groups "(..)"
     *  @param replacement Regular expression replacement, may contain references "$1"
     *  @throws PatternSyntaxException on error in pattern
     */
    public NamePatch(final String pattern, final String replacement) throws PatternSyntaxException
    {
        this.pattern = Pattern.compile(pattern);
        this.replacement = replacement.replace("[@]", "@");
    }

    public String patch(final String name)
    {
        return pattern.matcher(name).replaceAll(replacement);
    }

    @Override
    public String toString()
    {
        return "NamePatch " + pattern.pattern() + " -> " + replacement;
    }
}
