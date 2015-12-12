/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.WidgetRepresentation;
import org.csstudio.display.builder.representation.WidgetRepresentationFactory;

import javafx.scene.Group;
import javafx.scene.Node;

/** Creates JavaFX representation for model widget
 *  @author Kay Kasemir
 */
public class LabelRepresentationFactory implements WidgetRepresentationFactory<Group, Node, LabelWidget>
{
	@Override
	public WidgetRepresentation<Group, Node, LabelWidget> create(final ToolkitRepresentation<Group, Node> toolkit,
																 final LabelWidget model_widget)
	{
		return new LabelRepresentation(toolkit, model_widget);
	}
};
