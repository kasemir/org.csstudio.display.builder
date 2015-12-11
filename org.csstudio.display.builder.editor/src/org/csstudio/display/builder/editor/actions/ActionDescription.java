/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.actions;

import java.io.InputStream;
import java.net.URL;

import org.csstudio.display.builder.util.UtilPlugin;

/** Description of an action
 *
 *  Wraps the functionality, i.e. icon, tool tip, and what to execute,
 *  for use in a Java FX Button or Eclipse Action.
 *
 *  @author Kay Kasemir
 */
public abstract class ActionDescription
{
    private final String icon;
    private final String tool_tip;

    /** @param icon Icon path
     *  @param tool_tip Tool tip
     */
    public ActionDescription(final String icon, final String tool_tip)
    {
        this.icon = icon;
        this.tool_tip = tool_tip;
    }

    /** @return URL for icon
     *  @throws Exception on error
     */
    public URL getIconURL() throws Exception
    {
        return new URL(icon);
    }

    /** @return Stream for icon's content
     *  @throws Exception on error
     */
    public InputStream getIconStream() throws Exception
    {
        return UtilPlugin.getStream(icon);
    }

    /** @return Tool tip */
    public String getToolTip()
    {
        return tool_tip;
    }

    /** Execute the action
     *
     *  <p>For plain "do it" actions, 'selected' will always be <code>true</code>.
     *  For actions that are bound to a toggle button because some feature
     *  is enabled or disabled, 'selected' reflects if the button was toggled 'on' or 'off'.

     *  @param selected Selected?
     */
    abstract public void run(boolean selected);
}
