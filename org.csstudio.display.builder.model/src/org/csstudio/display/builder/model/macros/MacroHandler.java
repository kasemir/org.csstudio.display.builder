/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
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
    /** Max. recursion level to guard against recursive macros that never resolve */
    // In principle, could try to detect loops like
    // A=$(B)
    // B=$(A)
    // Current implementation quits after MAX_RECURSION attempts because
    // that's much simpler and plenty fast.
    private static final int MAX_RECURSION = 50;

    // Pattern for $(xxx) or ${xxx}, asserting that there is NO leading '\' to escape it
    private static final Pattern spec = Pattern.compile("(?<!\\\\)\\$\\((\\w+)\\)" + "|" + "(?<!\\\\)\\$\\{(\\w+)\\}");

    /** Check if input contains unresolved macros
     *  @param input Text that may contain macros "$(NAME)" or "${NAME}",
     *  @return <code>true</code> if there is at least one unresolved macro
     */
    public static boolean containsMacros(final String input)
    {
        // Short cut to full regular expression
        if (input.indexOf('$') < 0)
            return false;

        // There is at least one '$'
        // Check if it matches the spec
        final Matcher matcher = spec.matcher(input);
        return matcher.find();
    }

    /** Replace macros in input
     *
     *  @param macros {@link MacroValueProvider} to use
     *  @param input Text that may contain macros "$(NAME)" or "${NAME}",
     *               also allowing nested "${{INNER}}"
     *  @return Text where all macros have been resolved
     *  @throws Exception on error, including recursive macro that never resolves
     */
    public static String replace(final MacroValueProvider macros, final String input) throws Exception
    {
        return replace(macros, input, 0, 0);
    }

    /** Replace macros in input
     *
     *  @param macros {@link MacroValueProvider} to use
     *  @param input Text that may contain macros "$(NAME)" or "${NAME}",
     *               also allowing nested "${{INNER}}"
     *  @param from Position within input where replacement should start
     *  @param recursion Recursion level
     *  @return Text where all macros have been resolved
     *  @throws Exception on error
     */
    private static String replace(final MacroValueProvider macros, final String input,
                                  final int from, final int recursion) throws Exception
    {
        if (recursion > MAX_RECURSION)
            throw new Exception("Recursive macro " + input);
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
            return replace(macros, result, 0, recursion + 1);
        }
        else
        {   // Leave macro unresolved, continue with remaining input
            return replace(macros, input, end+1, recursion);
        }
    }
}
