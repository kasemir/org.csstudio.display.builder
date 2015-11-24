/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.top;

import java.util.ArrayList;
import java.util.List;

/** Parser for {@link DisplayInfo}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DisplayInfoParser
{
    private static final char QUOTE = '"';

    /** Parse display info
     *
     *  <p>For format, see preferences.ini
     *
     *  @param display_infos String that contains display information
     *  @return {@link DisplayInfo}s
     */
    public static List<DisplayInfo> parse(final String display_infos) throws Exception
    {
        final List<DisplayInfo> displays = new ArrayList<>();

        int start = 0;

        int end = findDisplay(display_infos, start);
        while (end >= 0)
        {
            final String info_text = display_infos.substring(start, end);

            final int alias_end = findNextIgnoreQuotes('=', info_text, 0);
            String alias, path;
            if (alias_end >= 0  &&  alias_end < info_text.length())
            {   // Found alias = path
                alias = cleanup(info_text.substring(0, alias_end));
                path = cleanup(info_text.substring(alias_end+1));
            }
            else
            {   // Use file name as alias
                path = cleanup(info_text);
                final int sep = path.lastIndexOf('/');
                if (sep >= 0)
                    alias = path.substring(sep+1);
                else
                    alias = path;
                // Remove *.opi from alias
                if (alias.endsWith(".opi"))
                    alias = alias.substring(0, alias.length() - 4);
            }

            displays.add(new DisplayInfo(path, alias));

            start = end + 1;
            end = findDisplay(display_infos, start);
        }

        return displays;
    }

    /** @param text Text that may be quoted
     *  @return Trimmed, un-quoted text
     */
    private static String cleanup(String text)
    {
        text = text.trim();
        final int len = text.length();
        if (len > 0  &&  text.charAt(0) == QUOTE  &&  text.charAt(len-1) == QUOTE)
            return text.substring(1, len-1);
        return text;
    }

    /** Find next pipe-separated item
     *  @param text Text with comma-separated items
     *  @param start Where to look for the next item
     *  @return End index of item, or -1
     *  @throws Exception on error
     */
    private static int findDisplay(final String text, final int start) throws Exception
    {
        return findNextIgnoreQuotes('|', text, start);
    }

    /** Find next item
     *  @param separator Char that separates items
     *  @param text Text with items
     *  @param start Where to look for the next item
     *  @return End index of item, or -1
     *  @throws Exception on error
     */
    private static int findNextIgnoreQuotes(final char separator, final String text, final int start) throws Exception
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
            else if (c == separator)
                return pos;
            else
                ++pos;
        }
        return len;
    }
}
