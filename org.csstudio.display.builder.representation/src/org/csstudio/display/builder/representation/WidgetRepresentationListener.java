/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.properties.ActionInfo;

/** Listener to a widget representation
 *
 *  <p>Provides notification of events (action invoked, ..)
 *  independent from the underlying toolkit (JavaFX, ..)
 *
 *  @author Kay Kasemir
 */
public interface WidgetRepresentationListener
{
    /** User invoked an action
     *
     *  @param widget {@link Widget} on which user invoked the action
     *  @param action Information about the action that user wants to be executed
     */
    public void handleAction(Widget widget, ActionInfo action);
}
