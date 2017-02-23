/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.top;

import org.csstudio.display.builder.rcp.Preferences;
import org.eclipse.core.expressions.PropertyTester;

/** Show/hide the "Top Displays" toolbar button if there are any top displays.
 *
 *  <p>Property 'org.csstudio.display.builder.rcp.haveTopDisplays'
 *  defined in plugin.xml to allow 'visibleWhen' expression.
 *
 *  @author Kay Kasemir
 */
public class HaveTopDisplays extends PropertyTester
{
    /** Doesn't really need any parameters,
     *  but PropertyTester is always invoked 'with' something.
     *  So it's called with the 'activePerspective'.
     */
    @Override
    public boolean test(final Object receiver, final String property,
                        final Object[] args, final Object expectedValue)
    {
        // Could check the receiver, which contains the 'activePerspective',
        // and only enable in the display runtime...
//        if (! (receiver instanceof String))
//            return false;
//        final String activePerspective = (String) receiver;
//        if (! activePerspective.contains("display.builder"))
//            return false;
        final String setting = Preferences.getTopDisplays();
        return !setting.isEmpty();
    }
}
