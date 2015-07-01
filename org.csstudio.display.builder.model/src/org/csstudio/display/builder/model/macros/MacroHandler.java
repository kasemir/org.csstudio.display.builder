/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.macros;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Handler for {@link Macros}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class MacroHandler
{
    // Pattern for $(xxx) or ${xxx}
    private static final Pattern spec = Pattern.compile("\\$\\((\\w+)\\)" + "|" + "\\$\\{(\\w+)\\}");

    /** Replace macros in input
     *
     *  @param macros {@link Macros} to use
     *  @param input Text that may contain macros "$(NAME)" or "${NAME}",
     *               also allowing nested "${{INNER}}"
     *  @return Text where all macros have been resolved
     */
    public static String replace(final Macros macros, final String input)
    {
        return replace(macros, input, 0);
    }

    /** Replace macros in input
     *
     *  @param macros {@link Macros} to use
     *  @param input Text that may contain macros "$(NAME)" or "${NAME}",
     *               also allowing nested "${{INNER}}"
     *  @return Text where all macros have been resolved
     */
    public static String replace(final Macros macros, final String input, final int from)
    {
        // Short cut if there is nothing to replace
        if (input.indexOf('$',  from) < 0)
            return input;

        // Find first macro
        final Matcher matcher = spec.matcher(input);
        if (matcher.find(from) == false)
            return input;

        // Was it a $(macro) or ${macro}?
        final int which = matcher.start(1) > 0 ? 1 : 2;
        // Start and end of macro name
        final int start = matcher.start(which);
        final int end = matcher.end(which);
        final String name = input.substring(start, end);
        // Resolve
        final String value = macros.getValue(name);
        if (value != null)
        {
            // Replace macro in input, removing the '$(' resp. ')'
            final String result = input.substring(0, start-2) + value + input.substring(end+1);

            // Text has now changed.
            // Subsequent calls to find() would return indices for the original text
            // which are no longer valid for the changed text
            // -> Recurse with updated text for next macro,
            //    which also handles nested $($(INNER))
            return replace(macros, result, 0);
        }
        else
        {   // Leave macro unresolved, continue with remaining input
            return replace(macros, input, end+1);
        }
    }
}
