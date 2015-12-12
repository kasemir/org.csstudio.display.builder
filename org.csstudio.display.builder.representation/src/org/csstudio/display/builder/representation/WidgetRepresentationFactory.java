/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation;

import org.csstudio.display.builder.model.Widget;

/** Factory for creating Toolkit representation of a widget
 *
 *  <p>Extension point allow contributing implementations
 *  of this interface.
 *  
 *  TODO Change WidgetRepresentation to have no-arg constructor,
 *  instead using init(...), so that the WidgetRepresentation can be
 *  created from the extension point registry without need for WidgetRepresentationFactory
 *  
 *  @author Kay Kasemir
 *  @param <TWP> Toolkit widget parent class
 *  @param <TW> Toolkit widget base class
 *  @param <MW> Model widget class
 */
public interface WidgetRepresentationFactory<TWP, TW, MW extends Widget>
{
    /** Extension point ID for contributing {@link WidgetRepresentation}s */
    public static final String EXTENSION_POINT = "org.csstudio.display.builder.representation.widgets";

    /** Construct representation for a model widget
     *  @param toolkit Toolkit helper
     *  @param model_widget Model widget
     */
	public WidgetRepresentation<TWP, TW, MW> create(final ToolkitRepresentation<TWP, TW> toolkit,
    												final MW model_widget);
}

