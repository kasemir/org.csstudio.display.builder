/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.Collections;
import java.util.List;

/** Information about actions
 *
 *  @author Kay Kasemir
 */
public class ActionInfos
{
    public static final ActionInfos EMPTY = new ActionInfos(Collections.emptyList());

    final private List<ActionInfo> actions;
    final private boolean execute_as_one;

    public ActionInfos(final List<ActionInfo> actions)
    {
        this(actions, false);
    }

    public ActionInfos(final List<ActionInfo> actions, final boolean execute_as_one)
    {
        this.actions = Collections.unmodifiableList(actions);
        this.execute_as_one = execute_as_one;
    }

    /** @return List of actions */
    public List<ActionInfo> getActions()
    {
        return actions;
    }

    /** @return Should all actions on list be executed as one, or are they separate actions? */
    public boolean isExecutedAsOne()
    {
        return execute_as_one;
    }
}
