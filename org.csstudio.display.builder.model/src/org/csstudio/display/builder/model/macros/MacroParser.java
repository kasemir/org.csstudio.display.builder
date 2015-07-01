/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.macros;

/** Parses macro definitions
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class MacroParser
{
    private static final char QUOTE = '"';

    /** Create macros from a string
     *
     *  <p>Parses a list of "macro=value" entries,
     *  separated by comma.
     *  The value may be quoted, in which case it can contain commas.
     *
     *  @param definition Macro definition of the form 'macro1=value, macro2="Value, another'"'
     *  @return Macros
     *  @throws Exception on error
     */
    public static Macros parseDefinition(final String definition) throws Exception
    {
        final Macros macros = new Macros();
        int start = 0;
        int end = findItem(definition, start);
        while (end >= start)
        {
            final String item = definition.substring(start, end);
            final String[] info = parseItem(item);
            macros.add(info[0], info[1]);
            start = end+1;
            end = findItem(definition, start);
        }
        return macros;
    }

    /** Find next comma-separated item
     *  @param text Text with comma-separated items
     *  @param start Where to look for the next item
     *  @return End index of item, or -1
     *  @throws Exception on error
     */
    private static int findItem(final String text, final int start) throws Exception
    {
        final int len = text.length();
        if (start >= len)
            return -1;
        int pos=start;
        while (pos < len)
        {
            final char c = text.charAt(pos);
            if (c == QUOTE)
            {   // Proceed to end of quote, ignoring escaped quotes
                int end = text.indexOf(QUOTE, pos+1);
                while (end > 0  &&  text.charAt(end-1) == '\\')
                    end = text.indexOf(QUOTE, end+1);
                if (end < 0)
                    throw new Exception("Missing end of quoted text in '" + text + "'");
                pos = end + 1;
            }
            else if (c == ',')
                return pos;
            else
                ++pos;
        }
        return len;
    }

    /** Parse one "macro=value" item, handling quoted values
     *  @param item Description of one item
     *  @return { name, value }
     *  @throws Exception on error
     */
    private static String[] parseItem(final String item) throws Exception
    {
        final int sep = item.indexOf('=');
        if (sep < 0)
            throw new Exception("Missing macro=value in '" + item + "'");
        final String name = item.substring(0, sep).trim();
        String value = item.substring(sep+1).trim();
        if (value.length() > 0  &&  value.charAt(0) == QUOTE)
        {
            final int closing = value.lastIndexOf(QUOTE);
            if (closing < 0)
                throw new Exception("Missing closing quote in '" + value + "'");
            // Not trimmed, so that " Value with spaces " preserves the spaces
            value = value.substring(1, closing);
        }
        return new String[] { name, value };
    }
}
