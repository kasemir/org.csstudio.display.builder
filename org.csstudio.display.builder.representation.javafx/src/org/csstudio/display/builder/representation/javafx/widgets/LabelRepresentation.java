/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.beans.PropertyChangeEvent;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class LabelRepresentation extends JFXBaseRepresentation<Label, LabelWidget>
{
    private final DirtyFlag dirty_content = new DirtyFlag();

    public LabelRepresentation(final ToolkitRepresentation<Group, Node> toolkit,
                               final LabelWidget model_widget)
    {
        super(toolkit, model_widget);
    }

    @Override
    public Label createJFXNode() throws Exception
    {
        final Label label = new Label();
        // TODO Set font
        //        final Font font = Font.font("Sans", FontWeight.BOLD, 18);
        //        label.setFont(font);
        return label;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.displayText().addPropertyListener(this::contentChanged);
    }

    private void contentChanged(final PropertyChangeEvent event)
    {
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_content.checkAndClear())
            jfx_node.setText(model_widget.displayText().getValue());
    }
}
